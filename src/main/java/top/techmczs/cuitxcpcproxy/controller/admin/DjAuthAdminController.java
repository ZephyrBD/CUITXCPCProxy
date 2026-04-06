package top.techmczs.cuitxcpcproxy.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.techmczs.cuitxcpcproxy.dto.AdminDTO;
import top.techmczs.cuitxcpcproxy.dto.AuthTaskDTO;
import top.techmczs.cuitxcpcproxy.dto.DjTeamDTO;
import top.techmczs.cuitxcpcproxy.result.Result;
import top.techmczs.cuitxcpcproxy.services.DjAuthService;

import java.util.Arrays;
import java.util.concurrent.Executor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/auth")
public class DjAuthAdminController {

    private final DjAuthService djAuthService;

    @Resource(name = "taskExecutor")
    private Executor taskExecutor;

//    @GetMapping("/task")
//    public Result<AuthTaskDTO> getTeamLoginTask(){
//        return Result.success(djAuthService.getAuthTopTaskFromQueue());
//    }

    @GetMapping(value = "/task", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getTeamLoginTaskSse() {
        // 0L = 连接永不超时
        SseEmitter emitter = new SseEmitter(0L);

        // 异步执行推送逻辑，不阻塞主线程
        taskExecutor.execute(() -> djAuthService.pushAuthTaskBySse(emitter));

        // 连接生命周期回调
        emitter.onCompletion(() -> System.out.println("授权任务SSE连接正常关闭"));
        emitter.onTimeout(() -> {
            System.err.println("授权任务SSE连接超时");
            emitter.complete();
        });
        emitter.onError(ex -> {
            System.err.println("授权任务SSE连接异常：" + ex.getMessage());
            emitter.completeWithError(ex);
        });

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
