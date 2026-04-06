package top.techmczs.cuitxcpcproxy.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.techmczs.cuitxcpcproxy.dto.DjTeamDTO;
import top.techmczs.cuitxcpcproxy.entity.queuetask.QueueTask;
import top.techmczs.cuitxcpcproxy.result.Result;
import top.techmczs.cuitxcpcproxy.services.DjAuthService;

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
@RequestMapping("/public/auth")
public class DjAuthController {
    private final DjAuthService djAuthService;
    /**
     * 请求校验UserAgent，校验成功后会返回一个token用于重定向到Nginx的验证模块进行转发到后端的业务
     * 发送给Nginx的token以这样的形式给出 {org.domjudge.host}:{org.domjudge.port}{org.domjudge.nginx-verify-route-path}?token={token}
     */
    @PostMapping("/verify")
    public Result<DjTeamDTO> verifyClient(@RequestParam(value = "exam_num")String examNum, HttpServletRequest request) {
        // 移除UnTODO，补充日志
        log.info("开始验证客户端：examNum={}", examNum);
        //空值安全获取Cookie
        String clientId = Arrays.stream(request.getCookies() == null ? new Cookie[0] : request.getCookies())
                .filter(cookie -> "clientId".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        //System.out.println(clientId);
        //System.out.println(request.getHeader("User-Agent"));
        DjTeamDTO djTeamDTO = djAuthService.verifyClientAndGetToken(examNum, clientId, request.getHeader("User-Agent"));
        return Result.success(djTeamDTO);
    }

    @GetMapping("/verify")
    public Result<Object> getVerifyStatus(@RequestParam(value = "exam_num") String examNum) {
        QueueTask.Status status = djAuthService.getAuthStatus(examNum);
        log.info("前端轮询审核状态，examNum={}, 当前状态={}", examNum, status);
        // 状态为DONE（同意）时返回DjTeamDTO，否则返回状态
        if (QueueTask.Status.DONE.equals(status)) {
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
    @GetMapping("/validate")
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
}
