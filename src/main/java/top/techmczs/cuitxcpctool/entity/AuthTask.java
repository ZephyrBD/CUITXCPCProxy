package top.techmczs.cuitxcpctool.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.techmczs.cuitxcpctool.common.QueueTask;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
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
