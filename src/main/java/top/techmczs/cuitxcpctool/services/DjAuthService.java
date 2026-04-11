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

package top.techmczs.cuitxcpctool.services;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Component;
import top.techmczs.cuitxcpctool.common.QueueTaskStatus;
import top.techmczs.cuitxcpctool.dto.AdminDTO;
import top.techmczs.cuitxcpctool.dto.AuthTaskDTO;
import top.techmczs.cuitxcpctool.dto.DjTeamDTO;

@Component
public interface DjAuthService {
    boolean verifyToken(String token);
    DjTeamDTO verifyClientAndGetToken(String examNum, String clientId,String UserAgent);
    DjTeamDTO getApprovedTeamInfo(String examNum);
    QueueTaskStatus getAuthStatus(String examNum,String clientId);
    void acceptAuth(Long taskId);
    void denyAuth(Long taskId);
    AdminDTO getAdminToDomjudgeToken();
    IPage<AuthTaskDTO> queryAuthTasksByPage(int curPage);
    void clearAuthTaskQueue();
    void clearTeamClient();
}
