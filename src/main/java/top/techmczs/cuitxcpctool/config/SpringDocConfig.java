package top.techmczs.cuitxcpctool.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    //
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CUIT XCPC Tools API Document")
                        .description("本XCPC Tools所有接口的描述")
                        .version("1.0.0"));
    }

    //
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("top-api")
                .pathsToMatch("/**") //
                .packagesToScan("top") //
                .build();
    }
}

