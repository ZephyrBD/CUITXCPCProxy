package top.techmczs.cuitxcpcproxy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import top.techmczs.cuitxcpcproxy.entity.TeamClient;

@Mapper
public interface TeamClientMapper extends BaseMapper<TeamClient> {
    @Delete("TRUNCATE TABLE team_client")
    void clearAndResetAutoIncrement();
}
