package top.techmczs.cuitxcpctool.services.impl;

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
import top.techmczs.cuitxcpctool.constant.MessageConstant;
import top.techmczs.cuitxcpctool.constant.SseEventConstant;
import top.techmczs.cuitxcpctool.entity.Balloon;
import top.techmczs.cuitxcpctool.entity.DjBalloon;
import top.techmczs.cuitxcpctool.properties.DomjudgeProperties;
import top.techmczs.cuitxcpctool.result.Result;
import top.techmczs.cuitxcpctool.services.DjBalloonService;
import top.techmczs.cuitxcpctool.services.SseManagerService;
import top.techmczs.cuitxcpctool.utils.TimeUtil;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class DjBalloonServiceImpl implements DjBalloonService {

    // 注入全局SSE管理器
    @Resource
    private SseManagerService sseManagerService;

    private final DomjudgeProperties domjudgeProperties;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private ObjectMapper objectMapper;

    // 待推送气球队列
    private final List<DjBalloon> NEW_BALLOONS = new CopyOnWriteArrayList<>();
    private final Map<String, Long> FIRST_SOLVE = Collections.synchronizedMap(new HashMap<>());

    @Scheduled(fixedRate = 500)
    public void pushBalloonToClients() {
        if (NEW_BALLOONS.isEmpty()) {
            sseManagerService.sendHeartbeat();
            return;
        }

        List<DjBalloon> tempList = new ArrayList<>(NEW_BALLOONS);
        NEW_BALLOONS.clear();

        for (DjBalloon dto : tempList) {
            try {
                Balloon balloon = DjBallonToBalloon(dto);
                // 走全局SSE广播
                sseManagerService.broadcast(SseEventConstant.BALLOON_TASK, Result.success(balloon));
                log.info(MessageConstant.PUSH_BALLOON_TASK_SUCCESS, balloon.getBalloonId());
            } catch (Exception e) {
                log.error(MessageConstant.SKIP_BALLOON_TASK);
            }
        }
    }

    @Scheduled(fixedRate = 1000)
    public void getBalloonFromDomjudge() {
        fetchBalloon(true);
    }

    @Override
    public IPage<Balloon> getAllBalloonFromDomjudge(int cur) {
        List<DjBalloon> dtoList = fetchBalloon(false);
        List<Balloon> balloonList = Objects.nonNull(dtoList) ?
                dtoList.stream().map(this::DjBallonToBalloon).toList() : Collections.emptyList();

        Page<Balloon> page = new Page<>(cur, 10);
        page.setRecords(balloonList);
        page.setTotal(balloonList.size());
        return page;
    }

    private List<DjBalloon> fetchBalloon(boolean isTodo) {
        try {
            String auth = domjudgeProperties.getAuth();
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            RequestEntity<Void> request = RequestEntity
                    .get(domjudgeProperties.getDomjudgeBalloonApiUrl(isTodo))
                    .header(HttpHeaders.AUTHORIZATION, basicAuth)
                    .accept(MediaType.APPLICATION_JSON)
                    .build();

            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            List<DjBalloon> dtoList = objectMapper.readValue(response.getBody(), new TypeReference<>() {});

            log.info(MessageConstant.GET_BALLOON_TASK_SUCCESS,
                    isTodo ? MessageConstant.TASK_TO_DO : MessageConstant.TASK_ALL,
                    dtoList == null ? 0 : dtoList.size());

            if (isTodo && dtoList != null && !dtoList.isEmpty()) {
                NEW_BALLOONS.addAll(dtoList);
            }

            return dtoList;
        } catch (Exception e) {
            log.error(MessageConstant.GET_BALLOON_ERROR,e.getMessage());
            return Collections.emptyList();
        }
    }

    private Balloon DjBallonToBalloon(DjBalloon balloonDto) {
        try {
            boolean isFirst =
                    !FIRST_SOLVE.containsKey(balloonDto.getContestProblem().getShortName())
                    || FIRST_SOLVE.get(balloonDto.getContestProblem().getShortName()).equals(balloonDto.getBalloonId());

            if (FIRST_SOLVE.containsKey(balloonDto.getContestProblem().getShortName())) {
                FIRST_SOLVE.put(balloonDto.getContestProblem().getShortName(), balloonDto.getBalloonId());
            }

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
                    .setIsFinished(balloonDto.getDone())
                    .setIsFirst(isFirst);

            return balloon;
        } catch (Exception e) {
            return new Balloon();
        }
    }

    @Override
    public void setBalloonDone(Long id) {
        try {
            String auth = domjudgeProperties.getAuth();
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            String url = domjudgeProperties.getDomjudgeBalloonApiUrl(id);

            RequestEntity<Void> request = RequestEntity
                    .post(url)
                    .header(HttpHeaders.AUTHORIZATION, basicAuth)
                    .build();
            restTemplate.exchange(request, String.class);
        } catch (Exception e) {
            log.error(MessageConstant.SET_BALLOON_TASK_DONE_FAILED, id);
        }
    }
}