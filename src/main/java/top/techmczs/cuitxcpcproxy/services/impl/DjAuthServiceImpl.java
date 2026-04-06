package top.techmczs.cuitxcpcproxy.services.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 校验之前发出去的token是否是SpringBoot发出的
     * @param token Nginx请求的需要验证的token
     * @return 一个布尔值，表示是否验证通过
     */
    @Override
    public boolean verifyToken(String token) {
        try {
//            // 没有启用浏览器验证
//            if(!domjudgeProperties.isUseSpecialClient()){
//                throw new NoClientCheckException(MessageConstant.NO_CLIENT_CHECK);
//            }
            // token 验证不通过
            if (token == null || token.isBlank()) {
                throw new IllegalTokenException(MessageConstant.ILLEGAL_TOKEN);
            }
            JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
           return false; // 非法
        }
    }

    @Override
    public DjTeamDTO verifyClientAndGetToken(String examNum, String clientId,String userAgent) {
//        // 判断设置是否启用
//        if(!domjudgeProperties.isUseSpecialClient()){
//            throw new NoClientCheckException(MessageConstant.NO_CLIENT_CHECK);
//        }
        // 判断是否有ClientId传入
        if(clientId == null || clientId.isBlank()){
            throw new IllegalClientException(MessageConstant.ILLEGAL_CLIENT);
        }
        DjTeamDTO djTeamDTO = new DjTeamDTO().setToken(verifyAndGetToken(userAgent,examNum));
        DjTeam djTeam = djTeamMapper.selectOne(new QueryWrapper<DjTeam>().lambda().eq(DjTeam::getExamNumber,examNum));
        // 如果找不到队伍，说明队伍不是考号错误
        if(djTeam == null){
            throw new TeamNotExistException(MessageConstant.TEAM_NOT_FOUND);
        }
        String oldClientId = getClientIdFromTeamClient(djTeam.getExamNumber());
        // 如果数据库中存在ClientID了，说明是重复登录
        boolean hasOldClient = oldClientId != null && !oldClientId.isBlank();
        boolean isNewDevice = hasOldClient && !clientId.equals(oldClientId);
        if (isNewDevice) {
            // UN_TODO 加入登录审核队列
            enqueueAuthTask(new AuthTask(oldClientId,clientId,djTeam.getExamNumber()));
            // loginQueue.add(djTeam);
            throw new TeamTryLoginAgainException(djTeam.getTeamName() + MessageConstant.TEAM_LOGIN_AGAIN);
        }
        // 如上都通过说明是第一次登录，把ClientId写入数据库
        saveClientIdToTeamClient(examNum,clientId);
        BeanUtils.copyProperties(djTeam,djTeamDTO);
        String url = domjudgeProperties.getVerifyUrl();
        djTeamDTO.setDjUrl(url).setLoginTime(LocalDateTime.now());
        return djTeamDTO;
    }

    @Override
    public DjTeamDTO getApprovedTeamInfo(String examNum) {
        // 查询队伍基础信息
        DjTeam djTeam = djTeamMapper.selectById(examNum);
        if (djTeam == null) {
            throw new TeamNotExistException(MessageConstant.TEAM_NOT_FOUND);
        }
        // 组装DjTeamDTO（包含token等信息）
        DjTeamDTO djTeamDTO = new DjTeamDTO();
        BeanUtils.copyProperties(djTeam, djTeamDTO);
        // 重新生成有效token
        String token = JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtl(),examNum);
        String url = domjudgeProperties.getVerifyUrl();
        djTeamDTO.setToken(token).setDjUrl(url).setLoginTime(LocalDateTime.now());
        return djTeamDTO;
    }

//    @Override
//    public AuthTaskDTO getAuthTopTaskFromQueue() {
//        AuthTask authTask = dequeueAuthTask();
//        if (authTask == null) {
//            // 无待处理任务返回空DTO（或抛自定义异常）
//            return new AuthTaskDTO();
//        }
//        System.out.println("here");
//        DjTeam djTeam = djTeamMapper.selectById(authTask.getExamNum());
//        if (djTeam == null) {
//            throw new TeamNotExistException(MessageConstant.TEAM_NOT_FOUND);
//        }
//        return new AuthTaskDTO(authTask.getId(),djTeam.getTeamName(),authTask.getExamNum(),authTask.getCreateTime(),authTask.getStatus());
//    }

    @Override
    public void pushAuthTaskBySse(SseEmitter emitter) {
        try {
            // 持续监听队列（线程安全）
            while (!Thread.currentThread().isInterrupted()) {
                // 1. 从队列获取任务（复用你的原有方法）
                AuthTask authTask = dequeueAuthTask();

                // 2. 无任务 → 发送心跳保活，1秒检查一次
                if (authTask == null) {
                    emitter.send(SseEmitter.event().comment("heartbeat"));
                    Thread.sleep(1000);
                    continue;
                }

                System.out.println("here");
                AuthTaskDTO authTaskDTO;
                try {
                    // 3. 复用你的原有业务逻辑：查询团队信息
                    DjTeam djTeam = djTeamMapper.selectById(authTask.getExamNum());
                    if (djTeam == null) {
                        throw new TeamNotExistException(MessageConstant.TEAM_NOT_FOUND);
                    }
                    // 4. 复用你的原有DTO构造方式
                    authTaskDTO = new AuthTaskDTO(
                            authTask.getId(),
                            djTeam.getTeamName(),
                            authTask.getExamNum(),
                            authTask.getCreateTime(),
                            authTask.getStatus()
                    );
                } catch (TeamNotExistException e) {
                    // 团队不存在 → 推送错误信息（不中断SSE连接）
                    Result<?> errorResult = Result.error(e.getMessage());
                    emitter.send(SseEmitter.event().data(errorResult));
                    Thread.sleep(1000);
                    continue;
                }

                // 5. 【核心】向客户端推送任务（和原接口返回格式完全一致）
                Result<AuthTaskDTO> successResult = Result.success(authTaskDTO);
                emitter.send(SseEmitter.event().data(successResult));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.error("授权任务SSE推送失败：客户端已断开");
        } finally {
            emitter.complete();
        }
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
        //将队列中的Task标记为完成
        AuthTask authTask = authTaskMapper.selectById(taskId);
        if (authTask == null) {
            throw new AuthTaskNotExistException(MessageConstant.AUTH_TASK_NOT_EXIST);
        }
        authTask.setStatus(QueueTask.Status.DONE);
        authTaskMapper.updateById(authTask);

        // 3. 更新TeamClient（不存在则插入）
        TeamClient teamClient = new TeamClient()
                .setExamNum(authTask.getExamNum())
                .setClientId(authTask.getNowClientId())
                .setLoginTime(LocalDateTime.now());
        // 优化：saveOrUpdate自动判断新增/更新
        teamClientMapper.insertOrUpdate(teamClient);
    }

    @Override
    public void denyAuth(Long taskId) {
        //将队列中的Task标记为完成
        AuthTask authTask = authTaskMapper.selectById(taskId);
        if (authTask == null) {
            throw new AuthTaskNotExistException(MessageConstant.AUTH_TASK_NOT_EXIST);
        }
        authTask.setStatus(QueueTask.Status.DENY);
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
    }

    @Override
    public void clearTeamClient() {
        teamClientMapper.clearAndResetAutoIncrement();
    }

    private String getClientIdFromTeamClient(String examNum){
        QueryWrapper<TeamClient> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TeamClient::getExamNum, examNum);
        TeamClient teamClient = teamClientMapper.selectOne(queryWrapper);
        // 首次登录无记录返回null
        return teamClient == null ? null : teamClient.getClientId();
    }

    /**
     * 工具方法，保存ClientId到TeamClient表
     */
    private void saveClientIdToTeamClient(String examNum,String clientId) {
        TeamClient teamClient = new TeamClient()
                .setExamNum(examNum)
                .setClientId(clientId)
                .setLoginTime(LocalDateTime.now());
        // 存在则更新，不存在则插入
        teamClientMapper.insertOrUpdate(teamClient);
    }

    /**
     * 工具方法
     * 进行UserAgent验证并返回跳转的链接
     * 一般配合Nginx
     * 链接格式：{org.domjudge.host}:{org.domjudge.port}/{org.domjudge.nginx-verify-route-path}?token={token}
     * @param userAgent 提供的UA
     * @return 带有token的链接
     */
    private String verifyAndGetToken(String userAgent,String examNum) {
        // UA空安全校验
        String specialUA = domjudgeProperties.getSpecialClientUserAgent();
        if (userAgent == null || !userAgent.contains(specialUA)) {
            throw new IllegalClientException(MessageConstant.ILLEGAL_CLIENT);
        }
        // 生成并返回JWT
        //String url = domjudgeProperties.getHost() + ":" + domjudgeProperties.getPort() + domjudgeProperties.getNginxVerifyRoutePath();
        //return url + "?token=" + token;
        return JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtl(), examNum);
    }

    private void enqueueAuthTask(AuthTask authTask) {
        sqlQueue.enqueue(authTask, authTaskMapper);
    }

    private AuthTask dequeueAuthTask() {
        return sqlQueue.dequeue(authTaskMapper, new QueryWrapper<AuthTask>().lambda()
                .eq(AuthTask::getStatus, QueueTask.Status.PENDING)
                .orderByAsc(AuthTask::getCreateTime)
                .last("LIMIT 1"));
    }
}
