package top.techmczs.cuitxcpcproxy.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.techmczs.cuitxcpcproxy.interceptor.AuthInterceptor;
import top.techmczs.cuitxcpcproxy.interceptor.LoginInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final LoginInterceptor loginInterceptor;
    private final AuthInterceptor authInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns()
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/*.html",   // 登录页
                        "/admin/login",        // 登录接口
                        "/public/**"     // 公共接口
                );
        registry.addInterceptor(authInterceptor);
    }
}