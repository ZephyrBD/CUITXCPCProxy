package top.techmczs.cuitxcpcproxy.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import top.techmczs.cuitxcpcproxy.properties.JwtProperties;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final JwtProperties jwtProperties;

    @GetMapping("/admin/login")
    public String login(@RequestParam("examNum") String examNum, @RequestParam("password") String password, HttpSession session) {
        // 你的账号：admin，密码配置文件读取
        if ("admin".equals(examNum) && jwtProperties.getAdminPassword().equals(password)) {
            session.setAttribute("loginUser", examNum);
            session.setMaxInactiveInterval(8 * 60 * 60);
            // ✅ 修复：直接跳转 /admin
            return "redirect:/admin";
        }
        // ✅ 修复：登录失败回登录页
        return "redirect:/login.html";
    }

    @GetMapping("/admin")
    public String admin() {
        // ✅ 修复：直接跳转 /admin.html
        return "redirect:/admin.html";
    }

    @GetMapping("/public/verify_client")
    public String verifyClient() {
        // ✅ 修复：直接跳转 /verify.html
        return "redirect:/verify.html";
    }
}