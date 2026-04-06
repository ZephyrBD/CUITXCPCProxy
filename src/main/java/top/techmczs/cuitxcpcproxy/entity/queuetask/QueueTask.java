package top.techmczs.cuitxcpcproxy.entity.queuetask;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain=true)
public abstract class QueueTask implements Serializable {

    @Getter
    public enum Status {
        PENDING("PENDING"),    // 待处理（队列中）
        PROCESSING("PROCESSING"),// 处理中（已出队）
        DONE("DONE"),         // 已完成
        DENY("DENY");          // 已拒绝
        //EXPIRED("EXPIRED");   // 已过期

        @EnumValue
        final String status;

        Status(String status){
            this.status = status;
        }
    }

    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDateTime createTime;  // 创建时间
    //private LocalDateTime expireTime;  // 过期时间
    private Status status;             // 状态
}
