package top.techmczs.cuitxcpctool.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
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