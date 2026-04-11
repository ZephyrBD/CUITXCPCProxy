package top.techmczs.cuitxcpctool.services;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Component;
import top.techmczs.cuitxcpctool.entity.Balloon;

@Component
public interface DjBalloonService {
    IPage<Balloon> getAllBalloonFromDomjudge(int cur);
    void setBalloonDone(Long id);
}
