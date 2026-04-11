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

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 对应数据库表 dj_team
 */
@Data
@TableName("dj_team") // 绑定表名
public class DjTeam implements Serializable {

    @ExcelProperty("examNumber") // Excel列名
    @TableId
    private String examNumber;

    @ExcelProperty("teamName") // Excel列名
    private String teamName;

    @ExcelProperty("school")
    private String school;

    @ExcelProperty("position")
    private String position;

    @ExcelProperty("account")
    private String account;

    @ExcelProperty("password")
    private String password;
}