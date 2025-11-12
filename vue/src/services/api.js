import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  }
})

// 요청 인터셉터: 토큰 자동 추가
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      console.log('Request with token:', config.url, 'Token:', token.substring(0, 20) + '...')
    } else {
      console.warn('No token found for request:', config.url)
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
  createShortUrl(originalUrl, expirationDays = null, customCode = null) {
    return api.post('/urls', {
      originalUrl,
      expirationDays,
      customCode
    })
  },
  
  getUrlInfo(shortCode) {
    return api.get(`/urls/${shortCode}`)
  },
  
  getAllUrls() {
    return api.get('/urls')
  }
}

export const adminService = {
  getStats() {
    return api.get('/admin/stats')
  },
  
  getAllUrls() {
    return api.get('/admin/urls')
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

export default api

