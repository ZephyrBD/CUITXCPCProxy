import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import axios from './router/axios'

const app = createApp(App)
app.config.globalProperties.$axios = axios
app.use(router).use(ElementPlus).mount('#app')