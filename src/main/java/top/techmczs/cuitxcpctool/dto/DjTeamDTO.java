package top.techmczs.cuitxcpctool.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain=true)
public class DjTeamDTO implements Serializable {
    public DjTeamDTO() {
    }
    @NotNull
    private String examNumber;
    @NotNull
    private String teamName;
    @NotNull
    private String account;
    @NotNull
    private String password;
    @NotNull
    private String djUrl;
    @NotNull
    private String token;
    @NotNull
    private String position;
    @NotNull
    private LocalDateTime loginTime;
}
