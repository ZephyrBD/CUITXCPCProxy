package top.techmczs.cuitxcpctool.services;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Component;
import top.techmczs.cuitxcpctool.common.QueueTaskStatus;
import top.techmczs.cuitxcpctool.dto.AdminDTO;
import top.techmczs.cuitxcpctool.dto.AuthTaskDTO;
import top.techmczs.cuitxcpctool.dto.DjTeamDTO;

@Component
public interface DjAuthService {
    boolean verifyToken(String token);
    DjTeamDTO verifyClientAndGetToken(String examNum, String clientId,String UserAgent);
    DjTeamDTO getApprovedTeamInfo(String examNum);
    QueueTaskStatus getAuthStatus(String examNum,String clientId);
    void acceptAuth(Long taskId);
    void denyAuth(Long taskId);
    AdminDTO getAdminToDomjudgeToken();
    IPage<AuthTaskDTO> queryAuthTasksByPage(int curPage);
    void clearAuthTaskQueue();
    void clearTeamClient();
}
