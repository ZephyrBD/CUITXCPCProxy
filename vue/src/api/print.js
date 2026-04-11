import axios from '../router/axios'

export const getPrintTasks = (page) => {
  return axios.get(`/admin/print/task/page?cur_page=${page}`)
}

export const donePrintTask = (id) => {
  return axios.post(`/admin/print/task/done?task_id=${id}`)
}

export const downloadPrintPdf = (id) => {
  return axios({
    method: 'GET',
    url: `/admin/print/task/${id}/download`,
    responseType: 'blob'
  })
}