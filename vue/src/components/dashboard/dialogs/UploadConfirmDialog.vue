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
  <el-dialog :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)" title="危险操作确认" width="400px" center>
    <div style="text-align: center;">
      <p class="danger-operation">危险：此操作会清空队伍信息！</p>
      <p>导入前请务必先点击开始新比赛，否则无法导入！</p>
      <p class="danger-operation">比赛中执行会导致系统异常！</p>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="$emit('cancel')">取消</el-button>
        <el-button type="danger" @click="$emit('confirm')" :disabled="countdown > 0">
          {{ countdown > 0 ? `确认 (${countdown}s)` : '确认' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  countdown: { type: Number, default: 0 }
})
const emit = defineEmits(['update:modelValue','confirm','cancel'])
</script>

<style scoped>
.danger-operation { color: #d32f2f; font-weight: 500; }
</style>