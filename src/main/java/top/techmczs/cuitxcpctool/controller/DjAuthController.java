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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.techmczs.cuitxcpctool.common.QueueTaskStatus;
import top.techmczs.cuitxcpctool.constant.ResponseMessageConstant;
import top.techmczs.cuitxcpctool.dto.AdminDTO;
import top.techmczs.cuitxcpctool.dto.AuthTaskDTO;
import top.techmczs.cuitxcpctool.dto.DjTeamDTO;
import top.techmczs.cuitxcpctool.result.Result;
import top.techmczs.cuitxcpctool.services.DjAuthService;

import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth",description = "用于客户端认证的接口")
public class DjAuthController {
    private final DjAuthService djAuthService;

    @GetMapping("/admin/auth/task/page")
    @Operation(description = "分页查询所有登录请求")
    public Result<IPage<AuthTaskDTO>> getAllTeamLoginTask(@Parameter(description = "当前查询的页码") @RequestParam(value = "cur_page") int curPage){
        return Result.success(djAuthService.queryAuthTasksByPage(curPage));
    }

    @PostMapping("/admin/auth/task/accept")
    @Operation(description = "接受某一个登录请求")
    public Result<String> acceptLoginTask(@Parameter(description = "需要接受的请求的Task ID") @RequestParam(value = "task_id") Long taskId){
        djAuthService.acceptAuth(taskId);
        return Result.success(ResponseMessageConstant.SUCCESS);
    }

    @PostMapping("/admin/auth/task/deny")
    @Operation(description = "拒绝一个登录请求")
    public Result<String> denyLoginTask(@Parameter(description = "需要拒绝的请求的Task ID") @RequestParam(value = "task_id") Long taskId){
        djAuthService.denyAuth(taskId);
        return Result.success(ResponseMessageConstant.SUCCESS);
    }

    @GetMapping("/admin/auth/domjudge")
    @Operation(description = "返回跳转到Domjudge的含url、token组装的对象")
    public Result<AdminDTO> toDomjudge() {
        return Result.success(djAuthService.getAdminToDomjudgeToken());
    }

    /**
     * 请求校验UserAgent，校验成功后会返回一个token用于重定向到Nginx的验证模块进行转发到后端的业务
     * 发送给Nginx的token以这样的形式给出 {org.domjudge.host}:{org.domjudge.port}{org.domjudge.nginx-verify-route-path}?token={token}
     */
    @PostMapping("/public/auth/verify")
    @Operation(description = "请求验证客户端")
    public Result<DjTeamDTO> verifyClient(@Parameter(description = "考号") @RequestParam(value = "exam_num")String examNum, HttpServletRequest request) {
        DjTeamDTO djTeamDTO = djAuthService.verifyClientAndGetToken(examNum, getClientId(request), request.getHeader("User-Agent"));
        return Result.success(djTeamDTO);
    }

    @GetMapping("/public/auth/verify")
    @Operation(description = "返回验证客户端的请求状态")
    public Result<Object> getVerifyStatus(@Parameter(description = "考号") @RequestParam(value = "exam_num") String examNum, HttpServletRequest request) {
        QueueTaskStatus status = djAuthService.getAuthStatus(examNum, getClientId(request));
        // 状态为DONE（同意）时返回DjTeamDTO，否则返回状态
        if (QueueTaskStatus.DONE.equals(status)) {
            DjTeamDTO djTeamDTO = djAuthService.getApprovedTeamInfo(examNum);
            return Result.success(djTeamDTO);
        } else {
            return Result.success(status);
        }
    }
    /**
     * Nginx请求校验token，格式为Nginx会请求本程序以类似后面的形式验证token: localhost:{port}/validate 其会在头文件中含有
     * X-Original-URI 头（{org.domjudge.nginx-verify-route-path}?token={token}），会以 = 分离提取{token}验证。
     */
    @GetMapping("/public/auth/validate")
    @Operation(description = "用于nginx请求验证token是否有效")
    public Result<Object> validate(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getHeader("X-Original-URI").split("=")[1];
        //System.out.println(token);
        if (djAuthService.verifyToken(token)) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return Result.success();
    }

    /**
     * 用于获取OMSClient 1.X的clientId
     * @param request Http请求
     * @return 请求中Cookie含有的ClientID
     */
    private static String getClientId(HttpServletRequest request) {
        return Arrays.stream(request.getCookies() == null ? new Cookie[0] : request.getCookies())
                .filter(cookie -> "clientId".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
