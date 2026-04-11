/*
 * Copyright (C) 2018-2026 Modding Craft ZBD Studio.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

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