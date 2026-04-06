package top.techmczs.cuitxcpcproxy.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "top.jwt")
@Data
public class JwtProperties {
    // 管理jwt令牌相关配置
    private String secretKey;
    private long ttl;

    private String adminPassword;
}