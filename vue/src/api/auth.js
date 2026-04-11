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

import axios from '../router/axios'

export const getAuthTasks = (page) => {
  return axios.get(`/admin/auth/task/page?cur_page=${page}`)
}

export const acceptAuth = (id) => {
  return axios.post(`/admin/auth/task/accept?task_id=${id}`)
}

export const denyAuth = (id) => {
  return axios.post(`/admin/auth/task/deny?task_id=${id}`)
}

export const getDomjudgeUrl = () => {
  return axios.get('/admin/auth/domjudge')
}