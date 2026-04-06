package top.techmczs.cuitxcpcproxy.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.techmczs.cuitxcpcproxy.dto.PrintTaskDTO;
import top.techmczs.cuitxcpcproxy.dto.PrintTeamDTO;
import top.techmczs.cuitxcpcproxy.result.Result;
import top.techmczs.cuitxcpcproxy.services.DjPrintService;

import java.util.concurrent.Executor;

//#!/bin/bash
//# 参数顺序：位置 队伍名 文件
//LOCATION="$1"
//TEAMNAME="$2"
//FILE="$3"
//
//# ================= 配置项 =================
//PRINT_API="http://192.168.31.136:8080/cxtool/admin/print/task"
//API_TOKEN="3486dsay89x6786f87aerfbxncmbmghjf"
//PDF_DIR="/tmp/print"
//# ==========================================
//
//LOCATION=$(echo "$LOCATION" | tr -d "'\"" | xargs)
//TEAMNAME=$(echo "$TEAMNAME" | tr -d "'\"" | xargs)
//FILE=$(echo "$FILE" | tr -d "'\"" | xargs)
//
//echo "====================="
//echo "队伍位置：[${LOCATION}]"
//echo "队伍名称：[${TEAMNAME}]"
//echo "源文件：[${FILE}]"
//echo "====================="
//
//if [ -z "$TEAMNAME" ] || [ -z "$LOCATION" ]; then
//    echo "❌ 错误：队伍信息不能为空"
//    exit 1
//fi
//
//mkdir -p "$PDF_DIR"
//PDF_FILE="${PDF_DIR}/${TEAMNAME}_$(date +%Y%m%d%H%M%S).pdf"
//
//enscript -b "Delivery location: ${LOCATION},Print Time: $(date +%Y%m%d%H%M%S)" -f Courier10 -A4 "$FILE" -p - | ps2pdf - "$PDF_FILE"
//
//# PDF生成校验
//if [ ! -f "$PDF_FILE" ]; then
//    echo "❌ 错误：PDF生成失败！"
//    exit 1
//fi
//
//# 发送打印请求
//curl -X POST "$PRINT_API" \
//-H "token: $API_TOKEN" \
//-F "file=@${PDF_FILE}" \
//-F "printTeamDTO={\"teamName\":\"${TEAMNAME}\",\"teamPosition\":\"${LOCATION}\"};type=application/json"
//
//echo -e "\n✅ 打印任务提交成功"
//# ================= 新增：自动删除临时PDF文件 =================
//rm -f "$PDF_FILE"  # -f 强制删除，文件不存在也不会报错
//echo "🗑️ 临时PDF文件已清理：${PDF_FILE}"
//# ==============================================================
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/print")
public class DjPrintAdminController {
    private final DjPrintService djPrintService;

    @Resource(name = "taskExecutor")
    private Executor taskExecutor;

    @PostMapping("/task")
    public Result<Object> addPrintTask(@RequestPart("file") MultipartFile file, @RequestPart("printTeamDTO") PrintTeamDTO printTeamDTO) {
        djPrintService.addPrintTask(file, printTeamDTO);
        return Result.success();
    }

//    @GetMapping("/task")
//    public Result<PrintTaskDTO> getPrintTask() {
//        return Result.success(djPrintService.getAuthTopTaskFromQueue());
//    }

    @GetMapping(value = "/task", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getPrintTaskSse() {
        // 1. 创建SSE发射器，0=永不超时（长连接）
        SseEmitter emitter = new SseEmitter(0L);

        // 2. 异步执行推送逻辑（避免阻塞Controller主线程）
        taskExecutor.execute(() -> djPrintService.pushPrintTaskBySse(emitter));

        // 3. SSE连接生命周期回调
        emitter.onCompletion(() -> System.out.println("SSE连接正常关闭"));
        emitter.onTimeout(() -> {
            System.err.println("SSE连接超时");
            emitter.complete();
        });
        emitter.onError(ex -> {
            System.err.println("SSE连接异常：" + ex.getMessage());
            emitter.completeWithError(ex);
        });

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
