package top.techmczs.cuitxcpctool.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "org.domjudge")
@Data
public class DomjudgeProperties {

    private String host;
    private int port;
    private String contestId;
    private String nginxVerifyRoutePath;
    private String routePath;
    private String account;
    private String password;

    private boolean useSpecialClient;
    private String specialClientUserAgent;
    private String printToken;

    public String getAuth(){
        return account + ":" + password;
    }

    public String getDomjudgeBalloonApiUrl(Boolean isNotDone){
        return this.host + ":" + this.port + routePath + "/api/v4/contests/" + contestId + "/balloons?todo=" + isNotDone.toString();
    }

    public String getDomjudgeBalloonApiUrl(Long balloonId){
        return this.host + ":" + this.port + routePath + "/api/v4/contests/" + contestId + "/balloons" + "/" + balloonId + "/done";
    }

    public String getVerifyUrl(){
        return this.host + ":" + this.port + this.nginxVerifyRoutePath;
    }

}
