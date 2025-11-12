import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import UrlList from '../views/UrlList.vue'
import ApiDocs from '../views/ApiDocs.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/urls',
    name: 'UrlList',
    component: UrlList
  },
  {
    path: '/api-docs',
    name: 'ApiDocs',
    component: ApiDocs
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/Admin.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/stats/:shortCode',
    name: 'UrlStats',
    component: () => import('../views/UrlStats.vue')
  },
  {
    path: '/guide',
    name: 'Guide',
    component: () => import('../views/Guide.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

