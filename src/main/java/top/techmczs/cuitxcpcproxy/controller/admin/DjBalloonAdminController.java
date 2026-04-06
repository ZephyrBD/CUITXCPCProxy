package top.techmczs.cuitxcpcproxy.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.techmczs.cuitxcpcproxy.entity.Balloon;
import top.techmczs.cuitxcpcproxy.result.Result;
import top.techmczs.cuitxcpcproxy.services.DjBalloonService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/balloon")
public class DjBalloonAdminController {

    private final DjBalloonService djBalloonService;

    /**
     * 气球SSE统一推送接口 ✅ 修复：添加charset、移除多余线程池
     */
    @GetMapping(value = "/task", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public SseEmitter getBalloon() {
        // 创建永不超时的Emitter
        SseEmitter emitter = new SseEmitter(0L);
        // 交给Service管理客户端
        djBalloonService.registerClient(emitter);
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