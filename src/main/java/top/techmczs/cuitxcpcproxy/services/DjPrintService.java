package top.techmczs.cuitxcpcproxy.services;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.techmczs.cuitxcpcproxy.dto.PrintTaskDTO;
import top.techmczs.cuitxcpcproxy.dto.PrintTeamDTO;

@Component
public interface DjPrintService {
    //PrintTaskDTO getAuthTopTaskFromQueue();
    void registerPrintClient(SseEmitter emitter);
    void addPrintTask(MultipartFile file, PrintTeamDTO printTeamDTO);
    void setPrintTaskDone(Long taskId);
    byte[] getPdfFileByTaskId(Long taskId);
    IPage<PrintTaskDTO> queryAuthTasksByPage(int curPage);
    void clearAll();
}
