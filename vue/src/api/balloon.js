import axios from '../router/axios'

export const getBalloonTasks = (page) => {
  return axios.get(`/admin/balloon/task/page?cur_page=${page}`)
}

export const doneBalloonTask = (id) => {
  return axios.post(`/admin/balloon/task/${id}/done`)
}