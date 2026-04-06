package top.techmczs.cuitxcpcproxy.services.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.techmczs.cuitxcpcproxy.constant.MessageConstant;
import top.techmczs.cuitxcpcproxy.dto.AdminDTO;
import top.techmczs.cuitxcpcproxy.dto.AuthTaskDTO;
import top.techmczs.cuitxcpcproxy.dto.DjTeamDTO;
import top.techmczs.cuitxcpcproxy.entity.DjTeam;
import top.techmczs.cuitxcpcproxy.entity.TeamClient;
import top.techmczs.cuitxcpcproxy.entity.queuetask.AuthTask;
import top.techmczs.cuitxcpcproxy.entity.queuetask.QueueTask;
import top.techmczs.cuitxcpcproxy.exception.*;
import top.techmczs.cuitxcpcproxy.mapper.AuthTaskMapper;
import top.techmczs.cuitxcpcproxy.mapper.DjTeamMapper;
import top.techmczs.cuitxcpcproxy.mapper.TeamClientMapper;
import top.techmczs.cuitxcpcproxy.properties.DomjudgeProperties;
import top.techmczs.cuitxcpcproxy.properties.JwtProperties;
import top.techmczs.cuitxcpcproxy.result.Result;
import top.techmczs.cuitxcpcproxy.services.DjAuthService;
import top.techmczs.cuitxcpcproxy.utils.JwtUtil;
import top.techmczs.cuitxcpcproxy.utils.SqlQueue;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
@RequiredArgsConstructor
@Slf4j
public class DjAuthServiceImpl implements DjAuthService {

    private final DomjudgeProperties domjudgeProperties;
    private final JwtProperties jwtProperties;
    private final SqlQueue<AuthTask> sqlQueue;
    private final DjTeamMapper djTeamMapper;
    private final AuthTaskMapper authTaskMapper;
    private final TeamClientMapper teamClientMapper;

    // ✅ 修复1：线程安全的客户端列表（管理所有管理员前端连接）
    private final Set<SseEmitter> AUTH_CLIENTS = new CopyOnWriteArraySet<>();
    // 缓存最后一次推送的任务ID，避免重复推送
    private final Set<Long> LAST_PUSH_TASK_IDS = Collections.synchronizedSet(new HashSet<>());

    // ===================== ✅ 修复2：注册SSE客户端（统一管理）=====================
    @Override
    public void registerAuthClient(SseEmitter emitter) {
        AUTH_CLIENTS.add(emitter);

        // 生命周期管理（自动清理断开的客户端）
        emitter.onCompletion(() -> {
            AUTH_CLIENTS.remove(emitter);
            log.info("授权SSE连接关闭，剩余客户端：{}", AUTH_CLIENTS.size());
        });
        emitter.onTimeout(() -> {
            AUTH_CLIENTS.remove(emitter);
            emitter.complete();
        });
        emitter.onError(ex -> {
            AUTH_CLIENTS.remove(emitter);
            emitter.completeWithError(ex);
        });

        // 首次连接发送成功消息
        try {
            emitter.send(SseEmitter.event().name("connect").data(Result.success("授权SSE连接成功")));
        } catch (IOException e) {
            AUTH_CLIENTS.remove(emitter);
        }
    }

    // ===================== ✅ 修复3：定时推送授权任务（替代死循环）=====================
    @Scheduled(fixedRate = 1000)
    public void pushAuthTaskToClients() {
        // 1. 查询【待处理】的授权任务（只查不消费！）
        List<AuthTask> pendingTasks = getPendingAuthTasks();
        if (pendingTasks.isEmpty()) {
            sendHeartbeat();
            return;
        }

        // 2. 组装任务DTO
        List<AuthTaskDTO> taskDTOList = buildTaskDTOList(pendingTasks);

        // 3. 广播给所有管理员客户端
        broadcastAuthTask(taskDTOList);

        // 4. 记录已推送ID，避免重复推送
        pendingTasks.forEach(task -> LAST_PUSH_TASK_IDS.add(task.getId()));
    }

    // ===================== 工具：查询待处理任务 =====================
    private List<AuthTask> getPendingAuthTasks() {
        QueryWrapper<AuthTask> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(AuthTask::getStatus, QueueTask.Status.PENDING)
                .notIn(!LAST_PUSH_TASK_IDS.isEmpty(), AuthTask::getId, LAST_PUSH_TASK_IDS)
                .orderByAsc(AuthTask::getCreateTime);
        return authTaskMapper.selectList(wrapper);
    }

    // ===================== 工具：组装DTO =====================
    private List<AuthTaskDTO> buildTaskDTOList(List<AuthTask> tasks) {
        if (tasks.isEmpty()) return Collections.emptyList();

        List<String> examNums = tasks.stream().map(AuthTask::getExamNum).toList();
        List<DjTeam> teamList = djTeamMapper.selectByIds(examNums);
        Map<String, DjTeam> teamMap = new HashMap<>();
        teamList.forEach(team -> teamMap.put(team.getExamNumber(), team));

        return tasks.stream().map(task -> {
            DjTeam team = teamMap.get(task.getExamNum());
            String teamName = team == null ? "未知队伍" : team.getTeamName();
            return new AuthTaskDTO(
                    task.getId(),
                    teamName,
                    task.getExamNum(),
                    task.getCreateTime(),
                    task.getStatus()
            );
        }).toList();
    }

    // ===================== 工具：广播消息 =====================
    private void broadcastAuthTask(List<AuthTaskDTO> dtoList) {
        Iterator<SseEmitter> iterator = AUTH_CLIENTS.iterator();
        while (iterator.hasNext()) {
            SseEmitter emitter = iterator.next();
            try {
                emitter.send(SseEmitter.event()
                        .name("authTask")
                        .data(Result.success(dtoList)));
            } catch (Exception e) {
                iterator.remove();
            }
        }
    }

    // ===================== 工具：心跳保活 =====================
    private void sendHeartbeat() {
        Iterator<SseEmitter> iterator = AUTH_CLIENTS.iterator();
        while (iterator.hasNext()) {
            SseEmitter emitter = iterator.next();
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
            } catch (Exception e) {
                iterator.remove();
            }
        }
    }

    // ===================== 以下为原有业务逻辑（完全不变）=====================
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
        if(clientId == null || clientId.isBlank()){
            throw new IllegalClientException(MessageConstant.ILLEGAL_CLIENT);
        }
        DjTeamDTO djTeamDTO = new DjTeamDTO().setToken(verifyAndGetToken(userAgent,examNum));
        DjTeam djTeam = djTeamMapper.selectOne(new QueryWrapper<DjTeam>().lambda().eq(DjTeam::getExamNumber,examNum));
        if(djTeam == null){
            throw new TeamNotExistException(MessageConstant.TEAM_NOT_FOUND);
        }
        String oldClientId = getClientIdFromTeamClient(djTeam.getExamNumber());
        boolean hasOldClient = oldClientId != null && !oldClientId.isBlank();
        boolean isNewDevice = hasOldClient && !clientId.equals(oldClientId);
        if (isNewDevice) {
            enqueueAuthTask(new AuthTask(oldClientId,clientId,djTeam.getExamNumber()));
            throw new TeamTryLoginAgainException(djTeam.getTeamName() + MessageConstant.TEAM_LOGIN_AGAIN);
        }
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
            throw new TeamNotExistException(MessageConstant.TEAM_NOT_FOUND);
        }
        DjTeamDTO djTeamDTO = new DjTeamDTO();
        BeanUtils.copyProperties(djTeam, djTeamDTO);
        String token = JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtl(),examNum);
        String url = domjudgeProperties.getVerifyUrl();
        djTeamDTO.setToken(token).setDjUrl(url).setLoginTime(LocalDateTime.now());
        return djTeamDTO;
    }

    @Override
    public QueueTask.Status getAuthStatus(String examNum) {
        QueryWrapper<AuthTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AuthTask::getExamNum, examNum);
        AuthTask authTask = authTaskMapper.selectOne(queryWrapper);
        if (authTask == null) {
            return QueueTask.Status.PENDING;
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
        authTask.setStatus(QueueTask.Status.DONE);
        authTaskMapper.updateById(authTask);

        TeamClient teamClient = new TeamClient()
                .setExamNum(authTask.getExamNum())
                .setClientId(authTask.getNowClientId())
                .setLoginTime(LocalDateTime.now());
        teamClientMapper.insertOrUpdate(teamClient);
        // 推送后清除缓存，刷新列表
        LAST_PUSH_TASK_IDS.remove(taskId);
    }

    @Override
    public void denyAuth(Long taskId) {
        AuthTask authTask = authTaskMapper.selectById(taskId);
        if (authTask == null) {
            throw new AuthTaskNotExistException(MessageConstant.AUTH_TASK_NOT_EXIST);
        }
        authTask.setStatus(QueueTask.Status.DENY);
        authTaskMapper.updateById(authTask);
        LAST_PUSH_TASK_IDS.remove(taskId);
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
        List<String> tmp =taskPage.getRecords().stream().map(AuthTask::getExamNum).toList();
        if(tmp.isEmpty()){
            return new Page<>(curPage, 10);
        }
        List<DjTeam> djTeamList = djTeamMapper.selectByIds(tmp);
        Map<String,DjTeam> teamMap = new HashMap<>();
        djTeamList.forEach(djTeam -> teamMap.put(djTeam.getExamNumber(),djTeam));
        List<AuthTaskDTO> dtoList = taskPage.getRecords().stream().map(task -> {
            DjTeam djTeam = teamMap.get(task.getExamNum());
            return new AuthTaskDTO(task.getId(),djTeam.getTeamName(),task.getExamNum(),task.getCreateTime(),task.getStatus());
        }).toList();
        Page<AuthTaskDTO> dtoPage = new Page<>(curPage,10);
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public void clearAuthTaskQueue() {
        sqlQueue.clear(AuthTask.class);
        LAST_PUSH_TASK_IDS.clear();
    }

    @Override
    public void clearTeamClient() {
        teamClientMapper.clearAndResetAutoIncrement();
    }

    private String getClientIdFromTeamClient(String examNum){
        QueryWrapper<TeamClient> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TeamClient::getExamNum, examNum);
        TeamClient teamClient = teamClientMapper.selectOne(queryWrapper);
        return teamClient == null ? null : teamClient.getClientId();
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

    private void enqueueAuthTask(AuthTask authTask) {
        sqlQueue.enqueue(authTask, authTaskMapper);
    }
}