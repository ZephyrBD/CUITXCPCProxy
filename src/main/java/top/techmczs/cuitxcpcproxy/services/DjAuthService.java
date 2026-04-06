package top.techmczs.cuitxcpcproxy.services;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.techmczs.cuitxcpcproxy.dto.AdminDTO;
import top.techmczs.cuitxcpcproxy.dto.AuthTaskDTO;
import top.techmczs.cuitxcpcproxy.dto.DjTeamDTO;
import top.techmczs.cuitxcpcproxy.entity.queuetask.QueueTask;

@Component
public interface DjAuthService {
    boolean verifyToken(String token);
    DjTeamDTO verifyClientAndGetToken(String examNum, String clientId,String UserAgent);
    DjTeamDTO getApprovedTeamInfo(String examNum);
    //AuthTaskDTO getAuthTopTaskFromQueue();
    //void pushAuthTaskBySse(SseEmitter emitter);
    void registerAuthClient(SseEmitter emitter);
    QueueTask.Status getAuthStatus(String examNum);
    void acceptAuth(Long taskId);
    void denyAuth(Long taskId);

    AdminDTO getAdminToDomjudgeToken();
    IPage<AuthTaskDTO> queryAuthTasksByPage(int curPage);
    void clearAuthTaskQueue();
    void clearTeamClient();
}
