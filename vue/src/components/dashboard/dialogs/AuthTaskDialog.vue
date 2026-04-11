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
  <el-dialog :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)" title="新的登录请求" width="400px" center>
    <div style="padding: 8px 0;">
      <p><strong>队伍名称:</strong> {{ task?.teamName || '-' }}</p>
      <p><strong>考号:</strong> {{ task?.examNum || '-' }}</p>
      <p><strong>登录时间:</strong> {{ task?.loginTime || '-' }}</p>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="$emit('deny', task?.taskId)">拒绝</el-button>
        <el-button type="primary" @click="$emit('accept', task?.taskId)">同意</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  task: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['update:modelValue','accept','deny'])
</script>