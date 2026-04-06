package top.techmczs.cuitxcpcproxy.services.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.techmczs.cuitxcpcproxy.constant.MessageConstant;
import top.techmczs.cuitxcpcproxy.entity.DjTeam;
import top.techmczs.cuitxcpcproxy.exception.ImportExcelException;
import top.techmczs.cuitxcpcproxy.mapper.DjTeamMapper;
import top.techmczs.cuitxcpcproxy.services.DjTeamService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DjTeamServiceImpl implements DjTeamService {

    private final DjTeamMapper djTeamMapper;

    @Override
    public IPage<DjTeam> queryTeamsByPage(int curPage) {
        Page<DjTeam> page = new Page<>(curPage, 10);
        return djTeamMapper.selectPage(page, null);
    }

    @Override
    public void importTeamExcel(MultipartFile file) {
        try {
            // 解析Excel第一行是表头，自动映射到DjTeam字段
            List<DjTeam> teamList = EasyExcel.read(file.getInputStream())
                    .head(DjTeam.class) // 表头对应实体类
                    .sheet() // 读取第一个sheet
                    .doReadSync(); // 同步读取所有数据

            // 批量保存到数据库
            if (!teamList.isEmpty()) {
                djTeamMapper.delete(null);
                djTeamMapper.insert(teamList);
            }
        } catch (Exception e) {
            throw new ImportExcelException(MessageConstant.IMPORT_TEAM_FROM_EXCEL);
        }
    }
}
