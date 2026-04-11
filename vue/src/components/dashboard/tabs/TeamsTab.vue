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
      <el-button type="primary" @click="$emit('refresh')" size="small">刷新队伍信息</el-button>
      <!-- 上传Excel按钮 -->
      <el-button type="success" size="small" @click="triggerFileSelect">
        <i class="el-icon-upload"></i> 导入队伍Excel
      </el-button>
    </div>

    <div class="table-wrapper">
      <el-table
        :data="teamsTableData"
        border
        stripe
        :header-cell-style="{background: '#fafafa', color: '#424242'}"
      >
        <el-table-column prop="teamName" label="队伍名称" min-width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="school" label="学校" min-width="180" show-overflow-tooltip></el-table-column>
        <el-table-column prop="position" label="位置" min-width="120" show-overflow-tooltip></el-table-column>
        <el-table-column prop="examNumber" label="考号" min-width="120"></el-table-column>
        <el-table-column prop="account" label="账号" min-width="120"></el-table-column>
        <el-table-column prop="password" label="密码" min-width="120"></el-table-column>
      </el-table>
    </div>

    <div class="pagination-section">
      <el-pagination
        :current-page="teamsCurrentPage"
        :page-size="teamsPageSize"
        layout="total, prev, pager, next"
        :total="teamsTotal"
        @current-change="$emit('page-change', $event)"
      ></el-pagination>
    </div>

    <!-- 隐藏文件选择框 -->
    <input 
      type="file" 
      ref="fileInputRef" 
      @change="handleFileChange" 
      accept=".xlsx" 
      style="display: none;"
    >
  </div>
</template>

<script setup>
import {ref} from 'vue'

const props = defineProps({
  teamsTableData: { type: Array, default: () => [] },
  teamsCurrentPage: { type: Number, default: 1 },
  teamsPageSize: { type: Number, default: 10 },
  teamsTotal: { type: Number, default: 0 }
})

const emit = defineEmits(['refresh', 'page-change', 'file-upload'])
const fileInputRef = ref(null)

// 触发文件选择
const triggerFileSelect = () => {
  fileInputRef.value?.click()
}

// 文件选择后传递给父组件
const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (!file) return
  emit('file-upload', file)
  e.target.value = ''
}
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