<template>
  <div class="container mx-auto px-4 py-12 max-w-6xl">
    <div class="space-y-8">
      <!-- Header -->
      <div class="text-center">
        <h1 class="text-3xl font-bold text-foreground mb-2">대량 URL 생성</h1>
        <p class="text-muted-foreground">여러 URL을 한 번에 생성합니다</p>
      </div>

      <!-- Main Form -->
      <div class="bg-card border rounded-lg shadow-sm p-6 sm:p-8">
        <form @submit.prevent="bulkCreateUrls" class="space-y-6">
          <div class="space-y-4">
            <div class="flex items-center justify-between">
              <label class="text-sm font-medium text-foreground">URL 목록 (한 줄에 하나씩)</label>
              <button
                type="button"
                @click="addUrlRow"
                class="inline-flex items-center gap-1 text-sm text-primary hover:underline"
              >
                <Plus class="h-4 w-4" />
                행 추가
              </button>
            </div>
            
            <div class="space-y-2">
              <div
                v-for="(url, index) in urls"
                :key="index"
                class="flex gap-2 items-center"
              >
                <input
                  v-model="urls[index]"
                  type="url"
                  :placeholder="`URL ${index + 1}`"
                  class="flex h-10 flex-1 rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                />
                <button
                  type="button"
                  @click="removeUrlRow(index)"
                  class="inline-flex items-center gap-1 justify-center rounded-md text-sm font-medium bg-destructive text-destructive-foreground hover:bg-destructive/90 h-10 px-3"
                  :disabled="urls.length === 1"
                >
                  <Delete class="h-4 w-4" />
                  삭제
                </button>
              </div>
            </div>

            <div class="text-sm text-muted-foreground">
              총 {{ urls.length }}개의 URL이 생성됩니다.
            </div>
          </div>

          <div class="flex gap-4">
            <button
              type="submit"
              :disabled="loading || urls.every(u => !u.trim())"
              class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2 flex-1"
            >
              <Loading v-if="loading" class="h-4 w-4 animate-spin" />
              <DocumentAdd v-else class="h-4 w-4" />
              {{ loading ? '생성 중...' : '대량 생성' }}
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

        <!-- Results -->
        <div v-if="result" class="mt-8 pt-8 border-t space-y-4">
          <div class="rounded-md bg-primary/10 border border-primary/20 p-4">
            <h3 class="text-lg font-semibold text-foreground mb-4">
              생성 완료: {{ result.success }}개 성공, {{ result.failed }}개 실패
            </h3>

            <!-- Successful URLs -->
            <div v-if="result.successfulUrls && result.successfulUrls.length > 0" class="mb-4">
              <h4 class="font-medium text-foreground mb-2">성공한 URL ({{ result.successfulUrls.length }})</h4>
              <div class="space-y-2 max-h-60 overflow-y-auto">
                <div
                  v-for="(url, index) in result.successfulUrls"
                  :key="index"
                  class="flex gap-2 items-center p-2 bg-background rounded border"
                >
                  <span class="text-sm flex-1 truncate">{{ url.shortUrl }}</span>
                  <button
                    @click="copyToClipboard(url.shortUrl)"
                    class="inline-flex items-center gap-1 text-xs px-2 py-1 bg-primary text-primary-foreground rounded hover:bg-primary/90"
                  >
                    <CopyDocument class="h-3 w-3" />
                    복사
                  </button>
                </div>
              </div>
            </div>

            <!-- Failed URLs -->
            <div v-if="result.errors && result.errors.length > 0">
              <h4 class="font-medium text-destructive mb-2">실패한 URL ({{ result.errors.length }})</h4>
              <div class="space-y-2 max-h-60 overflow-y-auto">
                <div
                  v-for="(error, index) in result.errors"
                  :key="index"
                  class="p-2 bg-destructive/10 rounded border border-destructive/20"
                >
                  <p class="text-sm text-foreground">{{ error.originalUrl }}</p>
                  <p class="text-xs text-destructive mt-1">{{ error.error }}</p>
                </div>
              </div>
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
  name: 'BulkUrlCreate',
  data() {
    return {
      urls: [''],
      result: null,
      loading: false,
      error: ''
    }
  },
  methods: {
    addUrlRow() {
      this.urls.push('')
    },
    
    removeUrlRow(index) {
      if (this.urls.length > 1) {
        this.urls.splice(index, 1)
      }
    },
    
    async bulkCreateUrls() {
      this.loading = true
      this.error = ''
      this.result = null

      try {
        // URL 검증 및 필터링
        const urlRequests = this.urls
          .filter(url => url.trim())
          .map(url => {
            const trimmedUrl = url.trim()
            // URL 형식 검증
            if (!trimmedUrl.match(/^https?:\/\/.+/)) {
              throw new Error(`유효하지 않은 URL 형식: ${trimmedUrl}`)
            }
            return {
              originalUrl: trimmedUrl
            }
          })

        if (urlRequests.length === 0) {
          throw new Error('최소 하나의 URL을 입력해주세요.')
        }

        if (urlRequests.length > 100) {
          throw new Error('한 번에 최대 100개의 URL만 생성할 수 있습니다.')
        }

        const response = await urlService.bulkCreateShortUrls(urlRequests)
        this.result = response.data
      } catch (err) {
        // 백엔드 검증 에러 메시지 추출
        let errorMessage = '대량 URL 생성에 실패했습니다.'
        if (err.response?.data) {
          if (err.response.data.message) {
            errorMessage = err.response.data.message
          } else if (err.response.data.errors) {
            // Spring Validation 에러
            const validationErrors = err.response.data.errors
            errorMessage = validationErrors.map(e => e.defaultMessage || e.message).join(', ')
          }
        } else if (err.message) {
          errorMessage = err.message
        }
        this.error = errorMessage
        console.error('Error bulk creating URLs:', err)
      } finally {
        this.loading = false
      }
    },
    
    async copyToClipboard(text) {
      try {
        await navigator.clipboard.writeText(text)
        alert('클립보드에 복사되었습니다.')
      } catch (err) {
        console.error('Failed to copy:', err)
      }
    }
  }
}
</script>

