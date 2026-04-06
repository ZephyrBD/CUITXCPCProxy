package top.techmczs.cuitxcpcproxy.entity.queuetask;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AuthTask extends QueueTask {
    public AuthTask() {}
    public AuthTask(String oldClientId,String nowClientId,String examNum){
        this.oldClientId = oldClientId;
        this.nowClientId = nowClientId;
        this.examNum = examNum;
    }
    private String oldClientId;
    private String nowClientId;
    private String examNum;
}
