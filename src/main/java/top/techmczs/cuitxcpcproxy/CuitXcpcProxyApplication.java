package top.techmczs.cuitxcpcproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;
import org.springframework.boot.web.server.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ServletComponentScan
@SpringBootApplication
@EnableScheduling
@MapperScan("top.techmczs.cuitxcpcproxy.mapper")
public class CuitXcpcProxyApplication implements ApplicationListener<ServletWebServerInitializedEvent> {

    private static int serverPort;

    public static void main(String[] args) {
        SpringApplication.run(CuitXcpcProxyApplication.class, args);
        printHelpInfo();
    }

    @Override
    public void onApplicationEvent(ServletWebServerInitializedEvent event) {
        serverPort = event.getWebServer().getPort();
    }

    public static void printHelpInfo(){
        System.out.println("======================================");
        System.out.println("Verify Web: http://localhost:" + serverPort + "/cxtool/public/verify_client");
        System.out.println("Admin Web: http://localhost:" + serverPort + "/cxtool/admin");
        System.out.println("======================================");
    }
}