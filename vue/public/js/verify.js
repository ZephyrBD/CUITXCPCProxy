// ====================== 功能代码完全不动 ======================
const examInput = document.getElementById('examInput');
const submitBtn = document.getElementById('submitBtn');
const statusModal = document.getElementById('statusModal');
const successModal = document.getElementById('successModal');
const rejectModal = document.getElementById('rejectModal');
const statusDetail = document.getElementById('statusDetail');
const teamInfo = document.getElementById('teamInfo');
const redirectBtn = document.getElementById('redirectBtn');
const countdownText = document.getElementById('countdownText');
const refreshStatusBtn = document.getElementById('refreshStatusBtn');

let pollingInterval = null;
let pollCount = 0;
const maxPolls = 3;
let currentExamNum = '';

const closeButtons = document.querySelectorAll('.md-modal-close');
closeButtons.forEach(btn => {
    btn.addEventListener('click', () => {
        stopPolling();
        statusModal.style.display = 'none';
        successModal.style.display = 'none';
        rejectModal.style.display = 'none';
    });
});

window.addEventListener('click', e => {
    if (e.target === statusModal || e.target === successModal || e.target === rejectModal) {
        stopPolling();
        statusModal.style.display = 'none';
        successModal.style.display = 'none';
        rejectModal.style.display = 'none';
    }
});

function stopPolling() {
    if (pollingInterval) clearInterval(pollingInterval);
    pollingInterval = null;
    pollCount = 0;
    refreshStatusBtn.style.display = 'none';
}

submitBtn.addEventListener('click', async () => {
    const examNum = examInput.value.trim();
    if (!examNum) { alert('请输入考号！'); return; }
    currentExamNum = examNum;
    try {
        const formData = new FormData();
        formData.append('exam_num', examNum);
        const res = await fetch('/cxtool/public/auth/verify', { method: 'POST', body: formData });
        const result = await res.json();
        if (result.code === 1) {
            if (result.data?.token) showSuccess(result.data);
            else startStatusPolling(examNum);
        } else {
            alert('认证请求失败: ' + result.msg);
        }
    } catch (e) {
        console.error(e);
        alert('网络请求失败');
    }
});

function startStatusPolling(examNum) {
    stopPolling();
    statusModal.style.display = 'block';
    let timeLeft = 10;
    updateStatusCountdown(timeLeft);

    pollingInterval = setInterval(async () => {
        timeLeft--;
        updateStatusCountdown(timeLeft);
        if (timeLeft <= 0) {
            pollCount++;
            try {
                const res = await fetch(`/cxtool/public/auth/verify?exam_num=${encodeURIComponent(examNum)}`);
                const result = await res.json();
                if (result.code === 1) {
                    const status = result.data;
                    if (typeof status === 'string') {
                        if (status === 'DONE') {
                            const dRes = await fetch(`/cxtool/public/auth/verify?exam_num=${encodeURIComponent(examNum)}`);
                            const dResult = await dRes.json();
                            if (dResult.code === 1 && dResult.data.token) {
                                stopPolling(); statusModal.style.display = 'none'; showSuccess(dResult.data);
                            }
                        } else if (status === 'REJECTED' || status === 'FAILED') {
                            stopPolling(); statusModal.style.display = 'none'; showRejection();
                        } else {
                            if (pollCount >= maxPolls) {
                                stopPolling(); statusDetail.textContent = '已完成最大查询次数'; refreshStatusBtn.style.display = 'block';
                            } else timeLeft = 10;
                        }
                    } else {
                        stopPolling(); statusModal.style.display = 'none'; showSuccess(status);
                    }
                } else {
                    if (pollCount >= maxPolls) {
                        stopPolling(); statusDetail.textContent = '查询失败，已达最大次数'; refreshStatusBtn.style.display = 'block';
                    } else timeLeft = 10;
                }
            } catch (e) {
                if (pollCount >= maxPolls) {
                    stopPolling(); statusDetail.textContent = '网络异常'; refreshStatusBtn.style.display = 'block';
                } else timeLeft = 10;
            }
        }
    }, 1000);

    refreshStatusBtn.onclick = async () => {
        refreshStatusBtn.disabled = true;
        refreshStatusBtn.textContent = '刷新中...';
        try {
            const res = await fetch(`/cxtool/public/auth/verify?exam_num=${encodeURIComponent(currentExamNum)}`);
            const result = await res.json();
            if (result.code === 1) {
                const s = result.data;
                if (typeof s === 'string') {
                    if (s === 'DONE') {
                        const dRes = await fetch(`/cxtool/public/auth/verify?exam_num=${encodeURIComponent(currentExamNum)}`);
                        const dResult = await dRes.json();
                        if (dResult.code === 1 && dResult.data.token) {
                            stopPolling(); statusModal.style.display = 'none'; showSuccess(dResult.data);
                        }
                    } else if (s === 'REJECTED') {
                        stopPolling(); statusModal.style.display = 'none'; showRejection();
                    }
                } else {
                    stopPolling(); statusModal.style.display = 'none'; showSuccess(s);
                }
            }
        } catch (e) {} finally {
            refreshStatusBtn.disabled = false;
            refreshStatusBtn.textContent = '刷新认证状态';
        }
    };
}

function updateStatusCountdown(s) {
    statusDetail.textContent = `下次查询: ${s}秒后 (已查询${pollCount}/${maxPolls}次)`;
}

function showSuccess(data) {
    teamInfo.innerHTML = `
        <div class="md-info-item"><strong>考号：</strong>${data.examNumber || '未知'}</div>
        <div class="md-info-item"><strong>队名：</strong>${data.teamName || '未知'}</div>
        <div class="md-info-item"><strong>账号：</strong>${data.account || '未知'}</div>
        <div class="md-info-item"><strong>密码：</strong>${data.password || '未知'}</div>
        <div class="md-info-item"><strong>位置：</strong>${data.position || '未知'}</div>
        <div class="md-info-item"><strong>登录时间：</strong>${data.loginTime ? new Date(data.loginTime).toLocaleString() : '未知'}</div>
    `;
    let c = 10;
    redirectBtn.disabled = true;
    redirectBtn.textContent = `跳转到在线判题系统 (${c})`;
    const timer = setInterval(() => {
        c--;
        redirectBtn.textContent = `跳转到在线判题系统 (${c})`;
        if (c <= 0) {
            clearInterval(timer);
            redirectBtn.disabled = false;
            redirectBtn.textContent = '跳转到在线判题系统';
            countdownText.textContent = '现在可以点击跳转';
            redirectBtn.onclick = e => {
                e.preventDefault();
                const url = `${data.djUrl || ''}?token=${encodeURIComponent(data.token || '')}`;
                if (url) window.location.replace(url);
                else alert('跳转地址无效');
            };
        }
    }, 1000);
    successModal.style.display = 'block';
}

function showRejection() {
    rejectModal.style.display = 'block';
}

window.addEventListener('beforeunload', stopPolling);