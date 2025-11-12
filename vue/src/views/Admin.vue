<template>
  <div class="container mx-auto px-4 py-12 max-w-7xl">
    <div class="space-y-8">
      <!-- Header -->
      <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b">
        <div>
          <h1 class="text-3xl sm:text-4xl font-bold text-foreground mb-2">관리자 페이지</h1>
          <p class="text-sm text-muted-foreground">URL 단축 서비스 관리 및 통계</p>
        </div>
        <div class="flex gap-2 w-full sm:w-auto">
          <button
            @click="logout"
            class="inline-flex items-center justify-center gap-2 rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-destructive text-destructive-foreground hover:bg-destructive/90 h-10 px-4 py-2 flex-1 sm:flex-initial"
          >
            <TurnOff class="h-4 w-4" />
            로그아웃
          </button>
          <router-link
            to="/"
            class="inline-flex items-center justify-center gap-2 rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-secondary text-secondary-foreground hover:bg-secondary/80 h-10 px-4 py-2 flex-1 sm:flex-initial"
          >
            <House class="h-4 w-4" />
            홈으로
          </router-link>
        </div>
      </div>

      <!-- Stats Cards -->
      <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-4" v-if="stats">
        <div class="bg-card border rounded-lg shadow-sm p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-muted-foreground">총 URL 수</p>
              <p class="text-3xl font-bold text-foreground mt-2">{{ stats.totalUrls }}</p>
            </div>
            <div class="h-12 w-12 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
              <Link class="h-6 w-6 text-blue-600 dark:text-blue-400" />
            </div>
          </div>
        </div>

        <div class="bg-card border rounded-lg shadow-sm p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-muted-foreground">총 클릭 수</p>
              <p class="text-3xl font-bold text-foreground mt-2">{{ stats.totalClicks }}</p>
            </div>
            <div class="h-12 w-12 rounded-full bg-green-100 dark:bg-green-900 flex items-center justify-center">
              <Histogram class="h-6 w-6 text-green-600 dark:text-green-400" />
            </div>
          </div>
        </div>

        <div class="bg-card border rounded-lg shadow-sm p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-muted-foreground">활성 URL</p>
              <p class="text-3xl font-bold text-foreground mt-2">{{ stats.activeUrls }}</p>
            </div>
            <div class="h-12 w-12 rounded-full bg-purple-100 dark:bg-purple-900 flex items-center justify-center">
              <CircleCheck class="h-6 w-6 text-purple-600 dark:text-purple-400" />
            </div>
          </div>
        </div>

        <div class="bg-card border rounded-lg shadow-sm p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-muted-foreground">만료된 URL</p>
              <p class="text-3xl font-bold text-foreground mt-2">{{ stats.expiredUrls }}</p>
            </div>
            <div class="h-12 w-12 rounded-full bg-red-100 dark:bg-red-900 flex items-center justify-center">
              <Warning class="h-6 w-6 text-red-600 dark:text-red-400" />
            </div>
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="bg-card border rounded-lg shadow-sm p-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-2xl font-bold text-foreground flex items-center gap-2">
            <Setting class="h-6 w-6" />
            관리 작업
          </h2>
          <button
            @click="refreshData"
            :disabled="loading"
            class="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-secondary text-secondary-foreground hover:bg-secondary/80 h-10 px-4 py-2"
          >
            <RefreshRight class="h-4 w-4 mr-2" :class="{ 'animate-spin': loading }" />
            새로고침
          </button>
        </div>
        <div class="flex gap-4">
          <button
            @click="deleteExpiredUrls"
            :disabled="loading || !stats || stats.expiredUrls === 0"
            class="inline-flex items-center justify-center gap-2 rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-destructive text-destructive-foreground hover:bg-destructive/90 h-10 px-4 py-2"
          >
            <Delete class="h-4 w-4" />
            만료된 URL 삭제 ({{ stats?.expiredUrls || 0 }})
          </button>
        </div>
      </div>

      <!-- Search and Filter -->
      <div class="bg-card border rounded-lg shadow-sm p-6">
        <h2 class="text-xl font-bold text-foreground mb-4 flex items-center gap-2">
          <Search class="h-5 w-5" />
          URL 검색 및 필터
        </h2>
        <div class="flex items-center gap-4 mb-6">
          <div class="flex-1 relative">
            <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <input
              v-model="searchQuery"
              type="text"
              placeholder="URL 검색..."
              class="flex h-10 w-full rounded-md border border-input bg-background pl-10 pr-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
            />
          </div>
          <div class="relative">
            <Filter class="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
            <select
              v-model="filterStatus"
              class="flex h-10 rounded-md border border-input bg-background pl-10 pr-8 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 appearance-none"
            >
              <option value="all">전체</option>
              <option value="active">활성</option>
              <option value="expired">만료됨</option>
            </select>
          </div>
        </div>

        <!-- URLs Table -->
        <div v-if="loading && urls.length === 0" class="text-center py-12">
          <Loading class="inline-block h-8 w-8 animate-spin text-primary" />
          <p class="mt-4 text-muted-foreground">로딩 중...</p>
        </div>

        <div v-else-if="filteredUrls.length === 0" class="text-center py-12">
          <Document class="mx-auto h-12 w-12 text-muted-foreground mb-4" />
          <p class="text-muted-foreground">URL이 없습니다</p>
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full border-collapse">
            <thead>
              <tr class="border-b bg-muted/50">
                <th class="h-12 px-4 text-left align-middle font-medium text-muted-foreground whitespace-nowrap">ID</th>
                <th class="h-12 px-4 text-left align-middle font-medium text-muted-foreground whitespace-nowrap">원본 URL</th>
                <th class="h-12 px-4 text-left align-middle font-medium text-muted-foreground whitespace-nowrap">단축 URL</th>
                <th class="h-12 px-4 text-center align-middle font-medium text-muted-foreground whitespace-nowrap">클릭 수</th>
                <th class="h-12 px-4 text-left align-middle font-medium text-muted-foreground whitespace-nowrap">생성일</th>
                <th class="h-12 px-4 text-left align-middle font-medium text-muted-foreground whitespace-nowrap">만료일</th>
                <th class="h-12 px-4 text-center align-middle font-medium text-muted-foreground whitespace-nowrap">상태</th>
                <th class="h-12 px-4 text-center align-middle font-medium text-muted-foreground whitespace-nowrap">작업</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="url in filteredUrls"
                :key="url.id"
                class="border-b transition-colors hover:bg-muted/50"
              >
                <td class="p-4 align-middle text-sm text-muted-foreground font-mono">{{ url.id }}</td>
                <td class="p-4 align-middle">
                  <div class="max-w-xs truncate text-sm text-foreground" :title="url.originalUrl">
                    {{ url.originalUrl }}
                  </div>
                </td>
                <td class="p-4 align-middle">
                  <a
                    :href="url.shortUrl"
                    target="_blank"
                    class="text-sm text-primary hover:underline break-all"
                  >
                    {{ url.shortUrl }}
                  </a>
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
                  <span
                    v-if="isExpired(url)"
                    class="inline-flex items-center gap-1.5 rounded-full bg-destructive/10 text-destructive px-3 py-1 text-xs font-medium whitespace-nowrap"
                  >
                    <Warning class="h-3 w-3 flex-shrink-0" />
                    <span class="whitespace-nowrap">만료됨</span>
                  </span>
                  <span
                    v-else
                    class="inline-flex items-center gap-1.5 rounded-full bg-green-500/10 text-green-700 dark:text-green-400 px-3 py-1 text-xs font-medium whitespace-nowrap"
                  >
                    <CircleCheck class="h-3 w-3 flex-shrink-0" />
                    <span class="whitespace-nowrap">활성</span>
                  </span>
                </td>
                <td class="p-4 text-center align-middle">
                  <button
                    @click="deleteUrl(url.id)"
                    :disabled="loading"
                    class="inline-flex items-center justify-center gap-1.5 rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-destructive text-destructive-foreground hover:bg-destructive/90 h-8 px-3 whitespace-nowrap"
                  >
                    <Delete class="h-3 w-3 flex-shrink-0" />
                    <span class="whitespace-nowrap">삭제</span>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { adminService } from '../services/api'

export default {
  name: 'Admin',
  data() {
    return {
      stats: null,
      urls: [],
      loading: false,
      searchQuery: '',
      filterStatus: 'all'
    }
  },
  computed: {
    filteredUrls() {
      let filtered = this.urls

      // Search filter
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase()
        filtered = filtered.filter(url =>
          url.originalUrl.toLowerCase().includes(query) ||
          url.shortUrl.toLowerCase().includes(query) ||
          url.shortCode.toLowerCase().includes(query)
        )
      }

      // Status filter
      if (this.filterStatus === 'active') {
        filtered = filtered.filter(url => !this.isExpired(url))
      } else if (this.filterStatus === 'expired') {
        filtered = filtered.filter(url => this.isExpired(url))
      }

      return filtered
    }
  },
  mounted() {
    // 인증 확인
    if (!localStorage.getItem('authToken')) {
      this.$router.push('/login')
      return
    }
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const [statsResponse, urlsResponse] = await Promise.all([
          adminService.getStats(),
          adminService.getAllUrls()
        ])
        this.stats = statsResponse.data
        this.urls = urlsResponse.data
      } catch (err) {
        console.error('Error loading admin data:', err)
        alert('데이터를 불러오는데 실패했습니다.')
      } finally {
        this.loading = false
      }
    },
    
    async refreshData() {
      await this.loadData()
    },
    
    async deleteUrl(id) {
      if (!confirm('정말 이 URL을 삭제하시겠습니까?')) {
        return
      }
      
      this.loading = true
      try {
        await adminService.deleteUrl(id)
        await this.loadData()
      } catch (err) {
        console.error('Error deleting URL:', err)
        alert('URL 삭제에 실패했습니다.')
      } finally {
        this.loading = false
      }
    },
    
    async deleteExpiredUrls() {
      if (!confirm('만료된 모든 URL을 삭제하시겠습니까?')) {
        return
      }
      
      this.loading = true
      try {
        await adminService.deleteExpiredUrls()
        await this.loadData()
        alert('만료된 URL이 삭제되었습니다.')
      } catch (err) {
        console.error('Error deleting expired URLs:', err)
        alert('만료된 URL 삭제에 실패했습니다.')
      } finally {
        this.loading = false
      }
    },
    
    isExpired(url) {
      if (!url.expiresAt) return false
      return new Date(url.expiresAt) < new Date()
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
    
    logout() {
      localStorage.removeItem('authToken')
      this.$router.push('/login')
    }
  }
}
</script>

