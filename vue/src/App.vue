<template>
  <div id="app" :class="{ 'dark': isDarkMode }" class="min-h-screen bg-background flex flex-col">
    <header class="border-b bg-card sticky top-0 z-50 backdrop-blur supports-[backdrop-filter]:bg-card/95">
      <div class="container mx-auto px-4 py-4">
        <div class="flex items-center justify-between">
          <router-link to="/" class="flex items-center gap-3 hover:opacity-80 transition-opacity">
            <div class="flex flex-col">
              <h1 class="text-2xl font-bold text-foreground leading-tight">H-Link</h1>
              <p class="text-xs text-muted-foreground leading-tight">Simple is the Best!</p>
            </div>
          </router-link>
          <nav class="flex items-center gap-2 flex-wrap">
            <!-- 핵심 기능 (모든 사용자) -->
            <router-link
              to="/"
              class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 px-3 py-2 text-muted-foreground hover:text-foreground hover:bg-accent"
            >
              <House class="h-4 w-4" />
              홈
            </router-link>
            <router-link
              to="/urls"
              class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 px-3 py-2 text-muted-foreground hover:text-foreground hover:bg-accent"
            >
              <Link class="h-4 w-4" />
              URL 목록
            </router-link>
            <router-link
              to="/urls/bulk"
              class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 px-3 py-2 text-muted-foreground hover:text-foreground hover:bg-accent"
            >
              <DocumentAdd class="h-4 w-4" />
              대량 생성
            </router-link>
            
            <template v-if="isAuthenticated">
              <!-- 지원 (인증 필요, 자주 접근) -->
              <router-link
                to="/support"
                class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 px-3 py-2 text-muted-foreground hover:text-foreground hover:bg-accent"
              >
                <Service class="h-4 w-4" />
                지원
              </router-link>
              
              <!-- 협업 기능 (인증 필요) -->
              <router-link
                to="/teams"
                class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 px-3 py-2 text-muted-foreground hover:text-foreground hover:bg-accent"
              >
                <User class="h-4 w-4" />
                팀 관리
              </router-link>
              
              <!-- 개발자 기능 (인증 필요) -->
              <router-link
                to="/api-keys"
                class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 px-3 py-2 text-muted-foreground hover:text-foreground hover:bg-accent"
              >
                <Key class="h-4 w-4" />
                API 키
              </router-link>
              
              <!-- 고급 기능 (인증 필요) -->
              <router-link
                to="/white-label"
                class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 px-3 py-2 text-muted-foreground hover:text-foreground hover:bg-accent"
              >
                <Brush class="h-4 w-4" />
                화이트라벨
              </router-link>
              
              <!-- 관리자 기능 (인증 + 관리자 권한) -->
              <router-link
                v-if="isAdmin"
                to="/admin"
                class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 px-3 py-2 text-muted-foreground hover:text-foreground hover:bg-accent"
              >
                <Setting class="h-4 w-4" />
                관리자
              </router-link>
              <router-link
                v-if="isAdmin"
                to="/account-manager"
                class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 px-3 py-2 text-muted-foreground hover:text-foreground hover:bg-accent"
              >
                <UserFilled class="h-4 w-4" />
                계정관리
              </router-link>
            </template>
            
            <!-- API 문서 (모든 사용자 접근 가능, 개발자 문서) -->
            <a
              :href="swaggerUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 px-3 py-2 text-muted-foreground hover:text-foreground hover:bg-accent"
              title="API 문서 (새 탭에서 열림)"
            >
              <Document class="h-4 w-4" />
              API 문서
            </a>
            
            <!-- 다크모드 토글 버튼 -->
            <button
              @click="toggleDarkMode"
              class="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 px-3 py-2 text-muted-foreground hover:text-foreground hover:bg-accent"
              :title="isDarkMode ? '라이트 모드로 전환' : '다크 모드로 전환'"
            >
              <component :is="isDarkMode ? 'Sunny' : 'Moon'" class="h-4 w-4" />
            </button>
            
            <!-- 로그인/로그아웃 버튼 -->
            <div class="flex items-center gap-2 ml-2 pl-2 border-l">
              <template v-if="!isAuthenticated">
                <router-link
                  to="/register"
                  class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 px-3 py-2 bg-secondary text-secondary-foreground hover:bg-secondary/80"
                >
                  <User class="h-4 w-4" />
                  회원가입
                </router-link>
                <router-link
                  to="/login"
                  class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 px-3 py-2 bg-primary text-primary-foreground hover:bg-primary/90"
                >
                  <UserFilled class="h-4 w-4" />
                  로그인
                </router-link>
              </template>
              <template v-else>
                <span class="text-sm text-muted-foreground px-2 inline-flex items-center gap-1">
                  <Avatar class="h-4 w-4" />
                  {{ username || '사용자' }}
                </span>
                <button
                  @click="handleLogout"
                  class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 px-3 py-2 bg-secondary text-secondary-foreground hover:bg-secondary/80"
                >
                  <TurnOff class="h-4 w-4" />
                  로그아웃
                </button>
              </template>
            </div>
          </nav>
        </div>
      </div>
    </header>
    
    <main class="flex-1">
      <router-view />
    </main>
    
    <footer class="border-t bg-card mt-auto">
      <div class="container mx-auto px-4 py-6">
        <p class="text-center text-sm text-muted-foreground">
          &copy; 2024 H-Link. URL Shortener Service
        </p>
      </div>
    </footer>
  </div>
</template>

<script>
import { House, Link, DocumentAdd, Service, User, Key, Brush, Setting, UserFilled, Document, TurnOff, Avatar } from '@element-plus/icons-vue'

export default {
  name: 'App',
  components: {
    House, Link, DocumentAdd, Service, User, Key, Brush, Setting, UserFilled, Document, TurnOff, Avatar
  },
  data() {
    return {
      isAuthenticated: false,
      username: null,
      userRole: null,
      isDarkMode: false
    }
  },
  computed: {
    isAdmin() {
      // admin 계정(username이 'admin') 또는 ADMIN 역할이면 관리자로 인식
      return this.userRole === 'ADMIN' || this.username === 'admin'
    },
    swaggerUrl() {
      // 프로덕션에서는 상대 경로 사용, 로컬에서는 절대 경로 사용
      if (import.meta.env.PROD) {
        // 프로덕션: Nginx를 통해 직접 접근
        return '/swagger-ui/index.html'
      } else {
        // 로컬 개발: 백엔드 서버로 직접 접근
        const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
        return `${apiBaseUrl}/swagger-ui/index.html`
      }
    }
  },
  mounted() {
    this.checkAuth()
    this.initDarkMode()
    // localStorage 변경 감지 (다른 탭에서의 변경)
    window.addEventListener('storage', this.checkAuth)
    window.addEventListener('storage', this.initDarkMode)
    // 로그인 상태 변경 이벤트 감지 (같은 탭에서의 변경)
    window.addEventListener('auth-changed', this.checkAuth)
  },
  beforeUnmount() {
    window.removeEventListener('storage', this.checkAuth)
    window.removeEventListener('storage', this.initDarkMode)
    window.removeEventListener('auth-changed', this.checkAuth)
  },
  watch: {
    $route() {
      this.checkAuth()
    }
  },
  methods: {
    checkAuth() {
      const token = localStorage.getItem('authToken')
      this.isAuthenticated = !!token
      this.username = localStorage.getItem('username')
      this.userRole = localStorage.getItem('userRole')
    },
    handleLogout() {
      if (confirm('로그아웃하시겠습니까?')) {
        // localStorage 정리
        localStorage.removeItem('authToken')
        localStorage.removeItem('userId')
        localStorage.removeItem('username')
        localStorage.removeItem('userRole')
        
        // 상태 업데이트
        this.isAuthenticated = false
        this.username = null
        this.userRole = null
        
        // 로그아웃 이벤트 발생
        window.dispatchEvent(new Event('auth-changed'))
        
        // 홈으로 리다이렉트
        this.$router.push('/')
      }
    },
    initDarkMode() {
      // localStorage에서 다크모드 설정 불러오기
      const savedMode = localStorage.getItem('darkMode')
      if (savedMode !== null) {
        this.isDarkMode = savedMode === 'true'
      } else {
        // 저장된 설정이 없으면 시스템 설정 확인
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
        this.isDarkMode = prefersDark
      }
      this.applyDarkMode()
    },
    toggleDarkMode() {
      this.isDarkMode = !this.isDarkMode
      localStorage.setItem('darkMode', this.isDarkMode.toString())
      this.applyDarkMode()
      // 다른 탭에 변경사항 알림
      window.dispatchEvent(new Event('storage'))
    },
    applyDarkMode() {
      if (this.isDarkMode) {
        document.documentElement.classList.add('dark')
      } else {
        document.documentElement.classList.remove('dark')
      }
    }
  }
}
</script>
