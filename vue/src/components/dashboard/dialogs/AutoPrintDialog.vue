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
  <el-dialog :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)" title="自动打印" width="600px" center>
    <div class="auto-content">
      <div v-if="task" class="current-task-info">
        <div class="task-item"><span class="task-label">任务ID:</span><span class="task-value">{{ task.taskId || '-' }}</span></div>
        <div class="task-item"><span class="task-label">队伍名称:</span><span class="task-value">{{ task.teamName || '-' }}</span></div>
        <div class="task-item"><span class="task-label">位置:</span><span class="task-value">{{ task.teamPosition || '无' }}</span></div>
        <div class="task-item"><span class="task-label">状态:</span><span class="task-value"><el-tag :type="getStatusTagType(task.status)" size="small">{{ getStatusText(task.status) }}</el-tag></span></div>
        <div v-if="status" :class="['task-status', `status-${status.type}`]">{{ status.message }}</div>
      </div>
      <div v-else><p style="color: #757575;">等待打印任务...</p></div>
      <div class="action-buttons"><el-button type="danger" @click="$emit('stop')" size="large">停止自动打印</el-button></div>
    </div>
  </el-dialog>
</template>

<script setup>
import {getStatusTagType, getStatusText} from '@/utils/status'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  task: { type: Object, default: () => ({}) },
  status: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['update:modelValue','stop'])
</script>

<style scoped>
.auto-content { padding: 24px; }
.current-task-info { background: #fafafa; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); max-width: 500px; margin: 0 auto 20px; }
.task-item { display: flex; align-items: center; padding: 10px 12px; background: white; border-radius: 6px; margin-bottom: 8px; border: 1px solid #f0f0f0; }
.task-label { width: 100px; flex-shrink: 0; font-weight: 500; color: #424242; }
.task-value { flex: 1; color: #212121; }
.action-buttons { display: flex; justify-content: center; gap: 12px; margin-top: 16px; }
</style>