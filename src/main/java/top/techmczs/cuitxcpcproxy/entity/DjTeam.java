package top.techmczs.cuitxcpcproxy.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain=true)
public class DjTeam implements Serializable {
    private String teamName;
    private String School;
    private String position;
    @TableId
    private String examNumber; //domjudge TeamID
    private String account;
    private String password;
}
