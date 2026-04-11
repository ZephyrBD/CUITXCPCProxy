package top.techmczs.cuitxcpctool.services;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import top.techmczs.cuitxcpctool.entity.DjTeam;

@Component
public interface DjTeamService {
    IPage<DjTeam> queryTeamsByPage(int curPage);
    void importTeamExcel(MultipartFile file);
}
