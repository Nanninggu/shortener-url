<template>
  <div class="container mx-auto px-4 py-12 max-w-6xl">
    <div class="space-y-8">
      <!-- Header -->
      <div class="text-center">
        <h1 class="text-3xl font-bold text-foreground mb-2">고급 URL 생성</h1>
        <p class="text-muted-foreground">타겟팅, 브랜드 도메인 등 고급 옵션을 포함한 URL 생성</p>
      </div>

      <!-- Main Form -->
      <div class="bg-card border rounded-lg shadow-sm p-6 sm:p-8">
        <form @submit.prevent="createShortUrl" class="space-y-6">
          <!-- Basic Settings -->
          <div class="space-y-4">
            <h2 class="text-xl font-semibold text-foreground border-b pb-2">기본 설정</h2>
            
            <div class="space-y-2">
              <label for="originalUrl" class="text-sm font-medium text-foreground">원본 URL *</label>
              <input
                id="originalUrl"
                v-model="form.originalUrl"
                type="url"
                placeholder="https://example.com/very/long/url..."
                class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                required
              />
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-2">
                <label for="customCode" class="text-sm font-medium text-foreground">커스텀 코드</label>
                <input
                  id="customCode"
                  v-model="form.customCode"
                  type="text"
                  placeholder="원하는 코드"
                  class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                />
              </div>
              <div class="space-y-2">
                <label for="expirationDays" class="text-sm font-medium text-foreground">만료일 (일)</label>
                <input
                  id="expirationDays"
                  v-model.number="form.expirationDays"
                  type="number"
                  min="1"
                  placeholder="예: 30"
                  class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                />
              </div>
            </div>
          </div>

          <!-- Targeting Settings -->
          <div class="space-y-4">
            <h2 class="text-xl font-semibold text-foreground border-b pb-2">타겟팅 설정</h2>
            
            <div class="space-y-2">
              <label for="mobileDeeplink" class="text-sm font-medium text-foreground">모바일 딥링크</label>
              <input
                id="mobileDeeplink"
                v-model="form.mobileDeeplink"
                type="url"
                placeholder="myapp://path/to/content"
                class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              />
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-2">
                <label for="desktopUrl" class="text-sm font-medium text-foreground">데스크톱 URL</label>
                <input
                  id="desktopUrl"
                  v-model="form.desktopUrl"
                  type="url"
                  placeholder="https://desktop.example.com"
                  class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                />
              </div>
              <div class="space-y-2">
                <label for="tabletUrl" class="text-sm font-medium text-foreground">태블릿 URL</label>
                <input
                  id="tabletUrl"
                  v-model="form.tabletUrl"
                  type="url"
                  placeholder="https://tablet.example.com"
                  class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                />
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-2">
                <label for="iosUrl" class="text-sm font-medium text-foreground">iOS URL</label>
                <input
                  id="iosUrl"
                  v-model="form.iosUrl"
                  type="url"
                  placeholder="https://ios.example.com"
                  class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                />
              </div>
              <div class="space-y-2">
                <label for="androidUrl" class="text-sm font-medium text-foreground">Android URL</label>
                <input
                  id="androidUrl"
                  v-model="form.androidUrl"
                  type="url"
                  placeholder="https://android.example.com"
                  class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                />
              </div>
            </div>

            <div class="space-y-2">
              <label class="text-sm font-medium text-foreground">언어별 리디렉션 (JSON)</label>
              <textarea
                v-model="form.languageRedirects"
                placeholder='{"en": "https://en.example.com", "ko": "https://ko.example.com"}'
                rows="3"
                class="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm font-mono ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              ></textarea>
            </div>

            <div class="space-y-2">
              <label class="text-sm font-medium text-foreground">지역별 리디렉션 (JSON)</label>
              <textarea
                v-model="form.regionRedirects"
                placeholder='{"US": "https://us.example.com", "KR": "https://kr.example.com"}'
                rows="3"
                class="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm font-mono ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              ></textarea>
            </div>
          </div>

          <!-- QR Code Settings -->
          <div class="space-y-4">
            <h2 class="text-xl font-semibold text-foreground border-b pb-2">QR 코드 설정</h2>
            
            <div class="flex items-center space-x-2">
              <input
                id="dynamicQrEnabled"
                v-model="form.dynamicQrEnabled"
                type="checkbox"
                class="h-4 w-4 rounded border-input bg-background text-primary focus:ring-2 focus:ring-ring focus:ring-offset-2"
              />
              <label for="dynamicQrEnabled" class="text-sm font-medium text-foreground">동적 QR 코드 활성화</label>
            </div>

            <div v-if="form.dynamicQrEnabled" class="space-y-2">
              <label class="text-sm font-medium text-foreground">QR 코드 커스텀 데이터 (JSON)</label>
              <textarea
                v-model="form.qrCustomData"
                placeholder='{"campaign": "summer2024", "source": "social"}'
                rows="3"
                class="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm font-mono ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              ></textarea>
            </div>
          </div>

          <!-- Submit Button -->
          <div class="flex gap-4">
            <button
              type="submit"
              :disabled="loading"
              class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2 flex-1"
            >
              <Plus v-if="!loading" class="h-4 w-4" />
              <Loading v-else class="h-4 w-4 animate-spin" />
              {{ loading ? '생성 중...' : 'URL 생성' }}
            </button>
            <router-link
              to="/"
              class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium bg-secondary text-secondary-foreground hover:bg-secondary/80 h-10 px-4 py-2"
            >
              <Close class="h-4 w-4" />
              취소
            </router-link>
          </div>
        </form>

        <!-- Success Result -->
        <div v-if="result" class="mt-8 pt-8 border-t space-y-4">
              <div class="rounded-md bg-primary/10 border border-primary/20 p-4">
            <h3 class="text-lg font-semibold text-foreground mb-4">URL이 생성되었습니다!</h3>
            
            <div class="flex gap-2 mb-4">
              <input
                :value="result.shortUrl"
                readonly
                class="flex h-10 flex-1 rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
              />
              <button
                @click="copyToClipboard(result.shortUrl)"
                class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2"
              >
                <CopyDocument v-if="!copied" class="h-4 w-4" />
                <CircleCheck v-else class="h-4 w-4" />
                {{ copied ? '복사됨!' : '복사' }}
              </button>
            </div>

            <div class="mt-4">
              <img
                :src="`/api/qrcode/${result.shortCode}`"
                alt="QR Code"
                class="mx-auto w-32 h-32 border rounded-md"
              />
            </div>
          </div>
        </div>

        <!-- Error Message -->
        <div v-if="error" class="mt-8 rounded-md bg-destructive/10 border border-destructive/20 p-4">
          <p class="text-sm text-destructive">{{ error }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { urlService } from '../services/api'

export default {
  name: 'UrlCreate',
  data() {
    return {
      form: {
        originalUrl: '',
        customCode: '',
        expirationDays: null,
        mobileDeeplink: '',
        desktopUrl: '',
        tabletUrl: '',
        iosUrl: '',
        androidUrl: '',
        languageRedirects: '',
        regionRedirects: '',
        dynamicQrEnabled: false,
        qrCustomData: ''
      },
      result: null,
      loading: false,
      error: '',
      copied: false
    }
  },
  methods: {
    async createShortUrl() {
      this.loading = true
      this.error = ''
      this.result = null

      try {
        // JSON 문자열 파싱 검증
        const data = { ...this.form }
        
        if (data.languageRedirects) {
          try {
            JSON.parse(data.languageRedirects)
          } catch (e) {
            throw new Error('언어별 리디렉션 JSON 형식이 올바르지 않습니다.')
          }
        }
        
        if (data.regionRedirects) {
          try {
            JSON.parse(data.regionRedirects)
          } catch (e) {
            throw new Error('지역별 리디렉션 JSON 형식이 올바르지 않습니다.')
          }
        }
        
        if (data.qrCustomData) {
          try {
            JSON.parse(data.qrCustomData)
          } catch (e) {
            throw new Error('QR 코드 커스텀 데이터 JSON 형식이 올바르지 않습니다.')
          }
        }

        // 빈 문자열을 null로 변환
        Object.keys(data).forEach(key => {
          if (data[key] === '') {
            data[key] = null
          }
        })

        const response = await urlService.createShortUrl(data)
        this.result = response.data
      } catch (err) {
        this.error = err.response?.data?.message || err.message || 'URL 생성에 실패했습니다.'
        console.error('Error creating short URL:', err)
      } finally {
        this.loading = false
      }
    },
    
    async copyToClipboard(text) {
      try {
        await navigator.clipboard.writeText(text)
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

