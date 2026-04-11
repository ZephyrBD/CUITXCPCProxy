import { createRouter, createWebHashHistory } from 'vue-router'
import Login from '../components/Login.vue'
import Dashboard from '../components/Dashboard.vue'
import AuthVerify from '../components/AuthVerify.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  { 
    path: '/login', 
    component: Login,
    meta: { title: 'Login - CUIT XCPC TOOL' }
  },
  {
    path: '/auth',
    component: AuthVerify,
    meta: { title: 'CUIT XCPC TOOL IS VERIFYING YOUR CLIENT!!!'}
  },
  { 
    path: '/dashboard', 
    component: Dashboard,
    meta: { requiresAuth: true , title: 'Dashboard - CUIT XCPC TOOL'}
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  base: '/cxtool/'
})

router.beforeEach((to, from, next) => {
    if (to.meta.title) {
    document.title = to.meta.title
  }

  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router