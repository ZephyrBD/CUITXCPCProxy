package top.techmczs.cuitxcpcproxy.services;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import top.techmczs.cuitxcpcproxy.entity.DjTeam;

@Component
public interface DjTeamService {
    IPage<DjTeam> queryTeamsByPage(int curPage);
    void importTeamExcel(MultipartFile file);
}
