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

package top.techmczs.cuitxcpctool.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

public class PdfUtil {
    private static final String BASE_DIR = System.getProperty("user.dir");
    public static final String PDF_DIR = BASE_DIR + File.separator + "pdf" + File.separator;

    public static String savePdf(MultipartFile file) throws Exception {
        // 自动创建pdf文件夹
        File directory = new File(PDF_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 原始文件名
        String originalFilename = file.getOriginalFilename();
        // 文件后缀（.pdf）
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 唯一文件名
        String uniqueFileName = UUID.randomUUID() + suffix;
        // 最终文件完整路径
        String absoluteFilePath = PDF_DIR + uniqueFileName;

        // 写入文件
        file.transferTo(new File(absoluteFilePath));

        // 返回存储路径（给数据库保存）
        return absoluteFilePath;
    }

    public static File readPdf(String path){
        return new File(path);
    }
}
