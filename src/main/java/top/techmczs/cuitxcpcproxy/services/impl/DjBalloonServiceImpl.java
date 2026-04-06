package top.techmczs.cuitxcpcproxy.services.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.techmczs.cuitxcpcproxy.constant.MessageConstant;
import top.techmczs.cuitxcpcproxy.dto.DjBalloonDTO;
import top.techmczs.cuitxcpcproxy.entity.Balloon;
import top.techmczs.cuitxcpcproxy.properties.DomjudgeProperties;
import top.techmczs.cuitxcpcproxy.result.Result;
import top.techmczs.cuitxcpcproxy.services.DjBalloonService;
import top.techmczs.cuitxcpcproxy.utils.TimeUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
@Slf4j
@RequiredArgsConstructor
public class DjBalloonServiceImpl implements DjBalloonService {

    private final DomjudgeProperties domjudgeProperties;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private ObjectMapper objectMapper;

    // 线程安全的客户端列表
    private final Set<SseEmitter> CLIENTS = new CopyOnWriteArraySet<>();

    // 待推送队列
    private final List<DjBalloonDTO> NEW_BALLOONS = new CopyOnWriteArrayList<>();
    // 首解标记
    private final Set<String> FIRST_SOLVE = Collections.synchronizedSet(new HashSet<>());

    // ===================== 注册SSE客户端 =====================
    @Override
    public void registerClient(SseEmitter emitter) {
        CLIENTS.add(emitter);

        // 连接生命周期管理
        emitter.onCompletion(() -> {
            CLIENTS.remove(emitter);
            log.info("气球SSE连接正常关闭，剩余客户端：{}", CLIENTS.size());
        });
        emitter.onTimeout(() -> {
            CLIENTS.remove(emitter);
            log.info("气球SSE连接超时，剩余客户端：{}", CLIENTS.size());
            emitter.complete();
        });
        emitter.onError(ex -> {
            CLIENTS.remove(emitter);
            log.error("气球SSE连接异常：{}，剩余客户端：{}", ex.getMessage(), CLIENTS.size());
            emitter.completeWithError(ex);
        });

        // 连接成功推送消息
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data(Result.success("气球SSE连接成功")));
        } catch (IOException e) {
            CLIENTS.remove(emitter);
            log.error("气球SSE初始化消息发送失败", e);
        }
    }

    // ===================== 定时推送气球到前端（核心修复） =====================
    @Scheduled(fixedRate = 500)
    public void pushBalloonToClients() {
//        // ===================== 调试专用：强制推送测试气球（上线删除） =====================
//        if (NEW_BALLOONS.isEmpty()) {
//            try {
//                DjBalloonDTO testDto = new DjBalloonDTO();
//                testDto.setBalloonId(999L);
//                testDto.setTeam("测试: 调试队伍");
//                testDto.setLocation("测试位置-001");
//                testDto.setDone(false);
//                testDto.setTime(String.valueOf(System.currentTimeMillis() / 1000.0));
//
//                DjBalloonDTO.ContestProblem problem = new DjBalloonDTO.ContestProblem();
//                problem.setShortName("A");
//                problem.setColor("红色");
//                testDto.setContestProblem(problem);
//
//                NEW_BALLOONS.add(testDto);
//                log.info("【调试】添加测试气球到推送队列");
//            } catch (Exception e) {
//                log.error("测试气球构造失败", e);
//            }
//        }
//        // ==============================================================================

        if (NEW_BALLOONS.isEmpty()) {
            sendHeartbeat();
            return;
        }

        List<DjBalloonDTO> tempList = new ArrayList<>(NEW_BALLOONS);
        NEW_BALLOONS.clear();

        for (DjBalloonDTO dto : tempList) {
            try {
                Balloon balloon = DTOToBalloon(dto);
                broadcastMessage("balloon", Result.success(balloon));
                log.info("推送气球任务成功，ID：{}", balloon.getBalloonId());
            } catch (Exception e) {
                log.error("单条气球推送失败，跳过", e);
            }
        }
    }

    // ===================== 广播消息工具 =====================
    private void broadcastMessage(String eventName, Object data) {
        Iterator<SseEmitter> iterator = CLIENTS.iterator();
        while (iterator.hasNext()) {
            SseEmitter emitter = iterator.next();
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (Exception e) {
                iterator.remove();
                log.error("客户端消息推送失败，已移除");
            }
        }
    }

    // ===================== 发送心跳 =====================
    private void sendHeartbeat() {
        broadcastMessage("heartbeat", "ping");
    }

    // ===================== 定时从Domjudge拉取气球 =====================
    @Scheduled(fixedRate = 1000)
    public void getBalloonFromDomjudge() {
        fetchBalloon(true);
    }

    // ===================== 分页查询所有气球 =====================
    @Override
    public IPage<Balloon> getAllBalloonFromDomjudge(int cur) {
        List<DjBalloonDTO> dtoList = fetchBalloon(false);
        List<Balloon> balloonList = Objects.nonNull(dtoList) ?
                dtoList.stream().map(this::DTOToBalloon).toList() : Collections.emptyList();

        Page<Balloon> page = new Page<>(cur, 10);
        page.setRecords(balloonList);
        page.setTotal(balloonList.size());
        return page;
    }

    // ===================== 拉取气球核心方法（修复空值+日志） =====================
    private List<DjBalloonDTO> fetchBalloon(boolean isTodo) {
        try {
            String auth = domjudgeProperties.getAuth();
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            RequestEntity<Void> request = RequestEntity
                    .get(domjudgeProperties.getDomjudgeBalloonApiUrl(isTodo))
                    .header(HttpHeaders.AUTHORIZATION, basicAuth)
                    .accept(MediaType.APPLICATION_JSON)
                    .build();

            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            List<DjBalloonDTO> dtoList = objectMapper.readValue(response.getBody(), new TypeReference<>() {});

            log.info("拉取Domjudge气球{}数据：{}条", isTodo ? "待处理" : "全部", dtoList == null ? 0 : dtoList.size());

            // 仅待处理数据加入推送队列
            if (isTodo && dtoList != null && !dtoList.isEmpty()) {
                NEW_BALLOONS.addAll(dtoList);
            }

            return dtoList;
        } catch (Exception e) {
            log.error(MessageConstant.GET_BALLOON_ERROR, e);
            return Collections.emptyList();
        }
    }

    // ===================== DTO转换实体（修复空指针！核心） =====================
    private Balloon DTOToBalloon(DjBalloonDTO balloonDto) {
        try {
            boolean isFirst = !FIRST_SOLVE.contains(balloonDto.getContestProblem().getShortName());
            if (isFirst) {
                FIRST_SOLVE.add(balloonDto.getContestProblem().getShortName());
            }

            // 安全拆分队伍名称，修复空指针崩溃
            String teamName = balloonDto.getTeam();
            if (teamName != null && teamName.contains(": ")) {
                teamName = teamName.split(": ")[1];
            }

            Balloon balloon = new Balloon();
            balloon.setBalloonId(balloonDto.getBalloonId())
                    .setProblem(balloonDto.getContestProblem().getShortName())
                    .setTeamName(teamName)
                    .setTeamLocation(balloonDto.getLocation())
                    .setColorName(balloonDto.getContestProblem().getColor())
                    .setTime(TimeUtil.timestampToLocalDateTime(Double.parseDouble(balloonDto.getTime())))
                    // UTODO 调试代码翻转了状态
                    .setIsFinished(balloonDto.getDone()) // 状态反转（调试用）
                    .setIsFirst(isFirst);

            return balloon;
        } catch (Exception e) {
            log.error("气球DTO转换失败，ID:{}", balloonDto.getBalloonId(), e);
            return new Balloon();
        }
    }

    // ===================== 标记气球已发放 =====================
    @Override
    public void setBalloonDone(Long id) {
        try {
            String auth = domjudgeProperties.getAuth();
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            String url = domjudgeProperties.getDomjudgeBalloonApiUrl() + "/" + id + "/done";

            RequestEntity<Void> request = RequestEntity
                    .post(url)
                    .header(HttpHeaders.AUTHORIZATION, basicAuth)
                    .build();

            restTemplate.exchange(request, String.class);
            log.info("标记气球完成成功，ID：{}", id);
        } catch (Exception e) {
            log.error("标记气球失败 ID:{}", id, e);
        }
    }
}