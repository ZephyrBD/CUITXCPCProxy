#!/bin/bash
#
# Copyright (C) 2018-2026 Modding Craft ZBD Studio.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#

# 参数顺序：位置 队伍名 [teamid] 文件名
LOCATION="$1"
TEAMNAME="$2"
EXAMNUM="$3"
FILE="$4"

# ================= 配置项 =================
PRINT_API="http://192.168.31.136:8080/cxtool/admin/print/task"
API_TOKEN="3486dsay89x6786f87aerfbxncmbmghjf"
PDF_DIR="/tmp/print"
# ==========================================

LOCATION=$(echo "$LOCATION" | tr -d "'\"" | xargs)
TEAMNAME=$(echo "$TEAMNAME" | tr -d "'\"" | xargs)
EXAMNUM=$(echo "$EXAMNUM" | tr -d "'\"" | xargs)
FILE=$(echo "$FILE" | tr -d "'\"" | xargs)

echo "====================="
echo "队伍位置：[${LOCATION}]"
echo "队伍名称：[${TEAMNAME}]"
echo "源文件：[${FILE}]"
echo "====================="

if [ -z "$TEAMNAME" ] || [ -z "$LOCATION" ]; then
    echo "❌ 错误：队伍信息不能为空"
    exit 1
fi

mkdir -p "$PDF_DIR"
PDF_FILE="${PDF_DIR}/${TEAMNAME}_$(date +%Y%m%d%H%M%S).pdf"
enscript -b "ExamNumber: ${EXAMNUM},Delivery location: ${LOCATION},Print Time: $(date +%Y%m%d%H%M%S)" -f Courier10 -A4 "$FILE" -p - | ps2pdf - "$PDF_FILE"

# PDF生成校验
if [ ! -f "$PDF_FILE" ]; then
    echo "❌ 错误：PDF生成失败！"
    exit 1
fi

# 发送打印请求
curl -X POST "$PRINT_API" \
-H "token: $API_TOKEN" \
-F "file=@${PDF_FILE}" \
-F "printTeamDTO={\"examNum\":\"${EXAMNUM}\"};type=application/json"

echo -e "\n✅ 打印任务提交成功"
rm -f "$PDF_FILE"
echo "🗑️ 临时PDF文件已清理：${PDF_FILE}"