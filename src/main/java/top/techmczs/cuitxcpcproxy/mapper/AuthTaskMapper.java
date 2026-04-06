package top.techmczs.cuitxcpcproxy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.techmczs.cuitxcpcproxy.entity.queuetask.AuthTask;

@Mapper
public interface AuthTaskMapper extends BaseMapper<AuthTask> {
}
