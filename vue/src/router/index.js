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
    path: '/urls/create',
    name: 'UrlCreate',
    component: () => import('../views/UrlCreate.vue')
  },
  {
    path: '/urls/bulk',
    name: 'BulkUrlCreate',
    component: () => import('../views/BulkUrlCreate.vue')
  },
  {
    path: '/api-docs',
    name: 'ApiDocs',
    component: ApiDocs,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/swagger',
    name: 'Swagger',
    component: () => import('../views/Swagger.vue')
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/Admin.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/teams',
    name: 'TeamManagement',
    component: () => import('../views/TeamManagement.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/api-keys',
    name: 'ApiKeyManagement',
    component: () => import('../views/ApiKeyManagement.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/white-label',
    name: 'WhiteLabel',
    component: () => import('../views/WhiteLabel.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/support',
    name: 'SupportTickets',
    component: () => import('../views/SupportTickets.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/account-manager',
    name: 'AccountManager',
    component: () => import('../views/AccountManager.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
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

// 라우터 가드: 인증이 필요한 페이지 체크
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('authToken')
  const userRole = localStorage.getItem('userRole')
  
  // 인증이 필요한 페이지인지 확인
  if (to.meta.requiresAuth) {
    if (!token) {
      // 로그인 페이지로 리다이렉트
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
    
    // 관리자 권한이 필요한 페이지인지 확인
    // admin 계정(username이 'admin')은 모든 페이지 접근 가능
    const isAdminUser = userRole === 'ADMIN' || localStorage.getItem('username') === 'admin'
    if (to.meta.requiresAdmin && !isAdminUser) {
      // 권한 없음 - 홈으로 리다이렉트
      alert('관리자 권한이 필요합니다.')
      next('/')
      return
    }
  }
  
  // 이미 로그인된 상태에서 로그인 페이지 접근 시
  if (to.name === 'Login' && token) {
    // 홈 또는 이전 페이지로 리다이렉트
    next(from.path || '/')
    return
  }
  
  next()
})

export default router

