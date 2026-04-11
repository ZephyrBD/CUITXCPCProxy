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

package top.techmczs.cuitxcpctool.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 通用转换工具类
 * 时间戳（秒.纳秒）转 LocalDateTime
 **/
public class TimeUtil {

    /**
     * 秒级时间戳(带小数纳秒) 转换为 LocalDateTime
     * @param timestamp 时间戳：如 1775478285.104100000
     * @return LocalDateTime
     */
    public static LocalDateTime timestampToLocalDateTime(double timestamp) {
        // 拆分：整数部分=秒，小数部分=纳秒
        long seconds = (long) timestamp;
        int nano = (int) ((timestamp - seconds) * 1_000_000_000);
        // 用JDK自带Instant转换 + 系统默认时区
        return Instant.ofEpochSecond(seconds, nano)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

}