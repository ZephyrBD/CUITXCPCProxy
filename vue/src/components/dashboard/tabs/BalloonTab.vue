<template>
  <div class="table-section">
    <div class="controls-panel">
      <el-button type="primary" @click="$emit('refresh')" size="small">刷新列表</el-button>
      <el-button type="warning" @click="$emit('start-auto')" size="small">开启自动小票打印</el-button>
    </div>

    <div class="table-wrapper">
      <el-table
        :data="balloonTableData"
        border
        stripe
        :header-cell-style="{background: '#fafafa', color: '#424242'}"
      >
        <el-table-column prop="balloonId" label="气球ID" width="100"></el-table-column>
        <el-table-column prop="teamName" label="队伍名称" min-width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="teamLocation" label="队伍位置" min-width="120"></el-table-column>
        <el-table-column prop="problem" label="题目编号" width="100"></el-table-column>
        <el-table-column prop="colorName" label="气球颜色" width="120"></el-table-column>
        <el-table-column prop="isFirst" label="一血" width="80">
          <template #default="{ row }">
            <el-tag type="danger" size="small" v-if="row.isFirst">是</el-tag>
            <el-tag type="info" size="small" v-else>否</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.time) }}
          </template>
        </el-table-column>
        <el-table-column prop="isFinished" label="发放状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.isFinished ? 'success' : 'warning'" size="small">
              {{ row.isFinished ? '已打印' : '待打印' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button
              size="small"
              type="success"
              @click="$emit('print', row)"
            >
              打印小票
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-section">
      <el-pagination
        :current-page="balloonCurrentPage"
        :page-size="balloonPageSize"
        layout="total, prev, pager, next"
        :total="balloonTotal"
        @current-change="$emit('page-change', $event)"
      ></el-pagination>
    </div>
  </div>
</template>

<script setup>
import { formatDateTime } from '@/utils/date'

const props = defineProps({
  balloonTableData: { type: Array, default: () => [] },
  balloonCurrentPage: { type: Number, default: 1 },
  balloonPageSize:{ type: Number, default: 10 },
  balloonTotal: { type: Number, default: 0 }
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