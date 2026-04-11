<!--
  - Copyright (C) 2018-2026 Modding Craft ZBD Studio.
  -
  - This program is free software; you can redistribute it and/or modify
  - it under the terms of the GNU General Public License as published by
  - the Free Software Foundation; either version 2 of the License, or
  - (at your option) any later version.
  -
  - This program is distributed in the hope that it will be useful,
  - but WITHOUT ANY WARRANTY; without even the implied warranty of
  - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  - GNU General Public License for more details.
  -
  - You should have received a copy of the GNU General Public License along
  - with this program; if not, write to the Free Software Foundation, Inc.,
  - 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  -->

<template>
  <div class="table-section">
    <div class="controls-panel">
      <el-button type="primary" @click="$emit('refresh')" size="small">刷新列表</el-button>
      <el-button type="warning" @click="$emit('start-auto')" size="small">开启自动打印</el-button>
    </div>

    <div class="table-wrapper">
      <el-table
        :data="printTableData"
        border
        stripe
        :header-cell-style="{background: '#fafafa', color: '#424242'}"
      >
        <el-table-column prop="taskId" label="任务ID" width="100"></el-table-column>
        <el-table-column prop="teamName" label="队伍名称" min-width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="teamPosition" label="位置" min-width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              @click="$emit('download', row.taskId)"
            >
              下载PDF
            </el-button>
            <el-button
              size="small"
              type="success"
              @click="$emit('print', row)"
            >
              打印
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-section">
      <el-pagination
        :current-page="printCurrentPage"
        :page-size="printPageSize"
        layout="total, prev, pager, next"
        :total="printTotal"
        @current-change="$emit('page-change', $event)"
      ></el-pagination>
    </div>
  </div>
</template>

<script setup>
import {getStatusTagType, getStatusText} from '@/utils/status'

const props = defineProps({
  printTableData: { type: Array, default: () => [] },
  printCurrentPage: { type: Number, default: 1 },
  printPageSize: { type: Number, default: 10 },
  printTotal: { type: Number, default: 0 }
})
</script>

<style scoped>
.table-section {
  padding: 20px;
  position: relative;
}
.controls-panel {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.table-wrapper {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e0e0e0;
}
.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>