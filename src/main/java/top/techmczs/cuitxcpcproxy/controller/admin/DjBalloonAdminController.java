package top.techmczs.cuitxcpcproxy.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.techmczs.cuitxcpcproxy.dto.PrintTaskDTO;
import top.techmczs.cuitxcpcproxy.entity.Balloon;
import top.techmczs.cuitxcpcproxy.result.Result;
import top.techmczs.cuitxcpcproxy.services.DjBalloonService;

import java.util.concurrent.Executor;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/balloon")
public class DjBalloonAdminController {

    private final DjBalloonService djBalloonService;

    @Resource(name = "taskExecutor")
    private Executor taskExecutor;

//    @GetMapping(value = "/print", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter getBalloon() {
//        return djBalloonService.connectSse();
//    }

    /**
     * 气球SSE统一推送接口
     */
    @GetMapping(value = "/task", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getBalloon() {
        // 1. 创建SSE发射器（0=永不超时）
        SseEmitter emitter = new SseEmitter(0L);

        // 2. 异步执行推送（核心：不阻塞主线程）
        taskExecutor.execute(() -> djBalloonService.connectBalloonSse(emitter));

        // 3. 统一生命周期回调（和授权/打印接口完全一致）
        emitter.onCompletion(() -> log.info("气球SSE连接正常关闭"));
        emitter.onTimeout(() -> {
            log.info("气球SSE连接超时");
            emitter.complete();
        });
        emitter.onError(ex -> {
            log.error("气球SSE连接异常：{}", ex.getMessage());
            emitter.completeWithError(ex);
        });

        return emitter;
    }

    @GetMapping("/task/page")
    public Result<IPage<Balloon>> getAllPrintTask(@RequestParam(value = "cur_page") int curPage){
        return Result.success(djBalloonService.getAllBalloonFromDomjudge(curPage));
    }

    @PostMapping("/task/{id}/done")
    public Result<Object> doneBalloon(@PathVariable Long id) {
        djBalloonService.setBalloonDone(id);
        return Result.success();
    }
}

