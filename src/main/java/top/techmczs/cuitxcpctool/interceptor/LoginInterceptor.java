package top.techmczs.cuitxcpctool.interceptor;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import top.techmczs.cuitxcpctool.properties.DomjudgeProperties;
import top.techmczs.cuitxcpctool.properties.JwtProperties;
import top.techmczs.cuitxcpctool.utils.JwtUtil;

/**
 * 登录拦截器
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    // 路径常量
    private static final String LOGIN_URI = "/cxtool/admin/login";
    private static final String PRINT_TASK_URI = "/cxtool/admin/print/task";
    private static final String PUBLIC_URI = "/cxtool/public";

    // 注入你的配置
    private final DomjudgeProperties domjudgeProperties;
    private final JwtProperties jwtProperties;

    // 构造器注入
    public LoginInterceptor(DomjudgeProperties domjudgeProperties, JwtProperties jwtProperties) {
        this.domjudgeProperties = domjudgeProperties;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {

        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        // 放行跨域预检 OPTIONS 请求
        if (HttpMethod.OPTIONS.matches(method)) {
            return true;
        }

        // 放行管理员登录接口
        if (LOGIN_URI.equals(requestUri)) {
            return true;
        }

        //System.out.println(requestUri);
        if(requestUri.startsWith(PUBLIC_URI)){
            return true;
        }
        // 特殊接口：打印任务(POST)，校验固定Token
        if (PRINT_TASK_URI.equals(requestUri) && HttpMethod.POST.matches(method)) {
            String token = request.getHeader("token");
            // 先判空再比较
            return token != null && token.equals(domjudgeProperties.getPrintToken());
        }

        // 拦截所有管理员接口，校验JWT
        String token = request.getHeader("Authorization");
        // SSE 兼容：从URL参数获取Token
        if (token == null) {
            token = request.getParameter("token");
        }

        // 无Token，直接返回401
        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        try {
            // JWT 校验
            String loginUser = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
            // 存入用户信息，后续接口可使用
            request.setAttribute("loginUser", loginUser);
            return true;
        } catch (Exception e) {
            // Token 无效/过期
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
}