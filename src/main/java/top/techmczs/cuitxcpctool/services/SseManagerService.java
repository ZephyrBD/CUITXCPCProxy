package top.techmczs.cuitxcpctool.services;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public interface SseManagerService {
    SseEmitter registerClient(String connectSuccessMsg);
    void broadcast(String eventName, Object data);
    void sendHeartbeat();
}
