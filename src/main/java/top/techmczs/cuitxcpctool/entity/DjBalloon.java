package top.techmczs.cuitxcpctool.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DjBalloon {
    @JsonProperty("balloonid")
    private Long balloonId;
    private String time;
    private String team;
    private String location;

    @JsonProperty("contestproblem")
    private ContestProblem contestProblem;
    private Boolean done;

    @Data
    @Accessors(chain = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContestProblem {
        @JsonProperty("short_name")
        private String shortName;
        private String color;
    }
}
