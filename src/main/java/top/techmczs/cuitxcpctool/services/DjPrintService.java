package top.techmczs.cuitxcpctool.services;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import top.techmczs.cuitxcpctool.dto.PrintTaskDTO;
import top.techmczs.cuitxcpctool.dto.PrintTeamDTO;

@Component
public interface DjPrintService {
    void addPrintTask(MultipartFile file, PrintTeamDTO printTeamDTO);
    void setPrintTaskDone(Long taskId);
    byte[] getPdfFileByTaskId(Long taskId);
    IPage<PrintTaskDTO> queryAuthTasksByPage(int curPage);
    void clearAll();
}
