package top.techmczs.cuitxcpcproxy.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain=true)
public class TeamClient implements Serializable {
    @TableId
    private String examNum;
    private String clientId;
    private LocalDateTime loginTime;
}
