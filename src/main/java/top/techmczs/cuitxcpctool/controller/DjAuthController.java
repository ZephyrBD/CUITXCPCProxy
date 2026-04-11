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

//Host：localhost:8080
//Connection：keep-alive
//Content-Length：39
//sec-ch-ua："Chromium";v="109"
//sec-ch-ua-platform："Windows"
//sec-ch-ua-mobile：?0
//User-Agent：Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.5414.87 OMSClient/1.1.35 Safari/537.36
//Content-Type：text/plain
//Accept：*/*
//Origin：http://localhost
//Cookie：clientId=ea6f24c9-b046-4cb6-8394-5db58dcea4a0
//Sec-Fetch-Site：same-site
//Sec-Fetch-Mode：cors
//Sec-Fetch-Dest：empty
//Referer：http://localhost/
//Accept-Encoding：gzip, deflate, br
//Accept-Language：zh-CN,zh;q=0.9
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth")
public class DjAuthController {
    private final DjAuthService djAuthService;

    @GetMapping("/admin/auth/task/page")
    public Result<IPage<AuthTaskDTO>> getAllTeamLoginTask(@RequestParam(value = "cur_page") int curPage){
        return Result.success(djAuthService.queryAuthTasksByPage(curPage));
    }

    @PostMapping("/admin/auth/task/accept")
    public Result<String> acceptLoginTask(@RequestParam(value = "task_id") Long taskId){
        djAuthService.acceptAuth(taskId);
        return Result.success(ResponseMessageConstant.SUCCESS);
    }

    @PostMapping("/admin/auth/task/deny")
    public Result<String> denyLoginTask(@RequestParam(value = "task_id") Long taskId){
        djAuthService.denyAuth(taskId);
        return Result.success(ResponseMessageConstant.SUCCESS);
    }

//    @DeleteMapping("/admin/auth/task/all")
//    public Result<String> clearLoginTask(){
//        djAuthService.clearAuthTaskQueue();
//        return Result.success(ResponseMessageConstant.SUCCESS);
//    }

//    @DeleteMapping("/admin/auth/teamclient/all")
//    public Result<Object> taskTeamClient(){
//        djAuthService.clearTeamClient();
//        return Result.success();
//    }

    @GetMapping("/admin/auth/domjudge")
    public Result<AdminDTO> toDomjudge() {
        return Result.success(djAuthService.getAdminToDomjudgeToken());
    }

    /**
     * 请求校验UserAgent，校验成功后会返回一个token用于重定向到Nginx的验证模块进行转发到后端的业务
     * 发送给Nginx的token以这样的形式给出 {org.domjudge.host}:{org.domjudge.port}{org.domjudge.nginx-verify-route-path}?token={token}
     */
    @PostMapping("/public/auth/verify")
    public Result<DjTeamDTO> verifyClient(@RequestParam(value = "exam_num")String examNum, HttpServletRequest request) {
        DjTeamDTO djTeamDTO = djAuthService.verifyClientAndGetToken(examNum, getClientId(request), request.getHeader("User-Agent"));
        return Result.success(djTeamDTO);
    }

    @GetMapping("/public/auth/verify")
    public Result<Object> getVerifyStatus(@RequestParam(value = "exam_num") String examNum, HttpServletRequest request) {
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

    private static String getClientId(HttpServletRequest request) {
        return Arrays.stream(request.getCookies() == null ? new Cookie[0] : request.getCookies())
                .filter(cookie -> "clientId".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
