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
          class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-secondary text-secondary-foreground hover:bg-secondary/80 h-10 px-4 py-2 w-full sm:w-auto"
        >
          <ArrowLeft class="h-4 w-4" />
          홈으로
        </router-link>
      </div>

      <!-- Search -->
      <div class="bg-card border rounded-lg shadow-sm p-4 mb-6">
        <div class="flex gap-4">
          <div class="flex-1 relative">
            <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
            <input
              v-model="searchQuery"
              @input="handleSearchChange"
              type="text"
              placeholder="원본 URL, 단축 URL, 단축 코드로 검색..."
              class="flex h-10 w-full rounded-md border border-input bg-background pl-10 pr-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
            />
          </div>
          <button
            v-if="searchQuery"
            @click="clearSearch"
            class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-secondary text-secondary-foreground hover:bg-secondary/80 h-10 px-4 py-2"
          >
            <Close class="h-4 w-4" />
            초기화
          </button>
        </div>
      </div>
      
      <!-- Loading State -->
      <div v-if="loading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
        <p class="mt-4 text-muted-foreground">로딩 중...</p>
      </div>
      
      <!-- Empty State -->
      <div v-else-if="urls.length === 0" class="text-center py-12">
        <Link class="mx-auto h-12 w-12 text-muted-foreground" />
        <h3 class="mt-4 text-lg font-semibold text-foreground">
          {{ searchQuery ? '검색 결과가 없습니다' : '아직 생성된 단축 URL이 없습니다' }}
        </h3>
        <p class="mt-2 text-sm text-muted-foreground">
          {{ searchQuery ? '다른 검색어를 시도해보세요' : '첫 번째 단축 URL을 만들어보세요' }}
        </p>
        <router-link
          to="/"
          class="mt-6 inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2"
        >
          <Plus class="h-4 w-4" />
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
                  'h-8 w-8 rounded-md border text-sm font-medium',
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
  </div>
</template>

<script>
import { urlService } from '../services/api'
import { Search, Close, Link, Plus, DataAnalysis, CopyDocument, ArrowLeft } from '@element-plus/icons-vue'

export default {
  name: 'UrlList',
  components: {
    Search, Close, Link, Plus, DataAnalysis, CopyDocument, ArrowLeft
  },
  data() {
    return {
      urls: [],
      loading: true,
      currentPage: 0,
      pageSize: 10,
      pagination: null,
      searchQuery: '',
      searchTimeout: null
    }
  },
  computed: {
    // 서버 사이드 페이지네이션을 사용하므로 클라이언트 필터링 제거
    totalPages() {
      return this.pagination ? this.pagination.totalPages : 0
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
    this.fetchUrls()
  },
  methods: {
    async fetchUrls() {
      this.loading = true
      try {
        // 서버 사이드 페이지네이션 사용 (검색 포함)
        const search = this.searchQuery && this.searchQuery.trim() ? this.searchQuery.trim() : null
        
        const response = await urlService.getAllUrls(this.currentPage, this.pageSize, search)
        
        // 페이징 응답인지 확인
        if (response.data.content) {
          this.urls = response.data.content
          this.pagination = {
            page: response.data.page,
            size: response.data.size,
            totalElements: response.data.totalElements,
            totalPages: response.data.totalPages,
            first: response.data.first,
            last: response.data.last
          }
          this.currentPage = response.data.page
        } else {
          // 하위 호환성: 배열 응답인 경우
          this.urls = response.data
          this.pagination = null
        }
      } catch (err) {
        console.error('Error fetching URLs:', err)
      } finally {
        this.loading = false
      }
    },
    
    goToPage(page) {
      if (page >= 0 && page < this.totalPages) {
        this.currentPage = page
        // 서버에서 데이터 다시 로드
        this.fetchUrls()
        // 페이지 상단으로 스크롤
        window.scrollTo({ top: 0, behavior: 'smooth' })
      }
    },
    
    handlePageSizeChange() {
      this.currentPage = 0
      // 페이지 크기 변경 시 첫 페이지로 이동하고 데이터 다시 로드
      this.fetchUrls()
    },
    
    handleSearchChange() {
      this.currentPage = 0
      // 검색어 변경 시 첫 페이지로 이동하고 데이터 다시 로드
      // 디바운싱을 위해 약간의 지연 추가
      clearTimeout(this.searchTimeout)
      this.searchTimeout = setTimeout(() => {
        this.fetchUrls()
      }, 500) // 500ms 지연
    },
    
    clearSearch() {
      this.searchQuery = ''
      this.currentPage = 0
      // 검색 초기화 시 첫 페이지로 이동하고 데이터 다시 로드
      this.fetchUrls()
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
