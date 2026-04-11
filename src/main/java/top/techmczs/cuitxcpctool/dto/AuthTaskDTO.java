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

package top.techmczs.cuitxcpctool.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import top.techmczs.cuitxcpctool.common.QueueTaskStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain=true)
public class AuthTaskDTO implements Serializable {
    public AuthTaskDTO(){}
    public AuthTaskDTO(Long taskId,String teamName,String examNum,LocalDateTime loginTime,QueueTaskStatus status){
        this.taskId = taskId;
        this.teamName = teamName;
        this.examNum = examNum;
        this.loginTime = loginTime;
        this.status = status;
    }
    private Long taskId;
    private String teamName;
    private String examNum;
    private LocalDateTime loginTime;
    private QueueTaskStatus status;
}
