package top.techmczs.cuitxcpctool.common;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain=true)
/*
  数据库队列需要的属性和常量抽象类，需要在数据库中存储的Task应当继承。
 */
public abstract class QueueTask implements Serializable {

    /**
     * QueueTask status属性对应的数据库表列名。
     **/
    public static final String COLUMN_STATUS_NAME = "status";
    /**
     * QueueTask createTime属性对应的数据库表列名。
     **/
    public static final String COLUMN_CREATE_TIME_NAME = "create_time";

    /**
     * QueueTask 任务ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * QueueTask 任务创建时间
     */
    private LocalDateTime createTime;
    /**
     * QueueTask 状态，状态值应该取QueueTaskStatus中的常量
     */
    private QueueTaskStatus status;
}
