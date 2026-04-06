package top.techmczs.cuitxcpcproxy.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 通用转换工具类
 * 时间戳（秒.纳秒）转 LocalDateTime
 **/
public class TimeUtil {

    /**
     * 【JDK自带库】将 秒级时间戳(带小数纳秒) 转换为 LocalDateTime
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