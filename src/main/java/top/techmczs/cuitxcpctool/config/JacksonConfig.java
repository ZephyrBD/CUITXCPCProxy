package top.techmczs.cuitxcpctool.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class JacksonConfig {

    // 注册RestTemplate，Spring就可以自动注入了
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    // 注册ObjectMapper，Spring自动注入
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
