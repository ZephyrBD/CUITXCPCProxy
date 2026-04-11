package top.techmczs.cuitxcpctool.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import top.techmczs.cuitxcpctool.common.QueueTaskStatus;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class PrintTaskDTO implements Serializable {
    private Long taskId;
    private String teamName;
    private String teamPosition;
    private QueueTaskStatus status;
}
