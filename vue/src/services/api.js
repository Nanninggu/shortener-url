import axios from 'axios'

// 환경별 API Base URL 설정
// Local: /api (프록시 사용)
// Prod: http://223.130.157.227:8080/api (직접 접근)
const getApiBaseUrl = () => {
  // 환경 변수가 있으면 사용
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL
  }
  // 프로덕션 빌드이고 localhost가 아니면 절대 URL 사용
  if (import.meta.env.PROD && window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1') {
    return `${window.location.protocol}//${window.location.hostname}:8080/api`
  }
  // 기본값: 상대 경로 (개발 환경에서 프록시 사용)
  return '/api'
}

const api = axios.create({
  baseURL: getApiBaseUrl(),
  headers: {
    'Content-Type': 'application/json'
  }
})

// 요청 인터셉터: 토큰 자동 추가 (로그인/회원가입 제외)
api.interceptors.request.use(
  (config) => {
    // 로그인 및 회원가입 요청은 토큰 불필요
    const isAuthRequest = config.url === '/auth/login' || config.url === '/users' && config.method === 'post'
    
    if (!isAuthRequest) {
      const token = localStorage.getItem('authToken')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
        console.log('Request with token:', config.url, 'Token:', token.substring(0, 20) + '...')
      } else {
        console.warn('No token found for request:', config.url)
      }
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 응답 인터셉터: 401 에러 시 로그아웃
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error.response?.status, error.response?.data, error.config?.url)
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken')
      window.location.href = '/login'
    } else if (error.response?.status === 403) {
      // 로그인 요청의 403은 다른 처리 (CORS 문제일 수 있음)
      if (error.config?.url === '/auth/login') {
        console.error('403 Forbidden on login - CORS or authentication issue')
        return Promise.reject(error)
      }
      console.error('403 Forbidden - Token may be invalid or expired')
      // 토큰이 만료되었을 수 있으므로 로그인 페이지로 리다이렉트
      localStorage.removeItem('authToken')
      alert('인증이 만료되었습니다. 다시 로그인해주세요.')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export const urlService = {
  createShortUrl(data) {
    return api.post('/urls', data)
  },
  
  bulkCreateShortUrls(urlRequests) {
    return api.post('/urls/bulk', { urls: urlRequests })
  },
  
  getUrlInfo(shortCode) {
    return api.get(`/urls/${shortCode}`)
  },
  
  getAllUrls(page = 0, size = 10, search = null) {
    const params = new URLSearchParams()
    if (page !== undefined && page !== null) params.append('page', page.toString())
    if (size !== undefined && size !== null) params.append('size', size.toString())
    if (search !== null && search !== undefined && search.trim() !== '') {
      params.append('search', search.trim())
    }
    return api.get(`/urls?${params.toString()}`)
  }
}

export const adminService = {
  getStats() {
    return api.get('/admin/stats')
  },
  
  getAllUrls(page = 0, size = 10, search = null, status = 'all') {
    const params = new URLSearchParams()
    if (page !== undefined && page !== null) params.append('page', page.toString())
    if (size !== undefined && size !== null) params.append('size', size.toString())
    if (search !== null && search !== undefined && search.trim() !== '') {
      params.append('search', search.trim())
    }
    if (status !== null && status !== undefined && status !== 'all') {
      params.append('status', status)
    }
    return api.get(`/admin/urls?${params.toString()}`)
  },
  
  getDatabaseInfo() {
    return api.get('/admin/database/info')
  },
  
  deleteUrl(id) {
    return api.delete(`/admin/urls/${id}`)
  },
  
  deleteExpiredUrls() {
    return api.delete('/admin/urls/expired')
  }
}

export const authService = {
  login(username, password) {
    return api.post('/auth/login', { username, password })
  },
  
  register(data) {
    return api.post('/users', data)
  }
}

export const qrCodeService = {
  getQrCode(shortCode) {
    return `/api/qrcode/${shortCode}`
  }
}

export const statsService = {
  getUrlStats(shortCode, days = 7) {
    return api.get(`/stats/${shortCode}`, { params: { days } })
  }
}

export const teamService = {
  createTeam(data, ownerId) {
    const params = new URLSearchParams({ ownerId: ownerId.toString() })
    return api.post(`/teams?${params.toString()}`, data)
  },
  
  getTeam(id) {
    return api.get(`/teams/${id}`)
  },
  
  getTeamsByOwner(ownerId) {
    return api.get(`/teams/owner/${ownerId}`)
  },
  
  addMember(teamId, userId, role = 'MEMBER') {
    return api.post(`/teams/${teamId}/members/${userId}?role=${role}`)
  },
  
  removeMember(teamId, userId) {
    return api.delete(`/teams/${teamId}/members/${userId}`)
  },
  
  getTeamMembers(teamId) {
    return api.get(`/teams/${teamId}/members`)
  }
}

export const apiKeyService = {
  createApiKey(keyName, userId, teamId = null, permissions = null, rateLimit = null) {
    const requestBody = {
      keyName: keyName,
      userId: userId,
      teamId: teamId,
      permissions: permissions,
      rateLimit: rateLimit
    }
    return api.post('/api-keys', requestBody)
  },
  
  getApiKeysByUser(userId) {
    return api.get(`/api-keys/user/${userId}`)
  },
  
  getApiKeysByTeam(teamId) {
    return api.get(`/api-keys/team/${teamId}`)
  },
  
  deactivateApiKey(id) {
    return api.post(`/api-keys/${id}/deactivate`)
  },
  
  activateApiKey(id) {
    return api.post(`/api-keys/${id}/activate`)
  },
  
  deleteApiKey(id) {
    return api.delete(`/api-keys/${id}`)
  }
}

export const whiteLabelService = {
  createOrUpdateSettings(data, userId, teamId = null) {
    const params = new URLSearchParams({ userId: userId.toString() })
    if (teamId) params.append('teamId', teamId.toString())
    return api.post(`/white-label?${params.toString()}`, data)
  },
  
  getSettingsByUser(userId) {
    return api.get(`/white-label/user/${userId}`)
  },
  
  getSettingsByTeam(teamId) {
    return api.get(`/white-label/team/${teamId}`)
  }
}

export const supportTicketService = {
  createTicket(data, userId) {
    const params = new URLSearchParams({ userId: userId.toString() })
    return api.post(`/support/tickets?${params.toString()}`, data)
  },
  
  getTicket(id) {
    return api.get(`/support/tickets/${id}`)
  },
  
  getTicketsByUser(userId, page = 0, size = 10) {
    const params = new URLSearchParams()
    if (page !== undefined && page !== null) params.append('page', page.toString())
    if (size !== undefined && size !== null) params.append('size', size.toString())
    return api.get(`/support/tickets/user/${userId}?${params.toString()}`)
  },
  
  getAllTickets(sort = null, page = 0, size = 10) {
    const params = new URLSearchParams()
    if (sort) params.append('sort', sort)
    if (page !== undefined && page !== null) params.append('page', page.toString())
    if (size !== undefined && size !== null) params.append('size', size.toString())
    return api.get(`/support/tickets?${params.toString()}`)
  },
  
  getTicketsByPriority(priority, page = 0, size = 10) {
    const params = new URLSearchParams()
    if (page !== undefined && page !== null) params.append('page', page.toString())
    if (size !== undefined && size !== null) params.append('size', size.toString())
    return api.get(`/support/tickets/priority/${priority}?${params.toString()}`)
  },
  
  getHighPriorityTickets() {
    return api.get('/support/tickets/high-priority')
  },
  
  assignTicket(id, assignedTo) {
    return api.post(`/support/tickets/${id}/assign?assignedTo=${assignedTo}`)
  },
  
  resolveTicket(id) {
    return api.post(`/support/tickets/${id}/resolve`)
  },
  
  // 댓글 관련 API
  getComments(ticketId) {
    return api.get(`/support/tickets/${ticketId}/comments`)
  },
  
  createComment(ticketId, data, userId) {
    const params = new URLSearchParams({ userId: userId.toString() })
    return api.post(`/support/tickets/${ticketId}/comments?${params.toString()}`, data)
  },
  
  deleteComment(ticketId, commentId) {
    return api.delete(`/support/tickets/${ticketId}/comments/${commentId}`)
  }
}

export const accountManagerService = {
  getUserAccount(userId) {
    return api.get(`/account-manager/users/${userId}`)
  },
  
  getUserUrls(userId) {
    return api.get(`/account-manager/users/${userId}/urls`)
  },
  
  getUserStats(userId) {
    return api.get(`/account-manager/users/${userId}/stats`)
  },
  
  getUserTickets(userId) {
    return api.get(`/account-manager/users/${userId}/tickets`)
  },
  
  toggleUserStatus(userId) {
    return api.post(`/account-manager/users/${userId}/toggle-status`)
  },
  
  updateUserRole(userId, role) {
    return api.put(`/account-manager/users/${userId}/role`, { role })
  }
}

export const userService = {
  getAllUsers() {
    return api.get('/users')
  },
  
  getUser(id) {
    return api.get(`/users/${id}`)
  },
  
  getUserByUsername(username) {
    return api.get(`/users/by-username/${username}`)
  },
  
  createUser(data) {
    return api.post('/users', data)
  }
}

export const openApiService = {
  // OpenAPI 스펙 가져오기
  async getOpenApiSpec() {
    // 환경에 따라 다른 경로 사용
    let baseUrl
    if (import.meta.env.VITE_API_BASE_URL) {
      baseUrl = import.meta.env.VITE_API_BASE_URL
    } else if (import.meta.env.PROD && window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1') {
      baseUrl = `${window.location.protocol}//${window.location.hostname}:8080`
    } else {
      // 개발 환경: 백엔드 서버로 직접 접근 또는 프록시 사용
      // Vite 프록시를 통해 /v3 경로도 프록시되므로 window.location.origin 사용
      baseUrl = window.location.origin
    }
    
    const response = await fetch(`${baseUrl}/v3/api-docs`)
    if (!response.ok) {
      throw new Error(`Failed to fetch OpenAPI spec: ${response.status} ${response.statusText}`)
    }
    return response.json()
  }
}

export default api

