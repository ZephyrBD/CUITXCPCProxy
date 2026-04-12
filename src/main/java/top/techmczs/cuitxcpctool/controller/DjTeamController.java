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
import org.springframework.web.multipart.MultipartFile;
import top.techmczs.cuitxcpctool.constant.MessageConstant;
import top.techmczs.cuitxcpctool.constant.ResponseMessageConstant;
import top.techmczs.cuitxcpctool.entity.DjTeam;
import top.techmczs.cuitxcpctool.result.Result;
import top.techmczs.cuitxcpctool.services.DjTeamService;

import java.util.Objects;

@RestController()
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
@Tag(name = "Team", description = "用于获取队伍信息的接口")
public class DjTeamController {

    private final DjTeamService djTeamService;

    @GetMapping("/team/page")
    @Operation(description = "分页查询所有队伍")
    public Result<IPage<DjTeam>> getAllTeamsByPage(@Parameter(description = "当前查询的页码") @RequestParam(value = "cur_page") int curPage){
        return Result.success(djTeamService.queryTeamsByPage(curPage));
    }

    /**
     * 清空队伍数据库，并导入队伍数据
     * @param file 队伍名单
     * @return 导入状态
     */
    @PostMapping("/team")
    @Operation(description = "上传XLSX解析成队伍")
    public Result<String> uploadTeam(@Parameter(description = "xlsx文件") @RequestPart MultipartFile file){
        try {
            if(!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".xlsx")){
                return Result.error(MessageConstant.MUST_XLSX);
            }
            djTeamService.importTeamExcel(file);
            return Result.success(ResponseMessageConstant.SUCCESS);
        } catch (Exception e){
            return Result.error(ResponseMessageConstant.FAILED);
        }
    }
}
