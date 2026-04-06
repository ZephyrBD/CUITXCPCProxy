package top.techmczs.cuitxcpcproxy.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import top.techmczs.cuitxcpcproxy.entity.queuetask.QueueTask;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain=true)
public class AuthTaskDTO implements Serializable {
    public AuthTaskDTO(){}
    public AuthTaskDTO(Long taskId,String teamName,String examNum,LocalDateTime loginTime,QueueTask.Status status){
        this.taskId = taskId;
        this.teamName = teamName;
        this.examNum = examNum;
        this.loginTime = loginTime;
        this.status = status;
    }
    private Long taskId;
    private String teamName;
    private String examNum;
    private LocalDateTime loginTime;
    private QueueTask.Status status;
}
