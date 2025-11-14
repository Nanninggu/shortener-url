<template>
  <div class="min-h-screen flex items-center justify-center bg-background px-4">
    <div class="w-full max-w-md">
      <div class="bg-card border rounded-lg shadow-sm p-8">
        <div class="text-center mb-8">
          <h1 class="text-3xl font-bold text-foreground mb-2">회원가입</h1>
          <p class="text-muted-foreground">H-Link에 가입하세요</p>
        </div>

        <form @submit.prevent="register" class="space-y-6">
          <div class="space-y-2">
            <label for="username" class="text-sm font-medium text-foreground">사용자명 *</label>
            <input
              id="username"
              v-model="form.username"
              type="text"
              placeholder="3-50자"
              minlength="3"
              maxlength="50"
              required
              class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
            />
          </div>

          <div class="space-y-2">
            <label for="email" class="text-sm font-medium text-foreground">이메일 *</label>
            <input
              id="email"
              v-model="form.email"
              type="email"
              placeholder="example@email.com"
              required
              class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
            />
          </div>

          <div class="space-y-2">
            <label for="password" class="text-sm font-medium text-foreground">비밀번호 *</label>
            <input
              id="password"
              v-model="form.password"
              type="password"
              placeholder="최소 8자 이상"
              minlength="8"
              required
              class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
            />
          </div>

          <div class="space-y-2">
            <label for="passwordConfirm" class="text-sm font-medium text-foreground">비밀번호 확인 *</label>
            <input
              id="passwordConfirm"
              v-model="form.passwordConfirm"
              type="password"
              placeholder="비밀번호를 다시 입력하세요"
              required
              class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
            />
            <p v-if="form.password && form.passwordConfirm && form.password !== form.passwordConfirm" class="text-xs text-destructive">
              비밀번호가 일치하지 않습니다.
            </p>
          </div>

          <div class="space-y-2">
            <label for="planType" class="text-sm font-medium text-foreground">플랜</label>
            <select
              id="planType"
              v-model="form.planType"
              class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
            >
              <option value="FREE">무료</option>
              <option value="BASIC">기본</option>
              <option value="PRO">프로</option>
              <option value="ENTERPRISE">엔터프라이즈</option>
            </select>
          </div>

          <div v-if="error" class="rounded-md bg-destructive/10 border border-destructive/20 p-4">
            <p class="text-sm text-destructive">{{ error }}</p>
          </div>

          <button
            type="submit"
            :disabled="loading || (form.password && form.passwordConfirm && form.password !== form.passwordConfirm)"
            class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2 w-full"
          >
            <Loading v-if="loading" class="h-4 w-4 animate-spin" />
            <User v-else class="h-4 w-4" />
            {{ loading ? '가입 중...' : '회원가입' }}
          </button>
        </form>

        <div class="mt-6 text-center">
          <router-link
            to="/login"
            class="text-sm text-muted-foreground hover:text-foreground transition-colors"
          >
            이미 계정이 있으신가요? 로그인
          </router-link>
        </div>

        <div class="mt-6 text-center">
          <router-link
            to="/"
            class="inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground transition-colors"
          >
            <ArrowLeft class="h-4 w-4" />
            홈으로 돌아가기
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { authService } from '../services/api'

export default {
  name: 'Register',
  data() {
    return {
      form: {
        username: '',
        email: '',
        password: '',
        passwordConfirm: '',
        planType: 'FREE'
      },
      loading: false,
      error: ''
    }
  },
  mounted() {
    // 이미 로그인되어 있으면 홈으로 리다이렉트
    if (localStorage.getItem('authToken')) {
      this.$router.push('/')
    }
  },
  methods: {
    async register() {
      // 비밀번호 확인
      if (this.form.password !== this.form.passwordConfirm) {
        this.error = '비밀번호가 일치하지 않습니다.'
        return
      }

      this.loading = true
      this.error = ''

      try {
        const registerData = {
          username: this.form.username.trim(),
          email: this.form.email.trim(),
          password: this.form.password,
          planType: this.form.planType
        }

        await authService.register(registerData)
        
        alert('회원가입이 완료되었습니다. 로그인해주세요.')
        this.$router.push('/login')
      } catch (err) {
        console.error('Register error:', err)
        if (err.response) {
          // 서버에서 에러 메시지가 있는 경우
          if (err.response.data && err.response.data.message) {
            this.error = err.response.data.message
          } else if (err.response.data && typeof err.response.data === 'object') {
            // Validation 에러
            const errors = Object.values(err.response.data).flat()
            this.error = errors.join(', ')
          } else if (err.response.status === 400) {
            this.error = '입력 정보를 확인해주세요.'
          } else {
            this.error = `회원가입에 실패했습니다. (${err.response.status})`
          }
        } else {
          this.error = '회원가입에 실패했습니다. 서버에 연결할 수 없습니다.'
        }
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

