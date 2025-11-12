<template>
  <div class="container mx-auto px-4 py-12 max-w-6xl">
    <div class="bg-card border rounded-lg shadow-sm p-8">
      <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-6 sm:mb-8 pb-6 border-b">
        <div>
          <h2 class="text-2xl sm:text-3xl font-bold text-card-foreground mb-2">단축 URL 목록</h2>
          <p class="text-sm text-muted-foreground">생성된 모든 단축 URL을 확인하세요</p>
        </div>
        <router-link
          to="/"
          class="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-secondary text-secondary-foreground hover:bg-secondary/80 h-10 px-4 py-2 w-full sm:w-auto"
        >
          ← 홈으로
        </router-link>
      </div>
      
      <!-- Loading State -->
      <div v-if="loading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
        <p class="mt-4 text-muted-foreground">로딩 중...</p>
      </div>
      
      <!-- Empty State -->
      <div v-else-if="urls.length === 0" class="text-center py-12">
        <Link class="mx-auto h-12 w-12 text-muted-foreground" />
        <h3 class="mt-4 text-lg font-semibold text-foreground">아직 생성된 단축 URL이 없습니다</h3>
        <p class="mt-2 text-sm text-muted-foreground">첫 번째 단축 URL을 만들어보세요</p>
        <router-link
          to="/"
          class="mt-6 inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2"
        >
          첫 번째 URL 만들기
        </router-link>
      </div>
      
      <!-- URL Table -->
      <div v-else class="overflow-x-auto">
        <table class="w-full border-collapse">
          <thead>
            <tr class="border-b bg-muted/50">
              <th class="h-12 px-4 text-left align-middle font-medium text-muted-foreground whitespace-nowrap">원본 URL</th>
              <th class="h-12 px-4 text-left align-middle font-medium text-muted-foreground whitespace-nowrap">단축 URL</th>
              <th class="h-12 px-4 text-center align-middle font-medium text-muted-foreground whitespace-nowrap">클릭 수</th>
              <th class="h-12 px-4 text-left align-middle font-medium text-muted-foreground whitespace-nowrap">생성일</th>
              <th class="h-12 px-4 text-left align-middle font-medium text-muted-foreground whitespace-nowrap">만료일</th>
              <th class="h-12 px-4 text-center align-middle font-medium text-muted-foreground whitespace-nowrap">작업</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="url in urls"
              :key="url.id"
              class="border-b transition-colors hover:bg-muted/50"
            >
              <td class="p-4 align-middle">
                <div class="max-w-xs truncate text-sm text-foreground" :title="url.originalUrl">
                  {{ truncateUrl(url.originalUrl) }}
                </div>
              </td>
              <td class="p-4 align-middle">
                <div class="flex items-center gap-2 flex-wrap">
                  <a
                    :href="url.shortUrl"
                    target="_blank"
                    class="text-sm text-primary hover:underline break-all"
                  >
                    {{ url.shortUrl }}
                  </a>
                  <router-link
                    :to="`/stats/${url.shortCode}`"
                    class="inline-flex items-center justify-center gap-1 rounded-md text-xs font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 bg-secondary text-secondary-foreground hover:bg-secondary/80 h-6 px-2"
                    title="통계 보기"
                  >
                    <DataAnalysis class="h-3 w-3" />
                    통계
                  </router-link>
                </div>
              </td>
              <td class="p-4 text-center align-middle">
                <span class="inline-flex items-center rounded-full bg-primary/10 px-2.5 py-0.5 text-xs font-medium text-primary">
                  {{ url.clickCount }}
                </span>
              </td>
              <td class="p-4 align-middle text-sm text-muted-foreground whitespace-nowrap">
                {{ formatDate(url.createdAt) }}
              </td>
              <td class="p-4 align-middle text-sm text-muted-foreground whitespace-nowrap">
                {{ url.expiresAt ? formatDate(url.expiresAt) : '무제한' }}
              </td>
              <td class="p-4 text-center align-middle">
                <button
                  @click="copyUrl(url.shortUrl)"
                  class="inline-flex items-center justify-center gap-1 rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-secondary text-secondary-foreground hover:bg-secondary/80 h-8 px-3"
                >
                  <CopyDocument class="h-3 w-3" />
                  복사
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import { urlService } from '../services/api'

export default {
  name: 'UrlList',
  data() {
    return {
      urls: [],
      loading: true
    }
  },
  mounted() {
    this.fetchUrls()
  },
  methods: {
    async fetchUrls() {
      this.loading = true
      try {
        const response = await urlService.getAllUrls()
        this.urls = response.data
      } catch (err) {
        console.error('Error fetching URLs:', err)
      } finally {
        this.loading = false
      }
    },
    
    truncateUrl(url) {
      if (url.length > 50) {
        return url.substring(0, 50) + '...'
      }
      return url
    },
    
    formatDate(dateString) {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    
    async copyUrl(url) {
      try {
        await navigator.clipboard.writeText(url)
        alert('URL이 복사되었습니다!')
      } catch (err) {
        console.error('Failed to copy:', err)
      }
    }
  }
}
</script>
