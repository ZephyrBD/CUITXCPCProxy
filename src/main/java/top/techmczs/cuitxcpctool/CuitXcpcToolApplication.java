package top.techmczs.cuitxcpctool;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;
import org.springframework.boot.web.server.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.EnableScheduling;

@ServletComponentScan
@SpringBootApplication
@EnableScheduling
@MapperScan("top.techmczs.cuitxcpctool.mapper")
public class CuitXcpcToolApplication implements ApplicationListener<ServletWebServerInitializedEvent> {

    private static int serverPort;

    public static final String CXTOOL_VERSION = "Beta 0.3 | Preview";

    public static void main(String[] args) {
        SpringApplication.run(CuitXcpcToolApplication.class, args);
        printHelpInfo();
    }

    @Override
    public void onApplicationEvent(ServletWebServerInitializedEvent event) {
        serverPort = event.getWebServer().getPort();
    }

    public static void printHelpInfo(){
        System.out.println("======================================");
        System.out.println("Verify Web: http://localhost:" + serverPort + "/cxtool/#/auth");
        System.out.println("Admin Web: http://localhost:" + serverPort + "/cxtool/#/login");
        System.out.println("======================================");
    }
}