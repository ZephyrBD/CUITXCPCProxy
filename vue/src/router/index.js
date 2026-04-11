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

import {createRouter, createWebHashHistory} from 'vue-router'
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