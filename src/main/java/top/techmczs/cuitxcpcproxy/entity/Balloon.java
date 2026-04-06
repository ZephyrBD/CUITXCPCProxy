package top.techmczs.cuitxcpcproxy.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain=true)
public class Balloon {
    public Balloon() {
    }
    private Long balloonId;
    private LocalDateTime time;
    private String teamName;
    private String teamLocation;
    private String problem;
    private String colorName;
    private Boolean isFirst;
    private Boolean isFinished;
}
