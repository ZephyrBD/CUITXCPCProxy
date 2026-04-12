/*
 * Copyright (C) 2018-2026 Modding Craft ZBD Studio.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package top.techmczs.cuitxcpctool.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Print",description = "打印任务接口")
public class DjPrintController {

    private final DjPrintService djPrintService;

    @PostMapping("/task")
    @Operation(description = "用于Domjudge申请打印请求，单独验证TOKEN")
    @Parameters({
            @Parameter(name = "file",description = "Domjudge上传的打印PDF",required = true),
            @Parameter(name = "printTeamDTO",description = "包装了基本队伍基本信息的实体",required = true)
    })
    public Result<String> addPrintTask(@RequestPart("file") MultipartFile file, @RequestPart("printTeamDTO") PrintTeamDTO printTeamDTO) {
        djPrintService.addPrintTask(file, printTeamDTO);
        return Result.success(ResponseMessageConstant.SUCCESS);
    }

    @PostMapping("/task/done")
    @Operation(description = "标记某个打印任务已经完成")
    @Parameter(name = "taskId",description = "打印任务ID",required = true)
    public Result<String> successPrint(@RequestParam(value = "task_id") Long taskId) {
        djPrintService.setPrintTaskDone(taskId);
        return Result.success(ResponseMessageConstant.SUCCESS);
    }

    @GetMapping("task/{task_id}/download")
    @Operation(description = "请求某个打印任务对应的PDF文件")
    @Parameter(name = "taskId",description = "打印任务ID",required = true)
    public byte[] getPdfFile(@PathVariable("task_id") Long taskId){
        return djPrintService.getPdfFileByTaskId(taskId);
    }

    @GetMapping("/task/page")
    @Operation(description = "分页查询所有打印任务")
    public Result<IPage<PrintTaskDTO>> getAllPrintTask(@Parameter(description = "当前查询的页码") @RequestParam(value = "cur_page") int curPage){
        return Result.success(djPrintService.queryAuthTasksByPage(curPage));
    }
}