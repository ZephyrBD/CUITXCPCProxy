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
  <div class="login-wrapper">
    <div class="login-box">
        <div class="md-logo-wrapper">
          <img src="/cxtool/images/Logo.png" alt="软件图标" class="md-logo">
        </div>
      <h3 class="md-main-title">CUIT XCPC TOOL</h3>
      <h1 class="dashboard-title">Admin Dashboard</h1>
      <div class="input-item">
        <input type="text" v-model="examNum" placeholder=" " required>
        <label>账号</label>
      </div>
      <div class="input-item">
        <input type="password" v-model="pwd" placeholder=" " required>
        <label>密码</label>
      </div>
      <button class="login-btn" @click="login()">立即登录</button>

      <div class="copyright">
        <p>CUIT XCPC TOOL {{version}}</p>
        <p>Copyright © DSA Laboratory</p>
        <p>Designed and Powered By ZephyrBD</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import axios from 'axios'
import {ElMessage} from 'element-plus'
import {getAppVersion} from '@/api/utils'

const examNum = ref('')
const pwd = ref('')
const router = useRouter()
const version = ref('')

onMounted(async () => {
  version.value = await getAppVersion()
})

const login = async () => {
  const username = examNum.value.trim()
  const password = pwd.value.trim()

  if (!username || !password) {
    ElMessage.warning('账号和密码不能为空！')
    return
  }

  try {
    const res = await axios.get('/admin/login', {
      params: { userName: username, password: password }
    })

    if (res.data.code === 1) {
      const token = res.data.data
      localStorage.setItem('token', token)
      ElMessage.success('登录成功！')
      router.push('/dashboard')
    } else {
      ElMessage.error('账号或密码错误')
    }
  } catch (error) {
    ElMessage.error('登录失败')
  }
}
</script>

<style>
:root {
  --md-primary: #2196F3;
  --md-primary-dark: #1976D2;
  --md-primary-container: #E3F2FD;
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

.login-wrapper {
  background-color: var(--md-background);
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 16px;
}

/* ✅ 核心修复：添加弹性布局，让所有内容水平居中 */
.login-box {
  width: 100%;
  max-width: 420px;
  background: var(--md-surface);
  padding: 40px 32px;
  border-radius: var(--md-radius);
  box-shadow: var(--md-shadow-2);
  transition: box-shadow var(--md-transition);
  display: flex;
  flex-direction: column;
  align-items: center; /* 水平居中所有子元素 */
}

.login-box:hover {
  box-shadow: 0 10px 20px rgba(0,0,0,0.15);
}

/* 管理后台标题样式 */
.dashboard-title {
  font-size: 18px;
  color: var(--md-text-secondary);
  font-weight: 500;
  margin: 8px 0 32px 0;
  text-align: center;
}

.input-item {
  margin-bottom: 28px;
  position: relative;
  width: 100%; /* 输入框占满宽度 */
}

.input-item input {
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

.input-item input:focus {
  border-color: var(--md-primary);
  border-width: 2px;
}

.input-item label {
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

.input-item input:focus + label,
.input-item input:not(:placeholder-shown) + label {
  top: 0;
  transform: translateY(-50%);
  font-size: 12px;
  color: var(--md-primary);
}

.md-logo-wrapper {
  width: 80px;
  height: 80px;
  margin-bottom: 16px;
  overflow: hidden;
  border-radius: 12px;
}
.md-logo {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.md-main-title {
  color: var(--md-text-primary);
  font-size: 24px;
  font-weight: 500;
  letter-spacing: 0.1px;
  text-align: center;
}

.login-btn {
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
  position: relative;
  overflow: hidden;
}

.login-btn:hover {
  background-color: var(--md-primary-dark);
  box-shadow: var(--md-shadow-2);
}

.login-btn:active {
  transform: scale(0.98);
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
</style>