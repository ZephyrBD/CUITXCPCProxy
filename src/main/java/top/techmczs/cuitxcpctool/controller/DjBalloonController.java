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
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.techmczs.cuitxcpctool.constant.ResponseMessageConstant;
import top.techmczs.cuitxcpctool.entity.Balloon;
import top.techmczs.cuitxcpctool.result.Result;
import top.techmczs.cuitxcpctool.services.DjBalloonService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/balloon")
@Tag(name = "Balloon",description = "返回包装后的Domjudge气球信息")
public class DjBalloonController {

    private final DjBalloonService djBalloonService;

    @GetMapping("/task/page")
    @Operation(description = "分页查询所有气球小票任务")
    public Result<IPage<Balloon>> getAllPrintTask(@Parameter(description = "当前查询的页码") @RequestParam(value = "cur_page") int curPage){
        return Result.success(djBalloonService.getAllBalloonFromDomjudge(curPage));
    }

    @PostMapping("/task/{id}/done")
    @Operation(description = "标记某个气球已经发放")
    public Result<String> doneBalloon(@Parameter(description = "气球任务ID") @PathVariable Long id) {
        djBalloonService.setBalloonDone(id);
        return Result.success(ResponseMessageConstant.SUCCESS);
    }
}