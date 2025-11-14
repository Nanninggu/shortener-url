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
              @input="handleSearchChange"
              type="text"
              placeholder="URL 검색..."
              class="flex h-10 w-full rounded-md border border-input bg-background pl-10 pr-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
            />
          </div>
          <div class="relative">
            <Filter class="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
            <select
              v-model="filterStatus"
              @change="handleFilterChange"
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

        <div v-else-if="urls.length === 0" class="text-center py-12">
          <Document class="mx-auto h-12 w-12 text-muted-foreground mb-4" />
          <p class="text-muted-foreground">URL이 없습니다</p>
        </div>

        <div v-else>
          <div class="overflow-x-auto">
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
                  v-for="url in paginatedUrls"
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

          <!-- Pagination -->
          <div v-if="urls.length > 0 && totalPages > 1" class="mt-6 flex flex-col sm:flex-row items-center justify-between gap-4 border-t pt-4">
            <div class="flex items-center gap-2">
              <span class="text-sm text-muted-foreground">페이지당 항목 수:</span>
              <select
                v-model.number="pageSize"
                @change="handlePageSizeChange"
                class="h-8 rounded-md border border-input bg-background px-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
              >
                <option :value="10">10</option>
                <option :value="20">20</option>
                <option :value="50">50</option>
                <option :value="100">100</option>
              </select>
            </div>
            
            <div class="flex items-center gap-2">
              <span class="text-sm text-muted-foreground">
                총 {{ pagination ? pagination.totalElements : 0 }}개 중 {{ pagination ? ((currentPage * pageSize) + 1) : 0 }}-{{ pagination ? Math.min((currentPage + 1) * pageSize, pagination.totalElements) : 0 }}개 표시
              </span>
            </div>
            
            <div class="flex items-center gap-1">
              <button
                @click="goToPage(0)"
                :disabled="currentPage === 0"
                class="h-8 px-3 rounded-md border border-input bg-background text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-muted"
              >
                처음
              </button>
              <button
                @click="goToPage(currentPage - 1)"
                :disabled="currentPage === 0"
                class="h-8 px-3 rounded-md border border-input bg-background text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-muted"
              >
                이전
              </button>
              
              <div class="flex items-center gap-1">
                <button
                  v-for="pageNum in visiblePages"
                  :key="pageNum"
                  @click="goToPage(pageNum - 1)"
                  :class="[
                    'h-8 w-8 rounded-md border text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
                    pageNum - 1 === currentPage
                      ? 'bg-primary text-primary-foreground border-primary'
                      : 'bg-background border-input hover:bg-muted'
                  ]"
                >
                  {{ pageNum }}
                </button>
              </div>
              
              <button
                @click="goToPage(currentPage + 1)"
                :disabled="currentPage >= totalPages - 1"
                class="h-8 px-3 rounded-md border border-input bg-background text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-muted"
              >
                다음
              </button>
              <button
                @click="goToPage(totalPages - 1)"
                :disabled="currentPage >= totalPages - 1"
                class="h-8 px-3 rounded-md border border-input bg-background text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-muted"
              >
                마지막
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- H2 Database Info -->
      <div class="bg-card border rounded-lg shadow-sm p-6">
        <h2 class="text-xl font-bold text-foreground mb-4 flex items-center gap-2">
          <Setting class="h-5 w-5" />
          H2 데이터베이스 정보
        </h2>
        
        <div v-if="loading && !dbInfo" class="text-center py-8">
          <Loading class="inline-block h-8 w-8 animate-spin text-primary" />
          <p class="mt-4 text-muted-foreground">데이터베이스 정보 로딩 중...</p>
        </div>

        <div v-else-if="dbInfo" class="space-y-4">
          <div class="grid gap-4 md:grid-cols-2">
            <div class="bg-muted/50 rounded-lg p-4">
              <p class="text-sm font-medium text-muted-foreground mb-1">데이터베이스 타입</p>
              <p class="text-lg font-semibold text-foreground">{{ dbInfo.databaseType }}</p>
            </div>
            
            <div class="bg-muted/50 rounded-lg p-4">
              <p class="text-sm font-medium text-muted-foreground mb-1">TCP 포트</p>
              <p class="text-lg font-semibold text-foreground">{{ dbInfo.tcpPort }}</p>
            </div>
            
            <div class="bg-muted/50 rounded-lg p-4">
              <p class="text-sm font-medium text-muted-foreground mb-1">사용자명</p>
              <p class="text-lg font-semibold text-foreground font-mono">{{ dbInfo.username }}</p>
            </div>
            
            <div class="bg-muted/50 rounded-lg p-4">
              <p class="text-sm font-medium text-muted-foreground mb-1">비밀번호</p>
              <p class="text-lg font-semibold text-foreground font-mono">{{ dbInfo.password || '(없음)' }}</p>
            </div>
          </div>

          <div class="space-y-3">
            <div>
              <p class="text-sm font-medium text-muted-foreground mb-2">JDBC URL</p>
              <div class="bg-muted rounded-md p-3 flex items-center justify-between">
                <code class="text-sm text-foreground font-mono break-all">{{ dbInfo.jdbcUrl }}</code>
                <button
                  @click="copyToClipboard(dbInfo.jdbcUrl)"
                  class="ml-2 px-3 py-1 text-xs bg-primary text-primary-foreground rounded hover:bg-primary/90 whitespace-nowrap"
                >
                  복사
                </button>
              </div>
            </div>

            <div>
              <p class="text-sm font-medium text-muted-foreground mb-2">TCP URL (외부 접속용)</p>
              <div class="bg-muted rounded-md p-3 flex items-center justify-between">
                <code class="text-sm text-foreground font-mono break-all">{{ dbInfo.tcpUrl }}</code>
                <button
                  @click="copyToClipboard(dbInfo.tcpUrl)"
                  class="ml-2 px-3 py-1 text-xs bg-primary text-primary-foreground rounded hover:bg-primary/90 whitespace-nowrap"
                >
                  복사
                </button>
              </div>
            </div>

            <div>
              <p class="text-sm font-medium text-muted-foreground mb-2">H2 콘솔 URL</p>
              <div class="bg-muted rounded-md p-3 flex items-center justify-between">
                <a
                  :href="dbInfo.consoleUrl"
                  target="_blank"
                  class="text-sm text-primary hover:underline font-mono break-all"
                >
                  {{ dbInfo.consoleUrl }}
                </a>
                <a
                  :href="dbInfo.consoleUrl"
                  target="_blank"
                  class="ml-2 px-3 py-1 text-xs bg-primary text-primary-foreground rounded hover:bg-primary/90 whitespace-nowrap"
                >
                  열기
                </a>
              </div>
            </div>

            <div>
              <p class="text-sm font-medium text-muted-foreground mb-2">데이터베이스 경로</p>
              <div class="bg-muted rounded-md p-3">
                <code class="text-sm text-foreground font-mono">{{ dbInfo.databasePath }}</code>
              </div>
            </div>
          </div>

          <div class="flex gap-2 pt-2">
            <span
              :class="dbInfo.consoleEnabled ? 'bg-green-500/10 text-green-700 dark:text-green-400' : 'bg-red-500/10 text-red-700 dark:text-red-400'"
              class="inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-medium"
            >
              <CircleCheck v-if="dbInfo.consoleEnabled" class="h-3 w-3" />
              <Warning v-else class="h-3 w-3" />
              콘솔: {{ dbInfo.consoleEnabled ? '활성화' : '비활성화' }}
            </span>
            <span
              :class="dbInfo.externalAccessEnabled ? 'bg-green-500/10 text-green-700 dark:text-green-400' : 'bg-red-500/10 text-red-700 dark:text-red-400'"
              class="inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-medium"
            >
              <CircleCheck v-if="dbInfo.externalAccessEnabled" class="h-3 w-3" />
              <Warning v-else class="h-3 w-3" />
              외부 접근: {{ dbInfo.externalAccessEnabled ? '허용' : '차단' }}
            </span>
          </div>
        </div>

        <div v-else class="text-center py-8">
          <p class="text-muted-foreground">데이터베이스 정보를 불러올 수 없습니다.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { adminService } from '../services/api'
import { 
  Link, Histogram, CircleCheck, Warning, Setting, RefreshRight, 
  Delete, Search, Filter, Document, Loading, TurnOff, House 
} from '@element-plus/icons-vue'

export default {
  name: 'Admin',
  components: {
    Link, Histogram, CircleCheck, Warning, Setting, RefreshRight,
    Delete, Search, Filter, Document, Loading, TurnOff, House
  },
  data() {
    return {
      stats: null,
      urls: [],
      dbInfo: null,
      loading: false,
      searchQuery: '',
      filterStatus: 'all',
      currentPage: 0,
      pageSize: 10,
      pagination: null
    }
  },
  computed: {
    // 서버 사이드 페이지네이션을 사용하므로 클라이언트 필터링 제거
    filteredUrls() {
      return this.urls
    },
    
    totalPages() {
      return this.pagination ? this.pagination.totalPages : 0
    },
    
    paginatedUrls() {
      // 서버에서 이미 페이지네이션된 데이터를 받으므로 그대로 반환
      return this.urls
    },
    
    visiblePages() {
      const total = this.totalPages
      const current = this.currentPage + 1
      const pages = []
      
      // 최대 5개의 페이지 번호 표시
      let start = Math.max(1, current - 2)
      let end = Math.min(total, start + 4)
      
      // 끝에서 5개 미만이면 시작점 조정
      if (end - start < 4) {
        start = Math.max(1, end - 4)
      }
      
      for (let i = start; i <= end; i++) {
        pages.push(i)
      }
      
      return pages
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
        // 서버 사이드 페이지네이션 사용 (검색/필터 포함)
        const search = this.searchQuery && this.searchQuery.trim() ? this.searchQuery.trim() : null
        const status = this.filterStatus
        
        const [statsResponse, urlsResponse, dbInfoResponse] = await Promise.all([
          adminService.getStats(),
          adminService.getAllUrls(this.currentPage, this.pageSize, search, status),
          adminService.getDatabaseInfo().catch(() => null) // DB 정보는 실패해도 계속 진행
        ])
        this.stats = statsResponse.data
        
        // 페이징 응답인지 확인
        if (urlsResponse.data.content) {
          this.urls = urlsResponse.data.content
          this.pagination = {
            page: urlsResponse.data.page,
            size: urlsResponse.data.size,
            totalElements: urlsResponse.data.totalElements,
            totalPages: urlsResponse.data.totalPages,
            first: urlsResponse.data.first,
            last: urlsResponse.data.last
          }
        } else {
          // 하위 호환성: 배열 응답인 경우
          this.urls = urlsResponse.data
          this.pagination = null
        }
        
        if (dbInfoResponse) {
          this.dbInfo = dbInfoResponse.data
        }
      } catch (err) {
        console.error('Error loading admin data:', err)
        alert('데이터를 불러오는데 실패했습니다.')
      } finally {
        this.loading = false
      }
    },
    
    goToPage(page) {
      if (page >= 0 && page < this.totalPages) {
        this.currentPage = page
        // 서버에서 데이터 다시 로드
        this.loadData()
        window.scrollTo({ top: 0, behavior: 'smooth' })
      }
    },
    
    handlePageSizeChange() {
      this.currentPage = 0
      // 페이지 크기 변경 시 첫 페이지로 이동하고 데이터 다시 로드
      this.loadData()
    },
    
    handleSearchChange() {
      this.currentPage = 0
      // 검색어 변경 시 첫 페이지로 이동하고 데이터 다시 로드
      // 디바운싱을 위해 약간의 지연 추가
      clearTimeout(this.searchTimeout)
      this.searchTimeout = setTimeout(() => {
        this.loadData()
      }, 500) // 500ms 지연
    },
    
    handleFilterChange() {
      this.currentPage = 0
      // 필터 변경 시 첫 페이지로 이동하고 데이터 다시 로드
      this.loadData()
    },
    
    copyToClipboard(text) {
      navigator.clipboard.writeText(text).then(() => {
        alert('클립보드에 복사되었습니다.')
      }).catch(() => {
        // Fallback for older browsers
        const textarea = document.createElement('textarea')
        textarea.value = text
        document.body.appendChild(textarea)
        textarea.select()
        document.execCommand('copy')
        document.body.removeChild(textarea)
        alert('클립보드에 복사되었습니다.')
      })
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

