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