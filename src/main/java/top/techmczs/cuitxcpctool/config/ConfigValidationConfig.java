package top.techmczs.cuitxcpctool.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import java.io.*;
import java.util.Properties;

@Configuration
public class ConfigValidationConfig {

    // ====================== 配置文件名称 ======================
    private static final String DB_CONFIG = "db-secret.properties";
    private static final String JUDGE_CONFIG = "settings.properties";

    // ====================== 资源模板文件 ======================
    private static final String DB_TEMPLATE = "db-secret.properties.sample";
    private static final String JUDGE_TEMPLATE = "settings.properties.sample";

    // 启动时校验所有配置
    @PostConstruct
    public void validateAllConfigs() {
        validateDatabaseConfig();
        validateJudgeConfig();
    }

    private void validateDatabaseConfig() {
        File file = new File(DB_CONFIG);
        if (!file.exists()) {
            copyTemplateToFile(DB_TEMPLATE, file);
            haltWithError(
                    "ERROR: " + DB_CONFIG + " not found",
                    "A sample file has been generated.",
                    "Please fill in the database passwords and restart."
            );
        }

        Properties props = loadProperties(file);
        String filePwd = props.getProperty("db.file.password", "").trim();
        String userPwd = props.getProperty("db.user.password", "").trim();

        if (filePwd.isBlank() || userPwd.isBlank()) {
            haltWithError(
                    "ERROR: Database passwords cannot be empty!",
                    "Please complete both db.file.password and db.user.password"
            );
        }
    }

    private void validateJudgeConfig() {
        File file = new File(JUDGE_CONFIG);
        if (!file.exists()) {
            copyTemplateToFile(JUDGE_TEMPLATE, file);
            haltWithError(
                    "ERROR: " + JUDGE_CONFIG + " not found",
                    "A sample file has been generated.",
                    "Please fill in all required fields and restart."
            );
        }

        Properties props = loadProperties(file);
        String port = props.getProperty("server.port", "").trim();

        // 非空校验
        if (port.isBlank()) {
            haltWithError("ERROR: Server port cannot be empty!");
        }
        // 端口合法性校验（必须是数字）
        try {
            Integer.parseInt(port);
        } catch (NumberFormatException e) {
            haltWithError("ERROR: Server port must be a valid number!");
        }

        String[] requiredKeys = {
                "org.domjudge.host",
                "org.domjudge.route-path",
                "org.domjudge.port",
                "org.domjudge.contest-id",
                "org.domjudge.account",
                "org.domjudge.password",
                "org.domjudge.print-token",
                "top.jwt.secret-key",
                "top.jwt.admin-password"
        };

        for (String key : requiredKeys) {
            if (props.getProperty(key, "").isBlank()) {
                haltWithError(
                        "ERROR: Required config is empty",
                        "Missing key: " + key
                );
            }
        }
    }

    private void copyTemplateToFile(String templatePath, File targetFile) {
        try (InputStream in = new ClassPathResource(templatePath).getInputStream();
             FileOutputStream out = new FileOutputStream(targetFile)) {
            in.transferTo(out);
        } catch (IOException e) {
            haltWithError("ERROR: Failed to generate config file: " + targetFile.getName());
        }
    }

    private Properties loadProperties(File file) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
        } catch (IOException e) {
            haltWithError("ERROR: Failed to read file: " + file.getName());
        }
        return props;
    }

    private void haltWithError(String... messages) {
        System.err.println("=====================================================");
        for (String msg : messages) {
            System.err.println(msg);
        }
        System.err.println("=====================================================");
        System.exit(1);
    }
}