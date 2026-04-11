import axios from '../router/axios'

export const getTeams = (page) => {
  return axios.get(`/admin/team/page?cur_page=${page}`)
}

export const importTeams = (formData) => {
  return axios.post('/admin/team', formData, {
    headers: {'Content-Type': 'multipart/form-data'}
  })
}