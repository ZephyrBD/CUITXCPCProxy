package top.techmczs.cuitxcpctool.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "top.jwt")
@Data
public class JwtProperties {
    public static final String ADMIN_ACCOUNT = "admin";
    public static final Long ADMIN_TOKEN_TIME = 28800000L;

    // 管理jwt令牌相关配置
    private String secretKey;
    private long ttl;
    private String adminPassword;
}