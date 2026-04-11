/**
 * 公共工具方法 - 获取系统版本号
 * 接口：/cxtool/public/version
 * 返回：如 Beta 1.0
 */
import axios from '../router/axios'

export async function getAppVersion() {
  try {
    const res = await fetch('/cxtool/public/version')
    const versionText = await res.text()
    return versionText ? `${versionText}` : 'Beta 1.0'
  } catch (e) {
    console.error('获取版本号失败', e)
    return '{Beta 1.0}'
  }
}

export const startNewContest = () => {
  return axios.delete(`/admin/new/contest`);
}