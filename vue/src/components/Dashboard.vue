<template>
  <div class="app-container">
    <div class="header-section">
      <div class="header-content">
        <div class="title-section">
          <h1>DashBoard</h1>
          <p>CUIT XCPC TOOL {{appversion}}</p>
          <p>Copyright © DSA Laboratory</p>
          <p>Designed and Powered By ZephyrBD</p>
          <div class="header-buttons">
            <button class="domjudge-btn" @click="gotoDomjudge">转到 Domjudge</button>
            <button class="new-contest-btn" @click="showNewContestDialog">开始新比赛</button>
            <button class="logout-btn" @click="handleLogout">登出系统</button>
          </div>
        </div>

        <div class="header-logo-container">
          <img src="/cxtool/images/Logo.png" class="header-logo">
          <img src="/cxtool/images/DSALogo.png" class="header-logo">
          <img src="/cxtool/images/MCZSLogo.png" class="header-logo">
        </div>
      </div>
    </div>

    <div class="main-content">
      <el-tabs v-model="activeTab" type="card" class="tab-container">
        <el-tab-pane label="登录验证" name="auth">
          <AuthTab
            :auth-table-data="authTableData"
            :auth-current-page="authCurrentPage"
            :auth-page-size="authPageSize"
            :auth-total="authTotal"
            @refresh="fetchAuthTasks"
            @page-change="handleAuthPageChange"
            @accept="handleAcceptAuth"
            @deny="handleDenyAuth"
          />
        </el-tab-pane>

        <el-tab-pane label="打印任务" name="print">
          <PrintTab
            :print-table-data="printTableData"
            :print-current-page="printCurrentPage"
            :print-page-size="printPageSize"
            :print-total="printTotal"
            @refresh="fetchPrintTasks"
            @page-change="handlePrintPageChange"
            @start-auto="startAutoPrint"
            @download="handleDownloadPdf"
            @print="handleManualPrint"
          />
        </el-tab-pane>

        <el-tab-pane label="气球任务" name="balloon">
          <BalloonTab
            :balloon-table-data="balloonTableData"
            :balloon-current-page="balloonCurrentPage"
            :balloon-page-size="balloonPageSize"
            :balloon-total="balloonTotal"
            @refresh="fetchBalloonTasks"
            @page-change="handleBalloonPageChange"
            @start-auto="startAutoBalloon"
            @print="handleBalloonPrint"
          />
        </el-tab-pane>

        <el-tab-pane label="队伍信息" name="teams">
          <!-- 🔥 核心修复：修改子组件事件为 file-upload -->
          <TeamsTab
            :teams-table-data="teamsTableData"
            :teams-current-page="teamsCurrentPage"
            :teams-page-size="teamsPageSize"
            :teams-total="teamsTotal"
            @refresh="fetchTeams"
            @page-change="handleTeamsPageChange"
            @file-upload="handleFileUpload"
          />
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 隐藏的文件选择框（无需显示，仅备用） -->
    <input type="file" ref="fileInputRef" style="display: none;" accept=".xlsx">

    <AuthTaskDialog v-model="authTaskDialogVisible" :task="currentAuthTask" @accept="handleAcceptAuth" @deny="handleDenyAuth"/>
    <NewContestDialog v-model="newContestDialogVisible" :countdown="newContestCountdown" @confirm="confirmNewContest" @cancel="cancelNewContest"/>
    <UploadConfirmDialog v-model="uploadConfirmDialogVisible" :countdown="uploadConfirmCountdown" :file-name="selectedFileName" @confirm="confirmUpload" @cancel="cancelUpload"/>
    <AutoPrintDialog v-model="autoPrintWindowVisible" :task="currentPrintTask" :status="printStatus" @stop="stopAutoPrint"/>
    <AutoBalloonDialog v-model="autoBalloonWindowVisible" :task="currentBalloonTask" :status="balloonStatus" @stop="stopAutoBalloon"/>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElLoading, ElMessageBox } from 'element-plus'
import api from '@/api'
import { formatDateTime } from '@/utils/date'
import { printByIframe, printBalloonTxt } from '@/utils/print'
import { getAppVersion, startNewContest } from '../api/utils'

import AuthTab from './dashboard/tabs/AuthTab.vue'
import PrintTab from './dashboard/tabs/PrintTab.vue'
import BalloonTab from './dashboard/tabs/BalloonTab.vue'
import TeamsTab from './dashboard/tabs/TeamsTab.vue'
import AuthTaskDialog from './dashboard/dialogs/AuthTaskDialog.vue'
import NewContestDialog from './dashboard/dialogs/NewContestDialog.vue'
import UploadConfirmDialog from './dashboard/dialogs/UploadConfirmDialog.vue'
import AutoPrintDialog from './dashboard/dialogs/AutoPrintDialog.vue'
import AutoBalloonDialog from './dashboard/dialogs/AutoBalloonDialog.vue'
import axios from 'axios'

const activeTab = ref('auth');
const globalSSE = ref(null);

const authTableData = ref([]);
const authCurrentPage = ref(1);
const authPageSize = ref(10);
const authTotal = ref(0);
const authTaskDialogVisible = ref(false);
const currentAuthTask = ref({});

const printTableData = ref([]);
const printCurrentPage = ref(1);
const printPageSize = ref(10);
const printTotal = ref(0);
const autoPrintWindowVisible = ref(false);
const currentPrintTask = ref(null);
const printStatus = ref(null);

const balloonTableData = ref([]);
const balloonCurrentPage = ref(1);
const balloonPageSize = ref(10);
const balloonTotal = ref(0);
const autoBalloonWindowVisible = ref(false);
const currentBalloonTask = ref(null);
const balloonStatus = ref(null);

const teamsTableData = ref([]);
const teamsCurrentPage = ref(1);
const teamsPageSize = ref(10);
const teamsTotal = ref(0);

const newContestDialogVisible = ref(false);
const newContestCountdown = ref(0);
let newContestCountdownTimer = null;
const uploadConfirmDialogVisible = ref(false);
const uploadConfirmCountdown = ref(0);
let uploadConfirmCountdownTimer = null;
const selectedFile = ref(null);
const selectedFileName = ref('');
const appversion = ref('');

onMounted(async () => {
  appversion.value = await getAppVersion()
})

const fetchAuthTasks = async () => {
  const r=await api.auth.getAuthTasks(authCurrentPage.value);
  if(r.data.code===1){
    authTableData.value=r.data.data.records;
    authTotal.value=r.data.data.total;
  }
};
const fetchPrintTasks = async () => {
  const r=await api.print.getPrintTasks(printCurrentPage.value);
  if(r.data.code===1){printTableData.value=r.data.data.records;printTotal.value=r.data.data.total;}
};
const fetchBalloonTasks = async () => {
  const r=await api.balloon.getBalloonTasks(balloonCurrentPage.value);
  if(r.data.code===1){balloonTableData.value=r.data.data.records;balloonTotal.value=r.data.data.total;}
};
const fetchTeams = async () => {
  const r = await api.team.getTeams(teamsCurrentPage.value);
  if(r.data.code === 1){
    teamsTableData.value = r.data.data.records;
    teamsTotal.value = r.data.data.total;
  }
}

const connectGlobalSse = () => {
  if (globalSSE.value) globalSSE.value.close();
  const token = localStorage.getItem('token')
  globalSSE.value = new EventSource(`/cxtool/admin/sse/connect?token=${token}`)
  globalSSE.value.addEventListener('connect', () => console.log('SSE连接成功'));
  globalSSE.value.addEventListener('heartbeat', () => {});
  globalSSE.value.addEventListener('authTask', (e) => {
    try {
      const res = JSON.parse(e.data);
      const task = res.data?.[0];
      if (task) { 
        currentAuthTask.value = task;
        if(task.status!='AUTO_DONE'){
          authTaskDialogVisible.value = true; 
        }
        fetchAuthTasks(); 
      }
    } catch {}
  });
  globalSSE.value.addEventListener('printTask', async (e) => {
    try {
      const res = JSON.parse(e.data);
      const task = res.data?.[0];
      if (autoPrintWindowVisible.value && task) {
        currentPrintTask.value = task;
        printStatus.value = { type: 'loading', message: '正在处理打印任务...' };
        await api.print.donePrintTask(task.taskId);
        const pdf = await api.print.downloadPrintPdf(task.taskId);
        printByIframe(pdf.data);
        printStatus.value = { type: 'success', message: '自动打印完成' };
        setTimeout(() => { currentPrintTask.value = null; printStatus.value = null; }, 3000);
      }
    } catch (err) {
      if(autoPrintWindowVisible.value) printStatus.value = { type: 'error', message: '打印失败' };
    } finally { fetchPrintTasks(); }
  });
  globalSSE.value.addEventListener('balloonTask', async (e) => {
    try {
      const res = JSON.parse(e.data);
      const task = res.data;
      if (autoBalloonWindowVisible.value && task) {
        currentBalloonTask.value = task;
        balloonStatus.value = { type: 'loading', message: '正在自动打印气球小票...' };
        printBalloonTxt(task, formatDateTime);
        await api.balloon.doneBalloonTask(task.balloonId);
        balloonStatus.value = { type: 'success', message: '气球小票自动打印完成' };
        setTimeout(() => { currentBalloonTask.value = null; balloonStatus.value = null; }, 3000);
      }
    } catch (err) {
      if(autoBalloonWindowVisible.value) balloonStatus.value = { type: 'error', message: '小票打印失败' };
    } finally { fetchBalloonTasks(); }
  });
  globalSSE.value.onerror = () => { globalSSE.value.close(); setTimeout(connectGlobalSse, 3000); };
};

const startAutoPrint = () => { autoPrintWindowVisible.value = true; ElMessage.success('自动打印已开启'); };
const stopAutoPrint = () => { autoPrintWindowVisible.value = false; currentPrintTask.value = null; printStatus.value = null; ElMessage.success('自动打印已停止'); };
const startAutoBalloon = () => { autoBalloonWindowVisible.value = true; ElMessage.success('自动气球打印已开启'); };
const stopAutoBalloon = () => { autoBalloonWindowVisible.value = false; currentBalloonTask.value = null; balloonStatus.value = null; ElMessage.success('自动气球打印已停止'); };

const handleAuthPageChange = (p) => { authCurrentPage.value = p; fetchAuthTasks(); };
const handlePrintPageChange = (p) => { printCurrentPage.value = p; fetchPrintTasks(); };
const handleBalloonPageChange = (p) => { balloonCurrentPage.value = p; fetchBalloonTasks(); };
const handleTeamsPageChange = (p) => { teamsCurrentPage.value = p; fetchTeams(); };

const handleAcceptAuth = async (id) => {
  await api.auth.acceptAuth(id); fetchAuthTasks(); authTaskDialogVisible.value = false; ElMessage.success('已同意');
};
const handleDenyAuth = async (id) => {
  await api.auth.denyAuth(id); fetchAuthTasks(); authTaskDialogVisible.value = false; ElMessage.success('已拒绝');
};
const handleDownloadPdf = async (id) => {
  const r=await api.print.downloadPrintPdf(id);
  const a=document.createElement('a');a.href=URL.createObjectURL(new Blob([r.data]));a.download=`task_${id}.pdf`;a.click();URL.revokeObjectURL(a.href);
};
const handleManualPrint = async (row) => {
  const l=ElLoading.service({lock:true,text:'打印中...'});
  try{
    await api.print.donePrintTask(row.taskId);
    const r=await api.print.downloadPrintPdf(row.taskId);
    printByIframe(r.data);fetchPrintTasks();ElMessage.success('打印成功');
  }finally{l.close();}
};
const handleBalloonPrint = async (row) => {
  const l=ElLoading.service({lock:true,text:'打印中...'});
  try{
    printBalloonTxt(row, formatDateTime);
    await api.balloon.doneBalloonTask(row.balloonId);
    fetchBalloonTasks();ElMessage.success('打印成功');
  }catch{ElMessage.error('失败');}finally{l.close();}
};

const showNewContestDialog = () => { newContestDialogVisible.value=true; newContestCountdown.value=10; newContestCountdownTimer=setInterval(()=>newContestCountdown.value--,1000); };
const cancelNewContest = () => { clearInterval(newContestCountdownTimer); newContestDialogVisible.value=false; newContestCountdown.value=0; };
const confirmNewContest = async () => {
  cancelNewContest();
  const l=ElLoading.service({lock:true,text:'初始化中...'});
  try{
    await startNewContest();
    fetchAuthTasks();fetchPrintTasks();ElMessage.success('完成');
  }catch{ElMessage.error('失败');}finally{l.close();}
};

const gotoDomjudge = async () => {
  const l=ElLoading.service({lock:true,text:'获取中...',target:'.header-section'});
  try{
    const r=await api.auth.getDomjudgeUrl();
    if(r.data.code===1)window.open(`${r.data.data.url}?token=${r.data.data.token}`,'_blank');
  }catch{ElMessage.error('失败');}finally{l.close();}
};

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '提示',
      {
        confirmButtonText: '确定登出',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await axios.get('/admin/logout') 
    localStorage.removeItem('token')
    if (globalSSE.value) globalSSE.value.close()
    ElMessage.success('登出成功')
    window.location.href = '/cxtool/login'
  } catch (error) {
    ElMessage.info('已取消登出')
  }
}

// 🔥 核心修复：接收子组件上传的文件，处理上传逻辑
const handleFileUpload = (file) => {
  if (!file.name.endsWith('.xlsx')) {
    ElMessage.error('仅支持 .xlsx 格式文件！');
    return;
  }
  selectedFile.value = file;
  selectedFileName.value = file.name;
  showUploadConfirmDialog();
};

const showUploadConfirmDialog = () => {
  uploadConfirmDialogVisible.value=true;uploadConfirmCountdown.value=10;uploadConfirmCountdownTimer=setInterval(()=>uploadConfirmCountdown.value--,1000);
};
const cancelUpload = () => { clearInterval(uploadConfirmCountdownTimer);uploadConfirmDialogVisible.value=false;selectedFile.value=null; };
const confirmUpload = async () => {
  clearInterval(uploadConfirmCountdownTimer);uploadConfirmDialogVisible.value=false;
  const f=new FormData();f.append('file',selectedFile.value);
  try{
    const r=await api.team.importTeams(f);
    r.data.code===1?ElMessage.success('导入成功'):ElMessage.error('导入失败');
    fetchTeams();
  }catch{ElMessage.error('导入失败');}
};


onMounted(() => { fetchAuthTasks();fetchPrintTasks();fetchBalloonTasks();fetchTeams();connectGlobalSse(); });
onUnmounted(() => { globalSSE.value?.close(); })
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Roboto', sans-serif;
}

body {
  background: #f5f5f5;
  color: #212121;
  line-height: 1.6;
}

.app-container {
  min-height: 100vh;
  padding: 16px;
}

.header-section {
  background: #1976d2;
  color: white;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1), 0 4px 8px rgba(0,0,0,0.08);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.title-section h1 {
  font-size: 22px;
  font-weight: 500;
  margin-bottom: 4px;
}

.title-section p {
  font-size: 14px;
  opacity: 0.92;
  font-weight: 400;
}

.header-buttons {
  display: flex;
  gap: 12px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.logout-btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #ff9800;
  color: white;
}
.logout-btn:hover {
  background: #f57c00;
}

.domjudge-btn,
.new-contest-btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.domjudge-btn {
  background: rgba(255,255,255,0.16);
  color: white;
}

.domjudge-btn:hover {
  background: rgba(255,255,255,0.24);
}

.new-contest-btn {
  background: #d32f2f;
  color: white;
}

.new-contest-btn:hover {
  background: #b72020;
}

.header-logo-container {
  display: flex;
  gap: 16px;
  align-items: center;
}
.header-logo {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  background: rgba(255,255,255,0.9);
  padding: 4px;
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
}

.tab-container {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.06), 0 4px 12px rgba(0,0,0,0.04);
  overflow: hidden;
}

@media (max-width: 768px) {
  .app-container {
    padding: 8px;
  }
  .header-logo {
    width: 60px;
    height: 60px;
  }
}
</style>