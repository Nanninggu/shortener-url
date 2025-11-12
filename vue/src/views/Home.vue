<template>
  <div class="container mx-auto px-4 py-12 max-w-4xl">
    <div class="space-y-8">
      <!-- Main Card -->
      <div class="bg-card border rounded-lg shadow-sm p-6 sm:p-8">
        <div class="text-center mb-6 sm:mb-8">
          <h2 class="text-2xl sm:text-3xl font-bold text-card-foreground mb-2">URL을 단축하세요</h2>
          <p class="text-sm sm:text-base text-muted-foreground">긴 URL을 짧고 간단한 링크로 변환합니다</p>
        </div>
        
        <form @submit.prevent="createShortUrl" class="space-y-6 max-w-2xl mx-auto">
          <div class="space-y-2">
            <label for="url" class="text-sm font-medium text-foreground">원본 URL</label>
            <input
              id="url"
              v-model="originalUrl"
              type="url"
              placeholder="https://example.com/very/long/url..."
              class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              required
            />
          </div>
          
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <label for="expiration" class="text-sm font-medium text-foreground">만료일 (선택사항)</label>
              <input
                id="expiration"
                v-model.number="expirationDays"
                type="number"
                min="1"
                placeholder="일 수"
                class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              />
            </div>
            <div class="space-y-2">
              <label for="customCode" class="text-sm font-medium text-foreground">커스텀 코드 (선택사항)</label>
              <input
                id="customCode"
                v-model="customCode"
                type="text"
                placeholder="원하는 코드"
                class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              />
            </div>
          </div>
          
          <button
            type="submit"
            :disabled="loading"
            class="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2 w-full"
          >
            {{ loading ? '생성 중...' : '단축 URL 생성' }}
          </button>
        </form>
        
        <!-- Success Result -->
        <div v-if="shortUrl" class="mt-8 pt-8 border-t space-y-4">
          <div class="rounded-md bg-green-50 dark:bg-green-950 border border-green-200 dark:border-green-800 p-4">
            <div class="flex items-center gap-2 mb-4">
              <CircleCheck class="h-5 w-5 text-green-600 dark:text-green-400" />
              <h3 class="text-lg font-semibold text-green-900 dark:text-green-100">단축 URL이 생성되었습니다!</h3>
            </div>
            
            <div class="flex gap-2 mb-4">
              <input
                :value="shortUrl"
                readonly
                class="flex h-10 flex-1 rounded-md border border-green-300 dark:border-green-700 bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
              />
              <button
                @click="copyToClipboard"
                class="inline-flex items-center justify-center gap-2 rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-green-600 text-white hover:bg-green-700 h-10 px-4 py-2"
              >
                <CopyDocument v-if="!copied" class="h-4 w-4" />
                <CircleCheck v-else class="h-4 w-4" />
                {{ copied ? '복사됨!' : '복사' }}
              </button>
            </div>
            
            <div class="rounded-md bg-background p-3 space-y-2 text-sm">
              <p class="text-muted-foreground">
                <span class="font-medium text-foreground">원본 URL:</span> {{ originalUrl }}
              </p>
              <p class="text-muted-foreground">
                <span class="font-medium text-foreground">클릭 수:</span> 0
              </p>
            </div>
            
            <div class="mt-4">
              <img
                :src="`/api/qrcode/${shortCode}`"
                alt="QR Code"
                class="mx-auto w-32 h-32 border rounded-md"
              />
              <p class="text-xs text-center text-muted-foreground mt-2">QR 코드</p>
            </div>
          </div>
        </div>
        
        <!-- Error Message -->
        <div v-if="error" class="mt-8 rounded-md bg-destructive/10 border border-destructive/20 p-4">
          <div class="flex items-center gap-2">
            <Warning class="h-5 w-5 text-destructive" />
            <p class="text-sm text-destructive">{{ error }}</p>
          </div>
        </div>
      </div>
      
      <!-- Features -->
      <div class="grid gap-6 md:grid-cols-3">
        <div class="bg-card border rounded-lg shadow-sm p-6 text-center hover:shadow-md transition-shadow">
          <div class="flex justify-center mb-4">
            <Promotion class="h-12 w-12 text-primary" />
          </div>
          <h3 class="text-lg font-semibold text-card-foreground mb-2">빠른 변환</h3>
          <p class="text-sm text-muted-foreground">즉시 URL을 단축합니다</p>
        </div>
        <div class="bg-card border rounded-lg shadow-sm p-6 text-center hover:shadow-md transition-shadow">
          <div class="flex justify-center mb-4">
            <DataAnalysis class="h-12 w-12 text-primary" />
          </div>
          <h3 class="text-lg font-semibold text-card-foreground mb-2">클릭 통계</h3>
          <p class="text-sm text-muted-foreground">링크 클릭 수를 추적합니다</p>
        </div>
        <div class="bg-card border rounded-lg shadow-sm p-6 text-center hover:shadow-md transition-shadow">
          <div class="flex justify-center mb-4">
            <Lock class="h-12 w-12 text-primary" />
          </div>
          <h3 class="text-lg font-semibold text-card-foreground mb-2">안전한 서비스</h3>
          <p class="text-sm text-muted-foreground">신뢰할 수 있는 단축 서비스</p>
        </div>
      </div>

      <!-- Help Links -->
      <div class="grid gap-6 md:grid-cols-2">
        <div class="bg-primary/10 border border-primary/20 rounded-lg shadow-sm p-6 text-center">
          <div class="flex justify-center mb-2">
            <Reading class="h-8 w-8 text-primary" />
          </div>
          <h3 class="text-lg font-semibold text-foreground mb-2">사용법 안내</h3>
          <p class="text-sm text-muted-foreground mb-4">
            H-Link URL Shortener의 모든 기능을 쉽게 배워보세요
          </p>
          <router-link
            to="/guide"
            class="inline-flex items-center justify-center gap-2 rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2"
          >
            사용법 보기
            <ArrowRight class="h-4 w-4" />
          </router-link>
        </div>
        <div class="bg-primary/10 border border-primary/20 rounded-lg shadow-sm p-6 text-center">
          <div class="flex justify-center mb-2">
            <Connection class="h-8 w-8 text-primary" />
          </div>
          <h3 class="text-lg font-semibold text-foreground mb-2">API로 통합하세요</h3>
          <p class="text-sm text-muted-foreground mb-4">
            RESTful API를 사용하여 애플리케이션에 URL 단축 기능을 통합할 수 있습니다
          </p>
          <router-link
            to="/api-docs"
            class="inline-flex items-center justify-center gap-2 rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2"
          >
            API 문서 보기
            <ArrowRight class="h-4 w-4" />
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { urlService } from '../services/api'

export default {
  name: 'Home',
  data() {
    return {
      originalUrl: '',
      expirationDays: null,
      customCode: '',
      shortUrl: '',
      shortCode: '',
      loading: false,
      error: '',
      copied: false
    }
  },
  methods: {
    async createShortUrl() {
      this.loading = true
      this.error = ''
      this.shortUrl = ''
      
      try {
        const response = await urlService.createShortUrl(
          this.originalUrl,
          this.expirationDays,
          this.customCode || null
        )
        
        this.shortUrl = response.data.shortUrl
        this.shortCode = response.data.shortCode
        this.originalUrl = response.data.originalUrl
      } catch (err) {
        this.error = err.response?.data?.message || 'URL 생성에 실패했습니다.'
        console.error('Error creating short URL:', err)
      } finally {
        this.loading = false
      }
    },
    
    async copyToClipboard() {
      try {
        await navigator.clipboard.writeText(this.shortUrl)
        this.copied = true
        setTimeout(() => {
          this.copied = false
        }, 2000)
      } catch (err) {
        console.error('Failed to copy:', err)
      }
    }
  }
}
</script>
