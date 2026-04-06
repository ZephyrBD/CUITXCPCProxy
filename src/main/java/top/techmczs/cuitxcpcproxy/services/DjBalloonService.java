package top.techmczs.cuitxcpcproxy.services;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.techmczs.cuitxcpcproxy.entity.Balloon;

@Component
public interface DjBalloonService {
    //SseEmitter connectSse();
    void registerClient(SseEmitter emitter);

    // ===================== 拉取全量气球（/print/all 用） =====================
    IPage<Balloon> getAllBalloonFromDomjudge(int cur);

    // ===================== 标记气球已打印（实现你调用的方法） =====================
    void setBalloonDone(Long id);
}
