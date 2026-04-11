<template>
  <el-dialog :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)" title="危险操作确认" width="400px" center>
    <div style="text-align: center;">
      <p class="danger-operation">危险：此操作会清空所有比赛数据！</p>
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