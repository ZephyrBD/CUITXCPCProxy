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