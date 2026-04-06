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

@Service
@Slf4j
@RequiredArgsConstructor
public class DjBalloonServiceImpl implements DjBalloonService {

    private final DomjudgeProperties domjudgeProperties;

    @Resource
    private RestTemplate restTemplate;
    @Resource
    private ObjectMapper objectMapper;

    // SSE 推送器
    private SseEmitter sseEmitter;
    // 🔥 去重：已推送气球ID
    private final Set<Long> PUSHED_BALLOON_IDS = Collections.synchronizedSet(new HashSet<>());
    // 待推送队列
    private final List<DjBalloonDTO> NEW_BALLOONS = new CopyOnWriteArrayList<>();
    // 首解标记
    private final Set<String> FIRST_SOLVE = Collections.synchronizedSet(new HashSet<>());

//    // ===================== SSE 前端连接 =====================
//    @Override
//    public SseEmitter connectSse() {
//        sseEmitter = new SseEmitter(0L);
//        // 500ms 推送一次
//        new Timer().scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                try {
//                    if (!NEW_BALLOONS.isEmpty() && sseEmitter != null) {
//                        for (DjBalloonDTO balloonDto : NEW_BALLOONS) {
//                            // 推送给前端
//                            sseEmitter.send(DTOToBalloon(balloonDto));
//                        }
//                        NEW_BALLOONS.clear();
//                    }
//                } catch (Exception e) {
//                    sseEmitter = null;
//                    this.cancel();
//                    log.error("SSE推送失败", e);
//                }
//            }
//        }, 0, 500);
//        return sseEmitter;
//    }

    @Override
    public void connectBalloonSse(SseEmitter emitter) {
        try {
            // 统一循环模式（替代Timer，更稳定）
            while (!Thread.currentThread().isInterrupted()) {
                boolean hasData = false;

                // ===================== 原有业务逻辑（完全保留）=====================
                if (!NEW_BALLOONS.isEmpty()) {
                    hasData = true;
                    for (DjBalloonDTO balloonDto : NEW_BALLOONS) {
                        // 统一包装 Result 格式（和你所有接口一致）
                        Object resultData = DTOToBalloon(balloonDto);
                        emitter.send(SseEmitter.event().data(Result.success(resultData)));
                    }
                    NEW_BALLOONS.clear();
                }
                // =================================================================

                // 无数据 → 发送心跳保活（统一规范）
                if (!hasData) {
                    emitter.send(SseEmitter.event().comment("balloon-heartbeat"));
                }

                // 固定500ms推送一次（和原有Timer间隔一致）
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("气球SSE推送线程中断");
        } catch (IOException e) {
            log.error("气球SSE推送失败：客户端断开连接", e);
        } catch (Exception e) {
            log.error("气球SSE未知异常", e);
        } finally {
            // 统一关闭连接
            emitter.complete();
        }
    }

    // ===================== 定时拉取：仅未打印气球（/print 用） =====================
    @Scheduled(fixedRate = 1000)
    public void getBalloonFromDomjudge() {
        fetchBalloon(true);
    }

    // ===================== 拉取全量气球（/print/all 用） =====================
    @Override
    public IPage<Balloon> getAllBalloonFromDomjudge(int cur) {
        // 1. 拉取全量DTO（不去重）
        List<DjBalloonDTO> dtoList = fetchBalloon(false);

        // 2. 修复Stream：DTO转实体（全量不去重）
        List<Balloon> balloonList = null;
        if (dtoList != null) {
            balloonList = dtoList.stream()
                    .map(this::DTOToBalloon)
                    .toList();
        }

        // 3. 分页封装（每页10条）
        Page<Balloon> page = new Page<>(cur, 10);
        page.setRecords(balloonList);
        page.setTotal(balloonList.size());

        return page;
    }

    // ===================== 通用拉取方法（去重核心） =====================
    private List<DjBalloonDTO> fetchBalloon(boolean isTodo) {
        try {
            // 认证
            String auth = domjudgeProperties.getAuth();
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            // 构建请求
            RequestEntity<Void> request = RequestEntity
                    .get(domjudgeProperties.getDomjudgeBalloonApiUrl(isTodo))
                    .header(HttpHeaders.AUTHORIZATION, basicAuth)
                    .accept(MediaType.APPLICATION_JSON)
                    .build();

            // 获取数据
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            List<DjBalloonDTO> dtoList = objectMapper.readValue(response.getBody(), new TypeReference<>() {});

            if(isTodo){
                addTodoBalloon(dtoList);
            } else {
                return dtoList;
            }

        } catch (Exception e) {
            log.error(MessageConstant.GET_BALLOON_ERROR, e);
        }
        return null;
    }

    private Balloon DTOToBalloon(DjBalloonDTO balloonDto) {
        boolean isFirst = !FIRST_SOLVE.contains(balloonDto.getContestProblem().getShortName());
        if (isFirst) FIRST_SOLVE.add(balloonDto.getContestProblem().getShortName());

        // 封装实体
        Balloon balloon = new Balloon();
        balloon.setBalloonId(balloonDto.getBalloonId())
                .setProblem(balloonDto.getContestProblem().getShortName())
                .setTeamName(balloonDto.getTeam().split(": ")[1])
                .setTeamLocation(balloonDto.getLocation())
                .setColorName(balloonDto.getContestProblem().getColor())
                .setTime(TimeUtil.timestampToLocalDateTime(Double.parseDouble(balloonDto.getTime())))
                .setIsFinished(balloonDto.getDone())
                .setIsFirst(isFirst);

        return balloon;
    }

    private void addTodoBalloon(List<DjBalloonDTO> dtoList){
        for (DjBalloonDTO balloon : dtoList) {
            Long balloonId = balloon.getBalloonId();
            if (!PUSHED_BALLOON_IDS.contains(balloonId)) {
                PUSHED_BALLOON_IDS.add(balloonId);
                NEW_BALLOONS.add(balloon);
            }
        }
    }
    // ===================== 标记气球已打印（实现你调用的方法） =====================
    @Override
    public void setBalloonDone(Long id) {
        try {
            String auth = domjudgeProperties.getAuth();
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            // 调用 DOMjudge API 标记气球已完成
            String url = domjudgeProperties.getDomjudgeBalloonApiUrl() + "/" + id + "/done";
            RequestEntity<Void> request = RequestEntity
                    .post(url)
                    .header(HttpHeaders.AUTHORIZATION, basicAuth)
                    .build();

            restTemplate.exchange(request, String.class);
        } catch (Exception e) {
            log.error("标记气球失败 ID:{}", id, e);
        }
    }
}