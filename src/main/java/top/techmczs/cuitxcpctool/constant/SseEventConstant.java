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

package top.techmczs.cuitxcpctool.constant;

/**
 * SSE队列事件名常量
 */
public class SseEventConstant {
    /**
     *连接事件
     **/
    public static final String CONNECT = "connect";
    /**
     * 心跳事件
     */
    public static final String HEARTBEAT = "heartbeat";
    /**
     * 气球队列事件
     */
    public static final String BALLOON_TASK = "balloonTask";
    /**
     * 打印队列事件
     */
    public static final String PRINT_TASK = "printTask";
    /**
     * 登录队列事件
     */
    public static final String AUTH_TASK = "authTask";
}
