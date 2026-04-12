/*
 * Copyright (C) 2018-2026 Modding Craft ZBD Studio.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package top.techmczs.cuitxcpctool;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;
import org.springframework.boot.web.server.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import top.techmczs.cuitxcpctool.utils.SettingsCheckUtil;

@ServletComponentScan
@SpringBootApplication
@EnableScheduling
@MapperScan("top.techmczs.cuitxcpctool.mapper")
public class CuitXcpcToolApplication implements ApplicationListener<ServletWebServerInitializedEvent> {

    private static int serverPort;

    // UTODO 需要完善Swagger
    public static final String CXTOOL_VERSION = "0.4-Preview";

    public static void main(String[] args) {
        printTitle();
        SettingsCheckUtil.init();
        SpringApplication.run(CuitXcpcToolApplication.class, args);
        printHelpInfo();
    }

    @Override
    public void onApplicationEvent(ServletWebServerInitializedEvent event) {
        serverPort = event.getWebServer().getPort();
    }

    public static void printTitle(){
        System.out.println(
                " ██████╗██╗   ██╗██╗████████╗    ██╗  ██╗ ██████╗██████╗  ██████╗    ████████╗ ██████╗  ██████╗ ██╗     \n" +
                "██╔════╝██║   ██║██║╚══██╔══╝    ╚██╗██╔╝██╔════╝██╔══██╗██╔════╝    ╚══██╔══╝██╔═══██╗██╔═══██╗██║     \n" +
                "██║     ██║   ██║██║   ██║        ╚███╔╝ ██║     ██████╔╝██║            ██║   ██║   ██║██║   ██║██║     \n" +
                "██║     ██║   ██║██║   ██║        ██╔██╗ ██║     ██╔═══╝ ██║            ██║   ██║   ██║██║   ██║██║     \n" +
                "╚██████╗╚██████╔╝██║   ██║       ██╔╝ ██╗╚██████╗██║     ╚██████╗       ██║   ╚██████╔╝╚██████╔╝███████╗\n" +
                " ╚═════╝ ╚═════╝ ╚═╝   ╚═╝       ╚═╝  ╚═╝ ╚═════╝╚═╝      ╚═════╝       ╚═╝    ╚═════╝  ╚═════╝ ╚══════╝\n" +
                "\n" +
                " \t "+"Ver:" + CXTOOL_VERSION + " █ Based Spring Boot:4.0.5 █ Designed and Powered By ZephyrBD\n");
    }

    public static void printHelpInfo(){
        System.out.println("======================================");
        System.out.println("Verify Web: http://localhost:" + serverPort + "/cxtool/#/auth");
        System.out.println("Admin Web: http://localhost:" + serverPort + "/cxtool/#/login");
        System.out.println("If you open API Documentation, please use next url to visit.");
        System.out.println("http://localhost:" + serverPort + "/cxtool/swagger-ui/index.html#/");
        System.out.println("======================================");
    }
}