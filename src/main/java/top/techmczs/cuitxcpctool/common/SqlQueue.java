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

package top.techmczs.cuitxcpctool.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

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
     */
    public void enqueue(T task, BaseMapper<T> mapper) {
        LocalDateTime now = LocalDateTime.now();
        task.setCreateTime(now).setStatus(QueueTaskStatus.PENDING);
        // 执行入库
        mapper.insert(task);
    }

    public void enqueue(T task, BaseMapper<T> mapper, QueueTaskStatus status) {
        LocalDateTime now = LocalDateTime.now();
        task.setCreateTime(now).setStatus(status);
        // 执行入库
        mapper.insert(task);
    }

    /**
     * 出队操作：查询队首 + 标记为处理中
     * @param mapper 查询最早待处理任务
     * @return 出队任务，无任务返回 null
     */
    public List<T> dequeueTasks(BaseMapper<T> mapper) {
        QueryWrapper<T> queryWrapper =  new QueryWrapper<>();
        queryWrapper.eq(QueueTask.COLUMN_STATUS_NAME, QueueTaskStatus.PENDING)
                .orderByAsc(QueueTask.COLUMN_CREATE_TIME_NAME);
        return mapper.selectList(queryWrapper);
    }

    public void clear(Class<T> clazz){
        String tableName = TableInfoHelper.getTableInfo(clazz).getTableName();
        // 执行 TRUNCATE 清空+重置自增
        String sql = "TRUNCATE TABLE " + tableName + " RESTART IDENTITY";
        jdbcTemplate.execute(sql);
    }

}