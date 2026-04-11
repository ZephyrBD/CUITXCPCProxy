package top.techmczs.cuitxcpctool.services.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.techmczs.cuitxcpctool.constant.MessageConstant;
import top.techmczs.cuitxcpctool.common.QueueTaskStatus;
import top.techmczs.cuitxcpctool.constant.SseEventConstant;
import top.techmczs.cuitxcpctool.dto.AdminDTO;
import top.techmczs.cuitxcpctool.dto.AuthTaskDTO;
import top.techmczs.cuitxcpctool.dto.DjTeamDTO;
import top.techmczs.cuitxcpctool.entity.AuthTask;
import top.techmczs.cuitxcpctool.entity.DjTeam;
import top.techmczs.cuitxcpctool.entity.TeamClient;
import top.techmczs.cuitxcpctool.exception.*;
import top.techmczs.cuitxcpctool.mapper.AuthTaskMapper;
import top.techmczs.cuitxcpctool.mapper.DjTeamMapper;
import top.techmczs.cuitxcpctool.mapper.TeamClientMapper;
import top.techmczs.cuitxcpctool.properties.DomjudgeProperties;
import top.techmczs.cuitxcpctool.properties.JwtProperties;
import top.techmczs.cuitxcpctool.result.Result;
import top.techmczs.cuitxcpctool.services.DjAuthService;
import top.techmczs.cuitxcpctool.services.SseManagerService;
import top.techmczs.cuitxcpctool.utils.JwtUtil;
import top.techmczs.cuitxcpctool.common.SqlQueue;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DjAuthServiceImpl implements DjAuthService {

    private final SseManagerService sseManagerService;

    // 原有依赖不变
    private final DomjudgeProperties domjudgeProperties;
    private final JwtProperties jwtProperties;
    private final SqlQueue<AuthTask> authTaskQueue;
    private final DjTeamMapper djTeamMapper;
    private final AuthTaskMapper authTaskMapper;
    private final TeamClientMapper teamClientMapper;

    @Scheduled(fixedRate = 60000)
    public void rePushPendingAuthTask() {
        // 查询待处理的验证任务
        List<AuthTask> pendingTasks = getPendingAuthTasks();
        if (pendingTasks.isEmpty()) {
            // 发送全局心跳
            sseManagerService.sendHeartbeat();
            return;
        }
        // 广播未处理任务
        pushAuthTaskToSSE(pendingTasks);
    }

    private void pushAuthTaskToSSE(AuthTask authTask) {
        List<AuthTaskDTO> dtoList = buildTaskDTOList(Collections.singletonList(authTask));
        sseManagerService.broadcast(SseEventConstant.AUTH_TASK, Result.success(dtoList));
    }

    private void pushAuthTaskToSSE(List<AuthTask> authTasks) {
        List<AuthTaskDTO> taskDTOList = buildTaskDTOList(authTasks);
        sseManagerService.broadcast(SseEventConstant.AUTH_TASK, Result.success(taskDTOList));
    }

    // 工具：查询待处理任务
    private List<AuthTask> getPendingAuthTasks() {
        return authTaskQueue.dequeueTasks(authTaskMapper);
    }

    // 工具：组装DTO
    private List<AuthTaskDTO> buildTaskDTOList(List<AuthTask> tasks) {
        if (tasks.isEmpty()) return Collections.emptyList();

        List<String> examNums = tasks.stream().map(AuthTask::getExamNum).toList();
        List<DjTeam> teamList = djTeamMapper.selectByIds(examNums);
        Map<String, DjTeam> teamMap = new HashMap<>();
        teamList.forEach(team -> teamMap.put(team.getExamNumber(), team));

        return tasks.stream().map(task -> {
            DjTeam team = teamMap.get(task.getExamNum());
            String teamName = team == null ? MessageConstant.UNKNOWN_TEAM : team.getTeamName();
            return new AuthTaskDTO(
                    task.getId(),
                    teamName,
                    task.getExamNum(),
                    task.getCreateTime(),
                    task.getStatus()
            );
        }).toList();
    }

    @Override
    public boolean verifyToken(String token) {
        try {
            if (token == null || token.isBlank()) {
                throw new IllegalTokenException(MessageConstant.ILLEGAL_TOKEN);
            }
            JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public DjTeamDTO verifyClientAndGetToken(String examNum, String clientId,String userAgent) {
        //判断是否存在Client ID
        if(clientId == null || clientId.isBlank()){
            log.warn(MessageConstant.ILLEGAL_CLIENT,examNum);
            throw new IllegalClientException(MessageConstant.ILLEGAL_CLIENT);
        }

        // 查询队伍
        DjTeam djTeam = djTeamMapper.selectOne(new QueryWrapper<DjTeam>().lambda().eq(DjTeam::getExamNumber,examNum));

        if(djTeam == null){
            log.error(MessageConstant.TEAM_NOT_FOUND,examNum);
            throw new TeamNotExistException(MessageConstant.TEAM_NOT_FOUND);
        }

        String oldClientId = getClientIdFromTeamClient(djTeam.getExamNumber());

        // 判断是否是新设备
        boolean isNewDevice = (oldClientId != null) && !oldClientId.isBlank() && !clientId.equals(oldClientId);

        AuthTask authTask = new AuthTask(oldClientId,clientId,djTeam.getExamNumber());

        log.info(MessageConstant.TEAM_NEED_LOGIN,djTeam.getTeamName());

        //是新设备，发前端判断
        if (isNewDevice) {
            enqueueAuthTask(new AuthTask(oldClientId,clientId,djTeam.getExamNumber()),false);
            log.warn(MessageConstant.TEAM_LOGIN_AGAIN,djTeam.getTeamName());
            return null;
        }

        enqueueAuthTask(authTask.setOldClientId(clientId),true);
        DjTeamDTO djTeamDTO = new DjTeamDTO().setToken(verifyAndGetToken(userAgent,examNum));
        saveClientIdToTeamClient(examNum,clientId);
        BeanUtils.copyProperties(djTeam,djTeamDTO);
        String url = domjudgeProperties.getVerifyUrl();
        djTeamDTO.setDjUrl(url).setLoginTime(LocalDateTime.now());
        return djTeamDTO;
    }

    @Override
    public DjTeamDTO getApprovedTeamInfo(String examNum) {
        DjTeam djTeam = djTeamMapper.selectById(examNum);
        if (djTeam == null) {
            throw new TeamNotExistException();
        }
        DjTeamDTO djTeamDTO = new DjTeamDTO();
        BeanUtils.copyProperties(djTeam, djTeamDTO);
        String token = JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtl(),examNum);
        String url = domjudgeProperties.getVerifyUrl();
        djTeamDTO.setToken(token).setDjUrl(url).setLoginTime(LocalDateTime.now());
        return djTeamDTO;
    }

    @Override
    public QueueTaskStatus getAuthStatus(String examNum,String clientId) {
        AuthTask authTask = getAuthTaskInQueue(examNum,clientId);
        if (authTask == null) {
            return QueueTaskStatus.PENDING;
        }
        return authTask.getStatus();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptAuth(Long taskId) {
        AuthTask authTask = authTaskMapper.selectById(taskId);
        if (authTask == null) {
            throw new AuthTaskNotExistException(MessageConstant.AUTH_TASK_NOT_EXIST);
        }
        authTask.setStatus(QueueTaskStatus.DONE);
        authTaskMapper.updateById(authTask);

        TeamClient teamClient = new TeamClient()
                .setExamNum(authTask.getExamNum())
                .setClientId(authTask.getNowClientId())
                .setLoginTime(LocalDateTime.now());
        teamClientMapper.insertOrUpdate(teamClient);
    }

    @Override
    public void denyAuth(Long taskId) {
        AuthTask authTask = authTaskMapper.selectById(taskId);
        if (authTask == null) {
            throw new AuthTaskNotExistException(MessageConstant.AUTH_TASK_NOT_EXIST);
        }
        authTask.setStatus(QueueTaskStatus.DENY);
        authTaskMapper.updateById(authTask);
    }

    @Override
    public AdminDTO getAdminToDomjudgeToken() {
        return new AdminDTO()
                .setUrl(domjudgeProperties.getVerifyUrl())
                .setToken(JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtl(),"admin"));
    }

    @Override
    public IPage<AuthTaskDTO> queryAuthTasksByPage(int curPage) {
        Page<AuthTask> taskPage = new Page<>(curPage,10);
        authTaskMapper.selectPage(taskPage,null);

        List<String> tmp = taskPage.getRecords().stream()
                .map(AuthTask::getExamNum)
                .toList();

        List<DjTeam> djTeamList = djTeamMapper.selectByIds(tmp);
        Map<String,DjTeam> teamMap = new HashMap<>();
        djTeamList.forEach(djTeam -> teamMap.put(djTeam.getExamNumber(),djTeam));

        List<AuthTaskDTO> dtoList = taskPage.getRecords().stream().map(task -> {
            DjTeam djTeam = teamMap.get(task.getExamNum());
            return new AuthTaskDTO(
                    task.getId(),
                    djTeam.getTeamName(),
                    task.getExamNum(),
                    task.getCreateTime(),
                    task.getStatus()
            );
        }).toList();

        Page<AuthTaskDTO> dtoPage = new Page<>(curPage, 10);
        dtoPage.setRecords(dtoList)
                .setTotal(taskPage.getTotal()) // 总条数
                .setCurrent(taskPage.getCurrent())  // 当前页
                .setSize(taskPage.getSize()); // 每页条数
        return dtoPage;
    }

    @Override
    public void clearAuthTaskQueue() {
        authTaskQueue.clear(AuthTask.class);
    }

    @Override
    public void clearTeamClient() {
        teamClientMapper.clearAndResetAutoIncrement();
    }


    private void enqueueAuthTask(AuthTask authTask,boolean isAutoAccept) {

        if(isAutoAccept){
            authTaskQueue.enqueue(authTask,authTaskMapper,QueueTaskStatus.AUTO_DONE);
            pushAuthTaskToSSE(authTask);
            return;
        }
        // 任务入队+入库
        AuthTask tmp = getAuthTaskInQueue(authTask.getExamNum(),authTask.getNowClientId());
        if(tmp!=null) {
            pushAuthTaskToSSE(tmp);
            return;
        }

        authTaskQueue.enqueue(authTask, authTaskMapper);
        // 组装DTO并推送全局SSE
        pushAuthTaskToSSE(authTask);

    }

    private String getClientIdFromTeamClient(String examNum){
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TeamClient> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.lambda().eq(TeamClient::getExamNum, examNum);
        TeamClient teamClient = teamClientMapper.selectOne(queryWrapper);
        return teamClient == null ? null : teamClient.getClientId();
    }

    private AuthTask getAuthTaskInQueue(String examNum, String clientId) {
        QueryWrapper<AuthTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AuthTask::getExamNum, examNum).eq(AuthTask::getNowClientId,clientId);
        return authTaskMapper.selectOne(queryWrapper);
    }

    private void saveClientIdToTeamClient(String examNum,String clientId) {
        TeamClient teamClient = new TeamClient()
                .setExamNum(examNum)
                .setClientId(clientId)
                .setLoginTime(LocalDateTime.now());
        teamClientMapper.insertOrUpdate(teamClient);
    }

    private String verifyAndGetToken(String userAgent,String examNum) {
        String specialUA = domjudgeProperties.getSpecialClientUserAgent();
        if (userAgent == null || !userAgent.contains(specialUA)) {
            throw new IllegalClientException(MessageConstant.ILLEGAL_CLIENT);
        }
        return JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtl(), examNum);
    }
}