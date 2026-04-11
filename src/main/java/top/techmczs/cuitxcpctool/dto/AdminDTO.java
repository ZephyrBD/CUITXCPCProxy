package top.techmczs.cuitxcpctool.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class AdminDTO implements Serializable {
    private String url;
    private String token;
}
