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
  <div class="auth-verify-page">
    <div class="md-card">
      <div class="md-header">
        <div class="md-logo-wrapper">
          <img src="/cxtool/images/Logo.png" alt="软件图标" class="md-logo">
        </div>
        <h1 class="md-main-title">CUIT XCPC TOOL</h1>
        <p class="md-sub-title">需要验证你的客户端</p>
      </div>

      <div class="md-input-container">
        <input
          v-model="examNum"
          type="text"
          id="examInput"
          class="md-input"
          placeholder=" "
          required
        />
        <label class="md-input-label">请输入考号(一般为OMS上方居中的字符串)</label>
      </div>

      <button @click="submitAuth" class="md-btn">认证</button>

      <div class="copyright">
        <p>CUIT XCPC TOOL {{version}}</p>
        <p>Copyright © DSA Laboratory</p>
        <p>Designed and Powered By ZephyrBD</p>
      </div>
    </div>

    <div v-show="showStatusModal" class="md-modal">
      <div class="md-modal-content">
        <span @click="closeAllModal" class="md-modal-close">&times;</span>
        <h2 style="font-size: 20px; font-weight: 500;">认证状态</h2>
        <p class="md-status-text">正在等待管理员审核...</p>
        <p class="md-status-text">{{ statusDetail }}</p>
        <button
          v-show="showRefreshBtn"
          @click="refreshStatus"
          :disabled="refreshing"
          class="md-btn md-refresh-btn"
        >
          {{ refreshing ? '刷新中...' : '刷新认证状态' }}
        </button>
      </div>
    </div>

    <!-- 成功模态框 -->
    <div v-show="showSuccessModal" class="md-modal">
      <div class="md-modal-content">
        <span @click="closeAllModal" class="md-modal-close">&times;</span>
        <h2 style="font-size: 20px; font-weight: 500;">认证成功</h2>
        <p class="md-highlight">
          注意：务必进行以下操作！！！
          <br />你把账号和密码复制到任意文本软件中，或抄写在草稿纸上！
          <br />没有账号密码你将无法登录后续的判题系统！
        </p>
        <div class="md-team-info" v-html="teamInfoHtml"></div>
        <button
          :disabled="redirectDisabled"
          @click="redirectToDj"
          class="md-btn md-redirect-btn"
        >
          {{ redirectBtnText }}
        </button>
        <div class="md-countdown" v-show="redirectDisabled">
          按钮将在倒计时结束后可用
        </div>
      </div>
    </div>

    <!-- 拒绝模态框 -->
    <div v-show="showRejectModal" class="md-modal">
      <div class="md-modal-content">
        <span @click="closeAllModal" class="md-modal-close">&times;</span>
        <h2 style="font-size: 20px; font-weight: 500;">认证被拒绝</h2>
        <p class="md-error-message">
          您的认证请求已被拒绝，请检查是否非法登录。
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import {onMounted, onUnmounted, ref} from 'vue'
import {getAppVersion} from '@/api/utils'

// 响应式数据
const examNum = ref('')
const showStatusModal = ref(false)
const showSuccessModal = ref(false)
const showRejectModal = ref(false)
const statusDetail = ref('')
const teamInfoHtml = ref('')
const redirectBtnText = ref('跳转到在线判题系统 (10)')
const redirectDisabled = ref(true)
const showRefreshBtn = ref(false)
const refreshing = ref(false)
const version = ref('')

// 轮询变量
let pollingInterval = null
let pollCount = 0
const maxPolls = 3
let currentExamNum = ''
let successData = null

onMounted(async () => {
  version.value = await getAppVersion()
})

// 关闭所有弹窗
const closeAllModal = () => {
  showStatusModal.value = false
  showSuccessModal.value = false
  showRejectModal.value = false
  stopPolling()
}

// 停止轮询
const stopPolling = () => {
  if (pollingInterval) clearInterval(pollingInterval)
  pollingInterval = null
  pollCount = 0
  showRefreshBtn.value = false
}

// 提交认证
const submitAuth = async () => {
  const num = examNum.value.trim()
  if (!num) {
    alert('请输入考号！')
    return
  }
  currentExamNum = num
  try {
    const formData = new FormData()
    formData.append('exam_num', num)
    const res = await fetch('/cxtool/public/auth/verify', {
      method: 'POST',
      body: formData
    })
    const result = await res.json()
    if (result.code === 1) {
      if (result.data?.token) {
        showSuccess(result.data)
      } else {
        startStatusPolling(num)
      }
    } else {
      alert('认证请求失败: ' + result.msg)
    }
  } catch (e) {
    console.error(e)
    alert('网络请求失败')
  }
}

// 开始状态轮询
const startStatusPolling = (examNum) => {
  stopPolling()
  showStatusModal.value = true
  let timeLeft = 10
  updateStatusCountdown(timeLeft)

  pollingInterval = setInterval(async () => {
    timeLeft--
    updateStatusCountdown(timeLeft)
    if (timeLeft <= 0) {
      pollCount++
      try {
        const res = await fetch(`/cxtool/public/auth/verify?exam_num=${encodeURIComponent(examNum)}`)
        const result = await res.json()
        if (result.code === 1) {
          const status = result.data
          if (typeof status === 'string') {
            if (status === 'DONE') {
              const dRes = await fetch(`/cxtool/public/auth/verify?exam_num=${encodeURIComponent(examNum)}`)
              const dResult = await dRes.json()
              if (dResult.code === 1 && dResult.data.token) {
                stopPolling()
                showStatusModal.value = false
                showSuccess(dResult.data)
              }
            } else if (status === 'REJECTED' || status === 'FAILED') {
              stopPolling()
              showStatusModal.value = false
              showRejectModal.value = true
            } else {
              timeLeft = pollCount >= maxPolls ? 0 : 10
              if (pollCount >= maxPolls) {
                statusDetail.value = '已完成最大查询次数'
                showRefreshBtn.value = true
              }
            }
          } else {
            stopPolling()
            showStatusModal.value = false
            showSuccess(status)
          }
        }
      } catch (e) {
        if (pollCount >= maxPolls) {
          statusDetail.value = '网络异常'
          showRefreshBtn.value = true
        } else {
          timeLeft = 10
        }
      }
    }
  }, 1000)
}

// 更新倒计时
const updateStatusCountdown = (s) => {
  statusDetail.value = `下次查询: ${s}秒后 (已查询${pollCount}/${maxPolls}次)`
}

// 手动刷新状态
const refreshStatus = async () => {
  if (!currentExamNum) return
  refreshing.value = true
  try {
    const res = await fetch(`/cxtool/public/auth/verify?exam_num=${encodeURIComponent(currentExamNum)}`)
    const result = await res.json()
    if (result.code === 1) {
      const s = result.data
      if (typeof s === 'string') {
        if (s === 'DONE') {
          const dRes = await fetch(`/cxtool/public/auth/verify?exam_num=${encodeURIComponent(currentExamNum)}`)
          const dResult = await dRes.json()
          if (dResult.code === 1 && dResult.data.token) {
            stopPolling()
            showStatusModal.value = false
            showSuccess(dResult.data)
          }
        } else if (s === 'REJECTED') {
          stopPolling()
          showStatusModal.value = false
          showRejectModal.value = true
        }
      } else {
        stopPolling()
        showStatusModal.value = false
        showSuccess(s)
      }
    }
  } catch (e) {} finally {
    refreshing.value = false
  }
}

// 显示成功
const showSuccess = (data) => {
  successData = data
  teamInfoHtml.value = `
    <div class="md-info-item"><strong>考号：</strong>${data.examNumber || '未知'}</div>
    <div class="md-info-item"><strong>队名：</strong>${data.teamName || '未知'}</div>
    <div class="md-info-item"><strong>账号：</strong>${data.account || '未知'}</div>
    <div class="md-info-item"><strong>密码：</strong>${data.password || '未知'}</div>
    <div class="md-info-item"><strong>位置：</strong>${data.position || '未知'}</div>
    <div class="md-info-item"><strong>登录时间：</strong>${data.loginTime ? new Date(data.loginTime).toLocaleString() : '未知'}</div>
  `
  showSuccessModal.value = true

  let c = 10
  redirectDisabled.value = true
  redirectBtnText.value = `跳转到在线判题系统 (${c})`
  const timer = setInterval(() => {
    c--
    redirectBtnText.value = `跳转到在线判题系统 (${c})`
    if (c <= 0) {
      clearInterval(timer)
      redirectDisabled.value = false
      redirectBtnText.value = '跳转到在线判题系统'
    }
  }, 1000)
}

// 跳转判题系统
const redirectToDj = () => {
  if (!successData) return
  const url = `${successData.djUrl || ''}?token=${encodeURIComponent(successData.token || '')}`
  if (url) window.location.replace(url)
  else alert('跳转地址无效')
}

// 页面卸载时停止轮询
onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
/* Material Design 3 样式（全部内联，无外部文件） */
:root {
  --md-primary: #2196F3;
  --md-primary-dark: #1976D2;
  --md-surface: #FFFFFF;
  --md-background: #F5F7FA;
  --md-text-primary: #1D1B20;
  --md-text-secondary: #49454F;
  --md-text-hint: #79747E;
  --md-outline: #79747E;
  --md-shadow-1: 0 1px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.24);
  --md-shadow-2: 0 3px 6px rgba(0,0,0,0.16), 0 3px 6px rgba(0,0,0,0.23);
  --md-radius: 12px;
  --md-transition: 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: "Roboto", "Microsoft Yahei", sans-serif;
}

.auth-verify-page {
  background-color: var(--md-background);
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 16px;
}

.md-card {
  width: 100%;
  max-width: 420px;
  background: var(--md-surface);
  padding: 40px 32px;
  border-radius: var(--md-radius);
  box-shadow: var(--md-shadow-2);
  transition: box-shadow var(--md-transition);
}

.md-card:hover {
  box-shadow: 0 10px 20px rgba(0,0,0,0.15);
}

.md-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 32px;
  gap: 8px;
}
/* 1:1 固定大小图标容器（核心：严格固定宽高） */
.md-logo-wrapper {
  width: 80px;    /* 固定宽度 */
  height: 80px;   /* 固定高度 = 宽度，严格1:1 */
  margin-bottom: 16px;
  overflow: hidden;
  border-radius: 12px; /* 图标圆角（可删除） */
}
/* 图片自适应容器，不变形 */
.md-logo {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 保持图片比例，填满容器 */
  /* 透明图标用：object-fit: contain; */
}
/* 主标题单独一行 */
.md-main-title {
  color: var(--md-text-primary);
  font-size: 24px;
  font-weight: 500;
  letter-spacing: 0.1px;
  text-align: center;
}
/* 副标题单独一行 */
.md-sub-title {
  color: var(--md-text-secondary);
  font-size: 16px;
  font-weight: 400;
  text-align: center;
}

.md-input-container {
  position: relative;
  margin-bottom: 28px;
}

.md-input {
  width: 100%;
  height: 56px;
  padding: 0 16px;
  border: 1px solid var(--md-outline);
  border-radius: 8px;
  font-size: 16px;
  color: var(--md-text-primary);
  background: transparent;
  outline: none;
  transition: all var(--md-transition);
  position: relative;
  z-index: 1;
}

.md-input:focus {
  border-color: var(--md-primary);
  border-width: 2px;
}

.md-input-label {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--md-text-hint);
  font-size: 16px;
  transition: all var(--md-transition);
  background-color: var(--md-surface);
  padding: 0 4px;
  z-index: 2;
  pointer-events: none;
}

.md-input:focus + .md-input-label,
.md-input:not(:placeholder-shown) + .md-input-label {
  top: 0;
  transform: translateY(-50%);
  font-size: 12px;
  color: var(--md-primary);
}

.md-btn {
  width: 100%;
  height: 48px;
  background-color: var(--md-primary);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  letter-spacing: 0.5px;
  box-shadow: var(--md-shadow-1);
  transition: all var(--md-transition);
}

.md-btn:hover {
  background-color: var(--md-primary-dark);
  box-shadow: var(--md-shadow-2);
}

.md-btn:active {
  transform: scale(0.98);
}

.md-btn:disabled {
  background-color: #BDBDBD;
  color: var(--md-text-hint);
  cursor: not-allowed;
  box-shadow: none;
}

.copyright {
  margin-top: 32px;
  text-align: center;
  color: var(--md-text-hint);
  font-size: 12px;
  line-height: 1.6;
}

.copyright p {
  margin: 4px 0;
}

.md-modal {
  display: flex;
  position: fixed;
  z-index: 1000;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.5);
  backdrop-filter: blur(2px);
  align-items: center;
  justify-content: center;
}

.md-modal-content {
  background-color: white;
  padding: 28px;
  border-radius: var(--md-radius);
  width: 90%;
  max-width: 420px;
  text-align: center;
  position: relative;
  box-shadow: var(--md-shadow-2);
  animation: modalFadeIn 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
}

@keyframes modalFadeIn {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

.md-modal-close {
  position: absolute;
  right: 16px;
  top: 16px;
  font-size: 24px;
  cursor: pointer;
  color: var(--md-text-hint);
}

.md-modal-close:hover {
  color: var(--md-text-primary);
}

.md-status-text {
  margin: 20px 0;
  font-size: 16px;
  color: var(--md-text-primary);
}

.md-highlight {
  background-color: #FFF8E1;
  padding: 16px;
  border-radius: 8px;
  margin: 16px 0;
  border-left: 4px solid #FFC107;
  text-align: left;
  color: var(--md-text-primary);
  font-size: 14px;
  line-height: 1.5;
}

.md-team-info {
  width: 100%;
  text-align: left;
  margin: 16px 0;
  padding: 16px;
  background: #FAFAFA;
  border-radius: 8px;
  border: 1px solid #E0E0E0;
}

.md-info-item {
  margin: 8px 0;
  font-size: 15px;
}

.md-info-item strong {
  color: var(--md-text-secondary);
  display: inline-block;
  width: 70px;
}

.md-error-message {
  color: #D32F2F;
  margin: 16px 0;
  font-weight: 500;
}

.md-countdown {
  margin-top: 12px;
  font-size: 13px;
  color: var(--md-text-hint);
}

.md-refresh-btn,
.md-redirect-btn {
  padding: 0 24px;
  height: 42px;
  margin-top: 12px;
  width: auto;
}
</style>