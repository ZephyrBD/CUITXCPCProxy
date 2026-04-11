package top.techmczs.cuitxcpctool.common;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 队列状态量，与QueueTask配合使用。
 */
@Getter
public enum QueueTaskStatus {
        PENDING("PENDING"),    // 待处理（队列中）
        DONE("DONE"),         // 已完成
        AUTO_DONE("AUTO_DONE"),
        DENY("DENY");          // 已拒绝

        @EnumValue
        final String status;

        QueueTaskStatus(String status){
            this.status = status;
        }
}
