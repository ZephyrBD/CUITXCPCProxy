package top.techmczs.cuitxcpctool.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.techmczs.cuitxcpctool.constant.ResponseMessageConstant;
import top.techmczs.cuitxcpctool.dto.PrintTaskDTO;
import top.techmczs.cuitxcpctool.dto.PrintTeamDTO;
import top.techmczs.cuitxcpctool.result.Result;
import top.techmczs.cuitxcpctool.services.DjPrintService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/print")
public class DjPrintController {

    private final DjPrintService djPrintService;

    @PostMapping("/task")
    public Result<String> addPrintTask(@RequestPart("file") MultipartFile file, @RequestPart("printTeamDTO") PrintTeamDTO printTeamDTO) {
        djPrintService.addPrintTask(file, printTeamDTO);
        return Result.success(ResponseMessageConstant.SUCCESS);
    }

    @PostMapping("/task/done")
    public Result<String> successPrint(@RequestParam(value = "task_id") Long taskId) {
        djPrintService.setPrintTaskDone(taskId);
        return Result.success(ResponseMessageConstant.SUCCESS);
    }

//    @DeleteMapping("/task/all")
//    public Result<String> deletePrintTask() {
//        djPrintService.clearAll();
//        return Result.success(ResponseMessageConstant.SUCCESS);
//    }

    @GetMapping("task/{task_id}/download")
    public byte[] getPdfFile(@PathVariable("task_id") Long taskId){
        return djPrintService.getPdfFileByTaskId(taskId);
    }

    @GetMapping("/task/page")
    public Result<IPage<PrintTaskDTO>> getAllPrintTask(@RequestParam(value = "cur_page") int curPage){
        return Result.success(djPrintService.queryAuthTasksByPage(curPage));
    }
}