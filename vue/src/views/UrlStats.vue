<template>
  <div class="container mx-auto px-4 py-12 max-w-6xl">
    <div class="space-y-8">
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-4xl font-bold text-foreground mb-2">URL 통계</h1>
          <p class="text-muted-foreground">단축 URL의 상세 통계를 확인하세요</p>
        </div>
        <router-link
          to="/urls"
          class="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-secondary text-secondary-foreground hover:bg-secondary/80 h-10 px-4 py-2"
        >
          ← 목록으로
        </router-link>
      </div>

      <!-- URL Info -->
      <div class="bg-card border rounded-lg shadow-sm p-6" v-if="urlInfo">
        <h2 class="text-xl font-bold text-foreground mb-4">URL 정보</h2>
        <div class="space-y-2">
          <p class="text-sm">
            <span class="font-medium text-muted-foreground">원본 URL:</span>
            <span class="text-foreground ml-2">{{ urlInfo.originalUrl }}</span>
          </p>
          <p class="text-sm">
            <span class="font-medium text-muted-foreground">단축 URL:</span>
            <a :href="urlInfo.shortUrl" target="_blank" class="text-primary hover:underline ml-2">
              {{ urlInfo.shortUrl }}
            </a>
          </p>
          <p class="text-sm">
            <span class="font-medium text-muted-foreground">총 클릭 수:</span>
            <span class="text-foreground ml-2">{{ urlInfo.clickCount }}</span>
          </p>
        </div>
      </div>

      <!-- Stats -->
      <div v-if="loading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
        <p class="mt-4 text-muted-foreground">로딩 중...</p>
      </div>

      <div v-else-if="stats" class="space-y-6">
        <!-- Clicks by Date -->
        <div class="bg-card border rounded-lg shadow-sm p-6">
          <h2 class="text-xl font-bold text-foreground mb-4">일별 클릭 통계</h2>
          <div class="space-y-2">
            <div
              v-for="item in stats.clicksByDate"
              :key="item.date"
              class="flex items-center justify-between p-3 bg-muted rounded-md"
            >
              <span class="text-sm font-medium text-foreground">{{ item.date }}</span>
              <span class="text-sm text-muted-foreground">{{ item.count }}회</span>
            </div>
            <p v-if="stats.clicksByDate.length === 0" class="text-sm text-muted-foreground text-center py-4">
              데이터가 없습니다
            </p>
          </div>
        </div>

        <!-- Clicks by Hour -->
        <div class="bg-card border rounded-lg shadow-sm p-6">
          <h2 class="text-xl font-bold text-foreground mb-4">시간대별 클릭 통계</h2>
          <div class="grid grid-cols-4 gap-2">
            <div
              v-for="item in stats.clicksByHour"
              :key="item.hour"
              class="p-3 bg-muted rounded-md text-center"
            >
              <p class="text-xs text-muted-foreground">{{ item.hour }}시</p>
              <p class="text-lg font-bold text-foreground mt-1">{{ item.count }}</p>
            </div>
            <p v-if="stats.clicksByHour.length === 0" class="col-span-4 text-sm text-muted-foreground text-center py-4">
              데이터가 없습니다
            </p>
          </div>
        </div>

        <!-- Clicks by Country -->
        <div class="bg-card border rounded-lg shadow-sm p-6">
          <h2 class="text-xl font-bold text-foreground mb-4">국가별 클릭 통계</h2>
          <div class="space-y-2">
            <div
              v-for="item in stats.clicksByCountry"
              :key="item.country"
              class="flex items-center justify-between p-3 bg-muted rounded-md"
            >
              <span class="text-sm font-medium text-foreground">{{ item.country }}</span>
              <span class="text-sm text-muted-foreground">{{ item.count }}회</span>
            </div>
            <p v-if="stats.clicksByCountry.length === 0" class="text-sm text-muted-foreground text-center py-4">
              데이터가 없습니다
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { urlService, statsService } from '../services/api'

export default {
  name: 'UrlStats',
  data() {
    return {
      shortCode: '',
      urlInfo: null,
      stats: null,
      loading: true
    }
  },
  mounted() {
    this.shortCode = this.$route.params.shortCode
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const [urlResponse, statsResponse] = await Promise.all([
          urlService.getUrlInfo(this.shortCode),
          statsService.getUrlStats(this.shortCode, 30)
        ])
        this.urlInfo = urlResponse.data
        this.stats = statsResponse.data
      } catch (err) {
        console.error('Error loading stats:', err)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

