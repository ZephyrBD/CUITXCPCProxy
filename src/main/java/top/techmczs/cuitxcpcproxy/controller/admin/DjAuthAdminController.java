package top.techmczs.cuitxcpcproxy.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.techmczs.cuitxcpcproxy.dto.AdminDTO;
import top.techmczs.cuitxcpcproxy.dto.AuthTaskDTO;
import top.techmczs.cuitxcpcproxy.result.Result;
import top.techmczs.cuitxcpcproxy.services.DjAuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/auth")
public class DjAuthAdminController {

    private final DjAuthService djAuthService;

    /**
     * 授权任务SSE ✅ 修复：添加charset、移除线程池死循环
     */
    @GetMapping(value = "/task", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public SseEmitter getTeamLoginTaskSse() {
        SseEmitter emitter = new SseEmitter(0L);
        // 注册客户端（统一管理生命周期）
        djAuthService.registerAuthClient(emitter);
        return emitter;
    }

    @GetMapping("/task/page")
    public Result<IPage<AuthTaskDTO>> getAllTeamLoginTask(@RequestParam(value = "cur_page") int curPage){
        return Result.success(djAuthService.queryAuthTasksByPage(curPage));
    }

    @PostMapping("/task/accept")
    public Result<Object> acceptLoginTask(@RequestParam(value = "task_id") Long taskId){
        djAuthService.acceptAuth(taskId);
        return Result.success();
    }

    @PostMapping("/task/deny")
    public Result<Object> denyLoginTask(@RequestParam(value = "task_id") Long taskId){
        djAuthService.denyAuth(taskId);
        return Result.success();
    }

    @DeleteMapping("/task/all")
    public Result<Object> clearLoginTask(){
        djAuthService.clearAuthTaskQueue();
        return Result.success();
    }

    @DeleteMapping("/teamclient/all")
    public Result<Object> taskTeamClient(){
        djAuthService.clearTeamClient();
        return Result.success();
    }

    @GetMapping("/domjudge")
    public Result<AdminDTO> toDomjudge() {
        return Result.success(djAuthService.getAdminToDomjudgeToken());
    }
}