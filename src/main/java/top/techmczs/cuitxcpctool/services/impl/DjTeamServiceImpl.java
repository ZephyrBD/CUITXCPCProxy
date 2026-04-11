package top.techmczs.cuitxcpctool.services.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.techmczs.cuitxcpctool.constant.MessageConstant;
import top.techmczs.cuitxcpctool.entity.DjTeam;
import top.techmczs.cuitxcpctool.exception.ImportExcelException;
import top.techmczs.cuitxcpctool.mapper.DjTeamMapper;
import top.techmczs.cuitxcpctool.services.DjTeamService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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
            List<DjTeam> teamList = EasyExcel.read(file.getInputStream())
                    .head(DjTeam.class)
                    .sheet()
                    .doReadSync();

            djTeamMapper.delete(null);
            djTeamMapper.insert(teamList);
        } catch (Exception e) {
            throw new ImportExcelException(MessageConstant.IMPORT_TEAM_FROM_EXCEL);
        }
    }
}
