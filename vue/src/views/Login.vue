<template>
  <div class="min-h-screen flex items-center justify-center bg-background px-4">
    <div class="w-full max-w-md">
      <div class="bg-card border rounded-lg shadow-sm p-8">
        <div class="text-center mb-8">
          <h1 class="text-3xl font-bold text-foreground mb-2">관리자 로그인</h1>
          <p class="text-muted-foreground">H-Link 관리자 페이지에 접속하세요</p>
        </div>

        <form @submit.prevent="login" class="space-y-6">
          <div class="space-y-2">
            <label for="username" class="text-sm font-medium text-foreground">사용자명</label>
            <input
              id="username"
              v-model="username"
              type="text"
              placeholder="admin"
              class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              required
            />
          </div>

          <div class="space-y-2">
            <label for="password" class="text-sm font-medium text-foreground">비밀번호</label>
            <input
              id="password"
              v-model="password"
              type="password"
              placeholder="비밀번호를 입력하세요"
              class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              required
            />
          </div>

          <div v-if="error" class="rounded-md bg-destructive/10 border border-destructive/20 p-4">
            <p class="text-sm text-destructive">{{ error }}</p>
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2 w-full"
          >
            {{ loading ? '로그인 중...' : '로그인' }}
          </button>
        </form>

        <div class="mt-6 text-center">
          <router-link
            to="/"
            class="text-sm text-muted-foreground hover:text-foreground transition-colors"
          >
            ← 홈으로 돌아가기
          </router-link>
        </div>

        <div class="mt-6 p-4 bg-muted rounded-md">
          <p class="text-xs text-muted-foreground text-center">
            기본 관리자 계정: admin / admin123
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { authService } from '../services/api'

export default {
  name: 'Login',
  data() {
    return {
      username: '',
      password: '',
      loading: false,
      error: ''
    }
  },
  mounted() {
    // 이미 로그인되어 있으면 관리자 페이지로 리다이렉트
    if (localStorage.getItem('authToken')) {
      this.$router.push('/admin')
    }
  },
  methods: {
    async login() {
      this.loading = true
      this.error = ''

      try {
        const response = await authService.login(this.username, this.password)
        if (response.data && response.data.token) {
          localStorage.setItem('authToken', response.data.token)
          this.$router.push('/admin')
        } else {
          this.error = '로그인 응답이 올바르지 않습니다.'
        }
      } catch (err) {
        console.error('Login error:', err)
        if (err.response) {
          // 서버에서 에러 메시지가 있는 경우
          if (err.response.data && err.response.data.message) {
            this.error = err.response.data.message
          } else if (err.response.status === 401) {
            this.error = '사용자명 또는 비밀번호가 올바르지 않습니다.'
          } else if (err.response.status === 400) {
            this.error = '입력 정보를 확인해주세요.'
          } else {
            this.error = `로그인에 실패했습니다. (${err.response.status})`
          }
        } else {
          this.error = '로그인에 실패했습니다. 서버에 연결할 수 없습니다.'
        }
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

