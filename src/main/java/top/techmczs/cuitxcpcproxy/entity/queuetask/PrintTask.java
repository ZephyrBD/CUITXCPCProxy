package top.techmczs.cuitxcpcproxy.entity.queuetask;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.techmczs.cuitxcpcproxy.dto.PrintTeamDTO;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PrintTask extends QueueTask {
    public PrintTask() {}
    public PrintTask(PrintTeamDTO printTeamDTO, String fileName, String filePath) {
        this.examNum = printTeamDTO.getExamNum();
        this.fileName = fileName;
        this.filePath = filePath;
    }
    private String examNum;
    private String filePath;
    private String fileName;
}
