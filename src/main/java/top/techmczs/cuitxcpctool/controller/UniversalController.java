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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.techmczs.cuitxcpctool.CuitXcpcToolApplication;
import top.techmczs.cuitxcpctool.constant.MessageConstant;
import top.techmczs.cuitxcpctool.constant.ResponseMessageConstant;
import top.techmczs.cuitxcpctool.properties.JwtProperties;
import top.techmczs.cuitxcpctool.result.Result;
import top.techmczs.cuitxcpctool.services.DjAuthService;
import top.techmczs.cuitxcpctool.services.DjPrintService;
import top.techmczs.cuitxcpctool.services.SseManagerService;
import top.techmczs.cuitxcpctool.utils.JwtUtil;

@RestController
@RequiredArgsConstructor
@Tag(name = "Universal",description = "通用接口")
public class UniversalController {

    private final JwtProperties jwtProperties;
    private final SseManagerService sseManagerService;
    private final DjPrintService djPrintService;
    private final DjAuthService djAuthService;

    @GetMapping("/public/version")
    @Operation(description = "获取版本号")
    public String version() {
        return CuitXcpcToolApplication.CXTOOL_VERSION;
    }

    @GetMapping("/admin/login")
    @Operation(description = "Admin请求登录")
    @Parameters({
            @Parameter(name = "userName",description = "用户名"),
            @Parameter(name = "password",description = "密码")
    })
    public Result<String> login(String userName, String password) {
        if (JwtProperties.ADMIN_ACCOUNT.equals(userName) && jwtProperties.getAdminPassword().equals(password)) {
            // 登录成功
            String token = JwtUtil.createJWT(jwtProperties.getSecretKey(), JwtProperties.ADMIN_TOKEN_TIME, userName);
            return Result.success(token);
        }
        return Result.error(ResponseMessageConstant.FAILED);
    }

    @DeleteMapping("/admin/new/contest")
    @Operation(description = "请求清空除队伍外的所有信息，开始新比赛")
    public Result<String> deleteAll() {
        djPrintService.clearAll();
        djAuthService.clearAuthTaskQueue();
        djAuthService.clearTeamClient();
        return Result.success(ResponseMessageConstant.SUCCESS);
    }

    @GetMapping("/admin/logout")
    @Operation(description = "请求登出")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * SSE连接入口
     */
    @GetMapping(value = "/admin/sse/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(description = "SSE 统一任务队列")
    public SseEmitter connect(@Parameter(description = "身份token") @RequestParam String token) {
        String userName;
        try {
            // 解析 Token
            userName = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
        } catch (Exception e) {
            // Token过期、非法、签名错误、格式错误
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, MessageConstant.ILLEGAL_TOKEN);
        }

        // 解析出来的用户必须不为空
        if (userName == null || userName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, MessageConstant.UNKNOWN_TEAM);
        }

        // 校验用户是Admin
        if (!userName.equals(JwtProperties.ADMIN_ACCOUNT)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,MessageConstant.UNKNOWN_TEAM);
        }

        // 验证通过，注册SSE连接
        return sseManagerService.registerClient(userName);
    }
}