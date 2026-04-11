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
