import axios from "axios"

axios.defaults.baseURL = '/cxtool'
axios.defaults.timeout = 10000

axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = token
  }
  return config
})

axios.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/cxtool/login'
    }
    return Promise.reject(err)
  }
)

export default axios