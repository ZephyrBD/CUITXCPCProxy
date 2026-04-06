package top.techmczs.cuitxcpcproxy.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.techmczs.cuitxcpcproxy.dto.PrintTaskDTO;
import top.techmczs.cuitxcpcproxy.dto.PrintTeamDTO;
import top.techmczs.cuitxcpcproxy.result.Result;
import top.techmczs.cuitxcpcproxy.services.DjPrintService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/print")
public class DjPrintAdminController {
    private final DjPrintService djPrintService;

    @PostMapping("/task")
    public Result<Object> addPrintTask(@RequestPart("file") MultipartFile file, @RequestPart("printTeamDTO") PrintTeamDTO printTeamDTO) {
        djPrintService.addPrintTask(file, printTeamDTO);
        return Result.success();
    }

    /**
     * 打印任务SSE ✅ 修复：添加charset、移除线程池死循环
     */
    @GetMapping(value = "/task", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public SseEmitter getPrintTaskSse() {
        SseEmitter emitter = new SseEmitter(0L);
        // 注册客户端，统一管理
        djPrintService.registerPrintClient(emitter);
        return emitter;
    }

    @PostMapping("/task/done")
    public Result<String> successPrint(@RequestParam(value = "task_id") Long taskId) {
        djPrintService.setPrintTaskDone(taskId);
        return Result.success();
    }

    @DeleteMapping("/task/all")
    public Result<String> deletePrintTask() {
        djPrintService.clearAll();
        return Result.success();
    }

    @GetMapping("task/{task_id}/download")
    public byte[] getPdfFile(@PathVariable("task_id") Long taskId){
        return djPrintService.getPdfFileByTaskId(taskId);
    }

    @GetMapping("/task/page")
    public Result<IPage<PrintTaskDTO>> getAllPrintTask(@RequestParam(value = "cur_page") int curPage){
        return Result.success(djPrintService.queryAuthTasksByPage(curPage));
    }
}