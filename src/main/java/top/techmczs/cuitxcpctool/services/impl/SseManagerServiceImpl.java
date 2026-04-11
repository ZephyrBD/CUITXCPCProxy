/*
 * Copyright (C) 2018-2026 Modding Craft ZBD Studio.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package top.techmczs.cuitxcpctool.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.techmczs.cuitxcpctool.constant.MessageConstant;
import top.techmczs.cuitxcpctool.constant.SseEventConstant;
import top.techmczs.cuitxcpctool.result.Result;
import top.techmczs.cuitxcpctool.services.SseManagerService;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 全局唯一SSE管理服务：所有业务共享连接
 */
@Service
@Slf4j
public class SseManagerServiceImpl implements SseManagerService {

    // 全局唯一SSE客户端集合
    private final Set<SseEmitter> GLOBAL_SSE_CLIENTS = new CopyOnWriteArraySet<>();

    /**
     * 统一注册SSE客户端
     */
    @Override
    public SseEmitter registerClient(String connectSuccessMsg) {
        SseEmitter emitter = new SseEmitter(0L);
        GLOBAL_SSE_CLIENTS.add(emitter);

        // 生命周期统一管理
        emitter.onCompletion(() -> {
            GLOBAL_SSE_CLIENTS.remove(emitter);
            log.info(MessageConstant.SEE_CLOSE, GLOBAL_SSE_CLIENTS.size());
        });
        emitter.onTimeout(() -> {
            GLOBAL_SSE_CLIENTS.remove(emitter);
            emitter.complete();
            log.warn(MessageConstant.SSE_TIME_OUT);
        });
        emitter.onError(ex -> {
            GLOBAL_SSE_CLIENTS.remove(emitter);
            emitter.completeWithError(ex);
            log.error(MessageConstant.SSE_LINK_FAILED);
        });

        // 发送连接成功消息
        try {
            emitter.send(SseEmitter.event()
                    .name(SseEventConstant.CONNECT)
                    .data(Result.success(connectSuccessMsg)));
        } catch (IOException e) {
            GLOBAL_SSE_CLIENTS.remove(emitter);
            log.error(MessageConstant.SSE_SEND_INIT_MESSAGE_FAILED);
        }

        return emitter;
    }

    /**
     * 统一广播消息（按事件名区分业务）
     */
    @Override
    public void broadcast(String eventName, Object data) {
        // CopyOnWriteArrayList 专用：removeIf 线程安全，支持删除
        GLOBAL_SSE_CLIENTS.removeIf(emitter -> {
            try {
                // 尝试发送消息
                emitter.send(SseEmitter.event().name(eventName).data(data));
                return false; // 发送成功 → 返回false,删除当前发射器
            } catch (Exception e) {
                // 发送失败：连接断开→ 删除当前发射器，返回true
                log.warn(MessageConstant.SSE_BROADCAST_FAILED, eventName);
                return true;
            }
        });
    }

    /**
     * 统一心跳
     */
    @Override
    public void sendHeartbeat() {
        broadcast(SseEventConstant.HEARTBEAT, "ping");
    }
}