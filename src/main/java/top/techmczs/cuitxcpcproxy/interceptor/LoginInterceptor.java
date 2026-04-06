package top.techmczs.cuitxcpcproxy.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import top.techmczs.cuitxcpcproxy.properties.DomjudgeProperties;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final DomjudgeProperties domjudgeProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {

        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        //System.out.println(requestUri);
        if ("/cxtool/admin/print/task".equals(requestUri) && HttpMethod.POST.toString().equalsIgnoreCase(method)) {
            return request.getHeader("token").equals(domjudgeProperties.getPrintToken());
        }

        HttpSession session = request.getSession();
        Object loginUser = session.getAttribute("loginUser");

        if (loginUser != null) {
            return true;
        }

        response.sendRedirect("/cxtool/login.html");
        return false;
    }
}