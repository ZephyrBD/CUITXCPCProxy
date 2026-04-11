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

package top.techmczs.cuitxcpctool.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.techmczs.cuitxcpctool.common.QueueTask;
import top.techmczs.cuitxcpctool.dto.PrintTeamDTO;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PrintTask extends QueueTask {
    public PrintTask() {}
    public PrintTask(PrintTeamDTO printTeamDTO, String fileName, String filePath) {
        this.examNum = printTeamDTO.getExamNum();
        this.fileName = fileName;
        this.filePath = filePath;
    }
    private String examNum;
    private String filePath;
    private String fileName;
}
