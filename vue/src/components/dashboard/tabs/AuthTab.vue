<template>
  <div class="table-section">
    <div class="controls-panel">
      <el-button type="primary" @click="$emit('refresh')" size="small">刷新列表</el-button>
    </div>

    <div class="table-wrapper">
      <el-table
        :data="authTableData"
        border
        stripe
        :header-cell-style="{background: '#fafafa', color: '#424242'}"
      >
        <el-table-column prop="taskId" label="任务ID" width="100"></el-table-column>
        <el-table-column prop="teamName" label="队伍名称" min-width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="examNum" label="考号" width="120"></el-table-column>
        <el-table-column prop="loginTime" label="登录时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.loginTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              @click="$emit('accept', row.taskId)"
              :disabled="row.status === 'DONE' || row.status === 'AUTO_DONE'"
            >
              同意
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="$emit('deny', row.taskId)"
              :disabled="row.status === 'DONE' || row.status === 'AUTO_DONE'"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-section">
      <el-pagination
        :current-page="authCurrentPage"
        :page-size="authPageSize"
        layout="total, prev, pager, next"
        :total="authTotal"
        @current-change="$emit('page-change', $event)"
      ></el-pagination>
    </div>
  </div>
</template>

<script setup>
import { getStatusTagType, getStatusText } from '@/utils/status'
import { formatDateTime } from '@/utils/date'

const props = defineProps({
  authTableData: { type: Array, default: () => [] },
  authCurrentPage: { type: Number, default: 1 },
  authPageSize: { type: Number, default: 10 },
  authTotal: { type: Number, default: 0 }
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