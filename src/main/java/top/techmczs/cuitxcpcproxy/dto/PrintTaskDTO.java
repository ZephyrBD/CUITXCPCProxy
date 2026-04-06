package top.techmczs.cuitxcpcproxy.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import top.techmczs.cuitxcpcproxy.entity.queuetask.QueueTask;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class PrintTaskDTO implements Serializable {
    private Long taskId;
    private String teamName;
    private String teamPosition;
    private QueueTask.Status status;
}
