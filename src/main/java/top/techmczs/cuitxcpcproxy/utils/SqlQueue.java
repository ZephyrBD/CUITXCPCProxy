package top.techmczs.cuitxcpcproxy.utils;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.techmczs.cuitxcpcproxy.entity.queuetask.QueueTask;

import java.time.LocalDateTime;
import java.util.function.BiFunction;

/**
 * 通用数据库持久化队列
 */
@Component
public class SqlQueue<T extends QueueTask> {

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 入队，插入数据库
     * @param task 任务对象
     * @param mapper 插入数据库的方法
     * @param expireMinutes 过期时间（分钟）
     */
    public void enqueue(T task, BaseMapper<T> mapper) {
        LocalDateTime now = LocalDateTime.now();
        //task.setCreateTime(now).setExpireTime(now.plusMinutes(expireMinutes)).setStatus(QueueTask.Status.PENDING);
        task.setCreateTime(now).setStatus(QueueTask.Status.PENDING);
        // 执行入库
        mapper.insert(task);
    }

    /**
     * 出队操作：查询队首 + 标记为处理中
     * @param mapper 查询最早待处理任务
     * @param queryWrapper 根据ID更新状态
     * @return 出队任务，无任务返回 null
     */
    public T dequeue(BaseMapper<T> mapper, Wrapper<T> queryWrapper) {
        // 查询队首任务
        T task = mapper.selectOne(queryWrapper);
        if (task == null) return null;

        // 使用普通UpdateWrapper+字符串字段名，不使用QueueTask接口Lambda
        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", task.getId())
                .set("status", QueueTask.Status.PROCESSING);
        mapper.update(null, updateWrapper);

        return task;
    }

    public void clear(Class<T> clazz){
        String tableName = TableInfoHelper.getTableInfo(clazz).getTableName();
        // 执行 TRUNCATE 清空+重置自增
        String sql = "TRUNCATE TABLE " + tableName;
        jdbcTemplate.execute(sql);
    }

    /**
     * 更新任务状态：完成/拒绝/过期
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long taskId, QueueTask.Status status, BiFunction<Long, QueueTask.Status, Integer> updateStatusFunction) {
        updateStatusFunction.apply(taskId, status);
    }
}