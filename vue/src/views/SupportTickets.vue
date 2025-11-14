<template>
  <div class="container mx-auto px-4 py-12 max-w-7xl">
    <div class="space-y-8">
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-foreground mb-2">지원 티켓</h1>
          <p class="text-muted-foreground">고객 지원 티켓 관리</p>
        </div>
        <button
          @click="showCreateModal = true"
          class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2"
        >
          <Plus class="h-4 w-4" />
          티켓 생성
        </button>
      </div>

      <!-- Filter -->
      <div class="bg-card border rounded-lg shadow-sm p-4">
        <div class="flex gap-4">
          <select
            v-model="filterPriority"
            @change="handleFilterChange"
            class="flex h-10 rounded-md border border-input bg-background px-3 py-2 text-sm"
          >
            <option value="">전체 우선순위</option>
            <option value="LOW">낮음</option>
            <option value="NORMAL">보통</option>
            <option value="HIGH">높음</option>
            <option value="URGENT">긴급</option>
          </select>
          <button
            @click="loadTickets"
            class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium bg-secondary text-secondary-foreground hover:bg-secondary/80 h-10 px-4 py-2"
          >
            <RefreshRight class="h-4 w-4" />
            새로고침
          </button>
        </div>
      </div>

      <!-- Tickets List -->
      <div class="bg-card border rounded-lg shadow-sm p-6">
        <h2 class="text-xl font-semibold text-foreground mb-4">티켓 목록</h2>
        
        <div v-if="loading" class="text-center py-12">
          <p class="text-muted-foreground">로딩 중...</p>
        </div>

        <div v-else-if="tickets.length === 0" class="text-center py-12">
          <p class="text-muted-foreground">티켓이 없습니다.</p>
        </div>

        <div v-else>
          <div class="space-y-4">
            <div
              v-for="ticket in paginatedTickets"
              :key="ticket.id"
              class="border rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer"
              @click="viewTicketDetail(ticket)"
            >
              <div class="flex items-start justify-between">
                <div class="flex-1">
                  <div class="flex items-center gap-2 mb-2">
                    <h3 class="text-lg font-semibold text-foreground">{{ ticket.subject }}</h3>
                    <span
                      :class="getPriorityClass(ticket.priority)"
                      class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium"
                    >
                      {{ ticket.priority }}
                    </span>
                    <span
                      :class="getStatusClass(ticket.status)"
                      class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium"
                    >
                      {{ ticket.status }}
                    </span>
                  </div>
                  <p class="text-sm text-muted-foreground mb-2 line-clamp-2">{{ ticket.description }}</p>
                  <div class="flex items-center gap-3 text-xs text-muted-foreground flex-wrap">
                    <span class="inline-flex items-center gap-1">
                      <User class="h-3 w-3" />
                      사용자 ID: {{ ticket.userId || 'N/A' }}
                    </span>
                    <span v-if="ticket.username" class="inline-flex items-center gap-1">
                      <UserFilled class="h-3 w-3" />
                      {{ ticket.username }}
                    </span>
                    <span v-if="ticket.userEmail" class="inline-flex items-center gap-1">
                      <Message class="h-3 w-3" />
                      {{ ticket.userEmail }}
                    </span>
                    <span class="inline-flex items-center gap-1">
                      <ChatLineRound class="h-3 w-3" />
                      답변: {{ ticket.commentCount || 0 }}
                    </span>
                    <span>생성일: {{ formatDate(ticket.createdAt) }}</span>
                  </div>
                </div>
                <div class="flex gap-2" @click.stop>
                  <button
                    @click.stop="viewTicketDetail(ticket)"
                    class="inline-flex items-center gap-1 text-xs px-3 py-1 bg-primary text-white rounded hover:bg-primary/90"
                  >
                    <View class="h-3 w-3" />
                    상세보기
                  </button>
                  <button
                    @click.stop="viewTicketDetail(ticket); showCommentForm = true"
                    class="inline-flex items-center gap-1 text-xs px-3 py-1 bg-primary text-primary-foreground rounded-md hover:bg-primary/90 ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                  >
                    <ChatLineRound class="h-3 w-3" />
                    답변
                  </button>
                  <button
                    v-if="isAdmin && ticket.status !== 'RESOLVED'"
                    @click.stop="resolveTicket(ticket.id)"
                    class="inline-flex items-center gap-1 text-xs px-3 py-1 bg-primary text-primary-foreground rounded-md hover:bg-primary/90 ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                  >
                    <CircleCheck class="h-3 w-3" />
                    해결
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Pagination -->
          <div v-if="tickets.length > 0" class="mt-6 flex flex-col sm:flex-row items-center justify-between gap-4 border-t pt-4">
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
                총 {{ pagination ? pagination.totalElements : tickets.length }}개 중 {{ (currentPage * pageSize) + 1 }}-{{ Math.min((currentPage + 1) * pageSize, pagination ? pagination.totalElements : tickets.length) }}개 표시
              </span>
            </div>
            
            <div v-if="totalPages > 1" class="flex items-center gap-1">
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

      <!-- Ticket Detail Modal -->
      <div v-if="selectedTicket" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="selectedTicket = null">
        <div class="bg-card border rounded-lg shadow-lg p-6 max-w-3xl w-full mx-4 max-h-[90vh] overflow-y-auto">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-semibold text-foreground">티켓 상세</h2>
            <button
              @click="selectedTicket = null"
              class="text-muted-foreground hover:text-foreground transition-colors"
            >
              <Close class="h-6 w-6" />
            </button>
          </div>
          
          <div class="space-y-6">
            <!-- Ticket Header -->
            <div class="flex items-start gap-4 pb-4 border-b">
              <div class="flex-1">
                <h3 class="text-xl font-bold text-foreground mb-3">{{ selectedTicket.subject }}</h3>
                <div class="flex items-center gap-2 flex-wrap">
                  <span
                    :class="getPriorityClass(selectedTicket.priority)"
                    class="inline-flex items-center gap-1 rounded-full px-3 py-1 text-sm font-medium"
                  >
                    <Warning class="h-4 w-4" />
                    {{ selectedTicket.priority }}
                  </span>
                  <span
                    :class="getStatusClass(selectedTicket.status)"
                    class="inline-flex items-center gap-1 rounded-full px-3 py-1 text-sm font-medium"
                  >
                    <CircleCheck v-if="selectedTicket.status === 'RESOLVED'" class="h-4 w-4" />
                    <Loading v-else-if="selectedTicket.status === 'IN_PROGRESS'" class="h-4 w-4 animate-spin" />
                    <InfoFilled v-else class="h-4 w-4" />
                    {{ selectedTicket.status }}
                  </span>
                </div>
              </div>
            </div>

            <!-- User Info -->
            <div class="bg-primary/10 border border-primary/20 rounded-lg p-4">
              <h4 class="text-sm font-semibold text-foreground mb-3 flex items-center gap-2">
                <UserFilled class="h-4 w-4" />
                작성자 정보
              </h4>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                <div>
                  <p class="text-xs font-medium text-muted-foreground mb-1">사용자 ID</p>
                  <p class="text-sm text-foreground font-mono">{{ selectedTicket.userId || 'N/A' }}</p>
                </div>
                <div v-if="selectedTicket.username">
                  <p class="text-xs font-medium text-muted-foreground mb-1">사용자명</p>
                  <p class="text-sm text-foreground">{{ selectedTicket.username }}</p>
                </div>
                <div v-if="selectedTicket.userEmail">
                  <p class="text-xs font-medium text-muted-foreground mb-1">이메일</p>
                  <p class="text-sm text-foreground">{{ selectedTicket.userEmail }}</p>
                </div>
                <div v-if="selectedTicket.userRole">
                  <p class="text-xs font-medium text-muted-foreground mb-1">역할</p>
                  <p class="text-sm text-foreground">{{ selectedTicket.userRole }}</p>
                </div>
              </div>
            </div>

            <!-- Ticket Info -->
            <div class="grid grid-cols-2 gap-4">
              <div>
                <p class="text-sm font-medium text-muted-foreground mb-1">티켓 ID</p>
                <p class="text-base text-foreground font-mono">#{{ selectedTicket.id }}</p>
              </div>
              <div>
                <p class="text-sm font-medium text-muted-foreground mb-1">생성일</p>
                <p class="text-base text-foreground">{{ formatDate(selectedTicket.createdAt) }}</p>
              </div>
              <div v-if="selectedTicket.updatedAt">
                <p class="text-sm font-medium text-muted-foreground mb-1">수정일</p>
                <p class="text-base text-foreground">{{ formatDate(selectedTicket.updatedAt) }}</p>
              </div>
              <div v-if="selectedTicket.resolvedAt">
                <p class="text-sm font-medium text-muted-foreground mb-1">해결일</p>
                <p class="text-base text-foreground">{{ formatDate(selectedTicket.resolvedAt) }}</p>
              </div>
            </div>

            <!-- Ticket Description -->
            <div>
              <p class="text-sm font-medium text-muted-foreground mb-2">설명</p>
              <div class="bg-muted/50 rounded-lg p-4">
                <p class="text-base text-foreground whitespace-pre-wrap">{{ selectedTicket.description }}</p>
              </div>
            </div>

            <!-- Comments Section -->
            <div>
              <div class="flex items-center justify-between mb-3">
                <h4 class="text-sm font-semibold text-foreground flex items-center gap-2">
                  <ChatLineRound class="h-4 w-4" />
                  답변 ({{ comments.length }})
                </h4>
                <button
                  @click="showCommentForm = !showCommentForm"
                  class="inline-flex items-center gap-1 text-xs px-3 py-1 bg-primary text-white rounded-md hover:bg-primary/90 transition-colors"
                >
                  <Plus class="h-3 w-3" />
                  답변 작성
                </button>
              </div>

              <!-- Comment Form -->
              <div v-if="showCommentForm" class="mb-4 p-4 bg-muted/50 rounded-lg border">
                <textarea
                  v-model="newComment"
                  rows="3"
                  placeholder="답변을 입력하세요..."
                  class="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm mb-2"
                ></textarea>
                <div class="flex gap-2 justify-end">
                  <button
                    @click="showCommentForm = false; newComment = ''"
                    class="inline-flex items-center gap-1 px-3 py-1 text-xs bg-secondary text-secondary-foreground rounded-md hover:bg-secondary/80"
                  >
                    취소
                  </button>
                  <button
                    @click="addComment"
                    :disabled="!newComment.trim() || addingComment"
                    class="inline-flex items-center gap-1 px-3 py-1 text-xs bg-primary text-white rounded-md hover:bg-primary/90 disabled:opacity-50"
                  >
                    <Loading v-if="addingComment" class="h-3 w-3 animate-spin" />
                    <Check v-else class="h-3 w-3" />
                    작성
                  </button>
                </div>
              </div>

              <!-- Comments List -->
              <div v-if="loadingComments" class="text-center py-4">
                <Loading class="inline-block h-5 w-5 animate-spin text-primary" />
                <p class="text-xs text-muted-foreground mt-2">댓글 로딩 중...</p>
              </div>
              
              <div v-else-if="comments.length === 0" class="text-center py-8 bg-muted/30 rounded-lg">
                <ChatLineRound class="mx-auto h-8 w-8 text-muted-foreground mb-2" />
                <p class="text-sm text-muted-foreground">아직 답변이 없습니다.</p>
              </div>
              
              <div v-else class="space-y-3">
                <div
                  v-for="comment in comments"
                  :key="comment.id"
                  class="bg-muted/50 rounded-lg p-4 border"
                >
                  <div class="flex items-start justify-between mb-2">
                    <div class="flex items-center gap-2">
                      <Avatar class="h-5 w-5 text-muted-foreground" />
                      <div>
                        <p class="text-sm font-medium text-foreground">
                          {{ comment.username || `사용자 #${comment.userId}` }}
                        </p>
                        <p class="text-xs text-muted-foreground">
                          {{ formatDate(comment.createdAt) }}
                        </p>
                      </div>
                    </div>
                    <button
                      v-if="canDeleteComment(comment)"
                      @click="deleteComment(comment.id)"
                      class="text-xs text-destructive hover:text-destructive/80"
                    >
                      <Delete class="h-3 w-3" />
                    </button>
                  </div>
                  <p class="text-sm text-foreground whitespace-pre-wrap">{{ comment.comment }}</p>
                </div>
              </div>
            </div>

            <!-- Actions -->
            <div class="flex gap-2 pt-4 border-t">
              <button
                v-if="isAdmin && selectedTicket.status !== 'RESOLVED'"
                @click="resolveTicket(selectedTicket.id)"
                class="inline-flex items-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-md hover:bg-primary/90 transition-colors ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
              >
                <CircleCheck class="h-4 w-4" />
                해결 처리
              </button>
              <button
                @click="selectedTicket = null"
                class="inline-flex items-center gap-2 px-4 py-2 bg-secondary text-secondary-foreground rounded-md hover:bg-secondary/80 transition-colors ml-auto"
              >
                <Close class="h-4 w-4" />
                닫기
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Create Ticket Modal -->
      <div v-if="showCreateModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showCreateModal = false">
        <div class="bg-card border rounded-lg shadow-lg p-6 max-w-2xl w-full mx-4 max-h-[80vh] overflow-y-auto">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-xl font-semibold text-foreground">티켓 생성</h2>
            <button
              @click="showCreateModal = false"
              class="text-muted-foreground hover:text-foreground transition-colors"
            >
              <Close class="h-5 w-5" />
            </button>
          </div>
          <form @submit.prevent="createTicket" class="space-y-4">
            <!-- Current User Info -->
            <div class="bg-primary/10 border border-primary/20 rounded-lg p-3 mb-4">
              <p class="text-xs font-medium text-muted-foreground mb-2">작성자 정보 (자동 포함됨)</p>
              <div class="flex items-center gap-4 text-sm">
                <span class="inline-flex items-center gap-1 text-foreground">
                  <User class="h-3 w-3" />
                  ID: {{ currentUserId || 'N/A' }}
                </span>
                <span v-if="currentUsername" class="inline-flex items-center gap-1 text-foreground">
                  <UserFilled class="h-3 w-3" />
                  {{ currentUsername }}
                </span>
                <span v-if="currentUserRole" class="inline-flex items-center gap-1 text-foreground">
                  <Setting class="h-3 w-3" />
                  {{ currentUserRole }}
                </span>
              </div>
            </div>
            
            <div>
              <label class="text-sm font-medium text-foreground">제목 *</label>
              <input
                v-model="newTicket.subject"
                type="text"
                required
                class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
              />
            </div>
            <div>
              <label class="text-sm font-medium text-foreground">설명 *</label>
              <textarea
                v-model="newTicket.description"
                rows="5"
                required
                class="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
              ></textarea>
            </div>
            <div>
              <label class="text-sm font-medium text-foreground">우선순위</label>
              <select
                v-model="newTicket.priority"
                class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
              >
                <option value="LOW">낮음</option>
                <option value="NORMAL">보통</option>
                <option value="HIGH">높음</option>
                <option value="URGENT">긴급</option>
              </select>
            </div>
            <div class="flex gap-2">
              <button
                type="submit"
                :disabled="creating"
                class="flex-1 inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2"
              >
                <Loading v-if="creating" class="h-4 w-4 animate-spin" />
                <Check v-else class="h-4 w-4" />
                {{ creating ? '생성 중...' : '생성' }}
              </button>
              <button
                type="button"
                @click="showCreateModal = false"
                class="flex-1 inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium bg-secondary text-secondary-foreground hover:bg-secondary/80 h-10 px-4 py-2"
              >
                <Close class="h-4 w-4" />
                취소
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { supportTicketService, userService } from '../services/api'

export default {
  name: 'SupportTickets',
  data() {
    return {
      tickets: [],
      loading: false,
      filterPriority: '',
      showCreateModal: false,
      selectedTicket: null,
      newTicket: {
        subject: '',
        description: '',
        priority: 'NORMAL'
      },
      creating: false,
      currentUserId: null,
      currentUsername: null,
      currentUserRole: null,
      comments: [],
      loadingComments: false,
      showCommentForm: false,
      newComment: '',
      addingComment: false,
      currentPage: 0,
      pageSize: 10,
      pagination: null
    }
  },
  computed: {
    isAdmin() {
      // admin 계정(username이 'admin') 또는 ADMIN 역할이면 관리자로 인식
      return this.currentUserRole === 'ADMIN' || this.currentUsername === 'admin'
    },
    
    totalPages() {
      if (this.pagination) {
        return this.pagination.totalPages
      }
      return Math.ceil(this.tickets.length / this.pageSize)
    },
    
    paginatedTickets() {
      if (this.pagination) {
        // 서버 사이드 페이지네이션 사용
        return this.tickets
      }
      // 클라이언트 사이드 페이지네이션 (하위 호환성)
      const start = this.currentPage * this.pageSize
      const end = start + this.pageSize
      return this.tickets.slice(start, end)
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
    this.loadTickets()
    this.loadCurrentUserInfo()
  },
  methods: {
    loadCurrentUserInfo() {
      this.currentUserId = localStorage.getItem('userId')
      this.currentUsername = localStorage.getItem('username')
      this.currentUserRole = localStorage.getItem('userRole')
    },
    
    async loadTickets() {
      // authToken이 없으면 로그인 페이지로 리다이렉트 (alert 없이)
      if (!localStorage.getItem('authToken')) {
        this.$router.push('/login')
        return
      }
      
      this.loading = true
      try {
        let response
        if (this.filterPriority) {
          // 우선순위 필터가 있으면 우선순위별 조회
          response = await supportTicketService.getTicketsByPriority(this.filterPriority, this.currentPage, this.pageSize)
        } else {
          // 모든 사용자가 모든 티켓을 볼 수 있도록 전체 티켓 조회
          response = await supportTicketService.getAllTickets(null, this.currentPage, this.pageSize)
        }
        
        // 페이징 응답인지 확인
        let ticketsData = []
        if (response.data.content) {
          // 페이지네이션 응답
          ticketsData = response.data.content
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
          // 하위 호환성: 배열 응답
          ticketsData = response.data
          this.pagination = null
        }
        
        // 티켓 목록에 사용자 정보 추가 (description에서 파싱 및 API로 가져오기)
        this.tickets = await Promise.all(ticketsData.map(async (ticket) => {
          // description에서 사용자 정보 추출
          const userInfoMatch = ticket.description?.match(/--- 작성자 정보 ---\n사용자 ID: (\d+)\n사용자명: ([^\n]+)\n이메일: ([^\n]+)\n역할: ([^\n]+)/)
          if (userInfoMatch) {
            ticket.username = userInfoMatch[2] !== 'N/A' ? userInfoMatch[2] : null
            ticket.userEmail = userInfoMatch[3] !== 'N/A' ? userInfoMatch[3] : null
            ticket.userRole = userInfoMatch[4] !== 'N/A' ? userInfoMatch[4] : null
          } else {
            // 이전 형식 지원 (이메일 없는 경우)
            const oldFormatMatch = ticket.description?.match(/--- 작성자 정보 ---\n사용자 ID: (\d+)\n사용자명: ([^\n]+)\n역할: ([^\n]+)/)
            if (oldFormatMatch) {
              ticket.username = oldFormatMatch[2] !== 'N/A' ? oldFormatMatch[2] : null
              ticket.userRole = oldFormatMatch[3] !== 'N/A' ? oldFormatMatch[3] : null
            }
          }
          
          // userId로 사용자 정보 가져오기 (이메일 등 추가 정보, description에 없는 경우)
          if (ticket.userId && !ticket.userEmail) {
            try {
              const userResponse = await userService.getUser(ticket.userId)
              if (userResponse.data) {
                ticket.userEmail = userResponse.data.email || null
                if (!ticket.username && userResponse.data.username) {
                  ticket.username = userResponse.data.username
                }
                if (!ticket.userRole && userResponse.data.role) {
                  ticket.userRole = userResponse.data.role
                }
              }
            } catch (err) {
              // 사용자 정보 가져오기 실패는 조용히 처리
              console.warn('Failed to fetch user info for ticket:', ticket.id, err)
            }
          }
          
          // 댓글 개수 가져오기
          try {
            const commentsResponse = await supportTicketService.getComments(ticket.id)
            ticket.commentCount = commentsResponse.data?.length || 0
          } catch (err) {
            // 댓글 API가 아직 구현되지 않은 경우 조용히 처리
            ticket.commentCount = 0
          }
          
          return ticket
        }))
      } catch (err) {
        console.error('Error loading tickets:', err)
        alert('티켓 목록을 불러오는데 실패했습니다.')
      } finally {
        this.loading = false
      }
    },
    
    goToPage(page) {
      if (page >= 0 && (!this.pagination || page < this.pagination.totalPages)) {
        this.currentPage = page
        this.loadTickets()
        window.scrollTo({ top: 0, behavior: 'smooth' })
      }
    },
    
    handlePageSizeChange() {
      this.currentPage = 0
      this.loadTickets()
    },
    
    handleFilterChange() {
      this.currentPage = 0
      this.loadTickets()
    },
    
    async createTicket() {
      // 입력 검증
      if (!this.newTicket.subject || !this.newTicket.subject.trim()) {
        alert('제목을 입력해주세요.')
        return
      }
      
      if (!this.newTicket.description || !this.newTicket.description.trim()) {
        alert('내용을 입력해주세요.')
        return
      }

      this.creating = true
      try {
        // authToken이 없으면 로그인 페이지로 리다이렉트
        if (!localStorage.getItem('authToken')) {
          this.$router.push('/login')
          return
        }
        
        const userId = localStorage.getItem('userId')
        const username = localStorage.getItem('username')
        const userRole = localStorage.getItem('userRole')
        
        // userId가 없으면 기본값 사용
        if (!userId) {
          console.warn('userId가 없습니다. 기본값을 사용합니다.')
          return
        }
        
        // 사용자 이메일 가져오기
        let userEmail = null
        try {
          const userResponse = await userService.getUser(parseInt(userId))
          if (userResponse.data && userResponse.data.email) {
            userEmail = userResponse.data.email
          }
        } catch (err) {
          console.warn('Failed to fetch user email:', err)
        }
        
        // 사용자 정보를 포함한 설명 생성
        const userInfo = `\n\n--- 작성자 정보 ---\n사용자 ID: ${userId}\n사용자명: ${username || 'N/A'}\n이메일: ${userEmail || 'N/A'}\n역할: ${userRole || 'N/A'}\n생성 시간: ${new Date().toLocaleString('ko-KR')}\n---`
        
        // 데이터 정리
        const ticketData = {
          subject: this.newTicket.subject.trim(),
          description: this.newTicket.description.trim() + userInfo,
          priority: this.newTicket.priority || 'NORMAL'
        }
        
        const response = await supportTicketService.createTicket(ticketData, parseInt(userId))
        
        // 응답에 사용자 정보 추가
        if (response.data) {
          response.data.username = username
          response.data.userRole = userRole
          response.data.userEmail = userEmail
        }
        
        this.showCreateModal = false
        this.newTicket = { subject: '', description: '', priority: 'NORMAL' }
        await this.loadTickets()
        alert('티켓이 생성되었습니다.')
      } catch (err) {
        console.error('Error creating ticket:', err)
        // 백엔드 검증 에러 메시지 추출
        let errorMessage = '티켓 생성에 실패했습니다.'
        if (err.response?.data) {
          if (err.response.data.message) {
            errorMessage = err.response.data.message
          } else if (err.response.data.errors) {
            // Spring Validation 에러
            const validationErrors = err.response.data.errors
            errorMessage = validationErrors.map(e => e.defaultMessage || e.message).join(', ')
          }
        }
        alert(errorMessage)
      } finally {
        this.creating = false
      }
    },
    
    async viewTicketDetail(ticket) {
      this.selectedTicket = ticket
      this.showCommentForm = false
      this.newComment = ''
      
      // 사용자 정보가 없으면 가져오기
      if (ticket.userId && (!ticket.username || !ticket.userEmail)) {
        try {
          const userResponse = await userService.getUser(ticket.userId)
          if (userResponse.data) {
            this.selectedTicket.userEmail = userResponse.data.email || ticket.userEmail || null
            this.selectedTicket.username = userResponse.data.username || ticket.username || null
            this.selectedTicket.userRole = userResponse.data.role || ticket.userRole || null
          }
        } catch (err) {
          console.warn('Failed to fetch user info:', err)
        }
      }
      
      // description에서 사용자 정보 파싱 (없는 경우)
      if (!this.selectedTicket.username || !this.selectedTicket.userEmail) {
        const userInfoMatch = ticket.description?.match(/--- 작성자 정보 ---\n사용자 ID: (\d+)\n사용자명: ([^\n]+)\n이메일: ([^\n]+)\n역할: ([^\n]+)/)
        if (userInfoMatch) {
          this.selectedTicket.username = userInfoMatch[2] !== 'N/A' ? userInfoMatch[2] : this.selectedTicket.username
          this.selectedTicket.userEmail = userInfoMatch[3] !== 'N/A' ? userInfoMatch[3] : this.selectedTicket.userEmail
          this.selectedTicket.userRole = userInfoMatch[4] !== 'N/A' ? userInfoMatch[4] : this.selectedTicket.userRole
        } else {
          // 이전 형식 지원
          const oldFormatMatch = ticket.description?.match(/--- 작성자 정보 ---\n사용자 ID: (\d+)\n사용자명: ([^\n]+)\n역할: ([^\n]+)/)
          if (oldFormatMatch) {
            this.selectedTicket.username = oldFormatMatch[2] !== 'N/A' ? oldFormatMatch[2] : this.selectedTicket.username
            this.selectedTicket.userRole = oldFormatMatch[3] !== 'N/A' ? oldFormatMatch[3] : this.selectedTicket.userRole
          }
        }
      }
      
      // 댓글 로드
      await this.loadComments(ticket.id)
    },
    
    async loadComments(ticketId) {
      this.loadingComments = true
      try {
        const response = await supportTicketService.getComments(ticketId)
        // 댓글에 사용자 정보 추가
        this.comments = await Promise.all((response.data || []).map(async (comment) => {
          if (comment.userId) {
            try {
              const userResponse = await userService.getUser(comment.userId)
              if (userResponse.data) {
                comment.username = userResponse.data.username || null
                comment.userEmail = userResponse.data.email || null
              }
            } catch (err) {
              console.warn('Failed to fetch user info for comment:', err)
            }
          }
          return comment
        }))
      } catch (err) {
        // 댓글 API가 아직 구현되지 않은 경우 조용히 처리
        if (err.response?.status !== 404) {
          console.error('Error loading comments:', err)
        }
        this.comments = []
      } finally {
        this.loadingComments = false
      }
    },
    
    async addComment() {
      if (!this.newComment.trim()) {
        return
      }
      
      // authToken이 없으면 로그인 페이지로 리다이렉트
      if (!localStorage.getItem('authToken')) {
        this.$router.push('/login')
        return
      }
      
      const userId = localStorage.getItem('userId')
      if (!userId) {
        console.warn('userId가 없습니다.')
        return
      }
      
      this.addingComment = true
      try {
        await supportTicketService.createComment(
          this.selectedTicket.id,
          { comment: this.newComment.trim() },
          parseInt(userId)
        )
        this.newComment = ''
        this.showCommentForm = false
        await this.loadComments(this.selectedTicket.id)
        
        // 티켓 목록의 댓글 개수 업데이트
        const ticketIndex = this.tickets.findIndex(t => t.id === this.selectedTicket.id)
        if (ticketIndex !== -1) {
          this.tickets[ticketIndex].commentCount = this.comments.length
        }
      } catch (err) {
        console.error('Error adding comment:', err)
        const errorMessage = err.response?.data?.message || '답변 작성에 실패했습니다.'
        alert(errorMessage)
      } finally {
        this.addingComment = false
      }
    },
    
    async deleteComment(commentId) {
      if (!confirm('이 답변을 삭제하시겠습니까?')) {
        return
      }
      
      try {
        await supportTicketService.deleteComment(this.selectedTicket.id, commentId)
        await this.loadComments(this.selectedTicket.id)
        
        // 티켓 목록의 댓글 개수 업데이트
        const ticketIndex = this.tickets.findIndex(t => t.id === this.selectedTicket.id)
        if (ticketIndex !== -1) {
          this.tickets[ticketIndex].commentCount = this.comments.length
        }
      } catch (err) {
        console.error('Error deleting comment:', err)
        alert('답변 삭제에 실패했습니다.')
      }
    },
    
    canDeleteComment(comment) {
      const currentUserId = parseInt(localStorage.getItem('userId'))
      return this.isAdmin || (comment.userId === currentUserId)
    },
    
    async resolveTicket(id) {
      if (!confirm('이 티켓을 해결 처리하시겠습니까?')) return
      
      try {
        await supportTicketService.resolveTicket(id)
        await this.loadTickets()
        // 선택된 티켓이 해결된 경우 상태 업데이트
        if (this.selectedTicket && this.selectedTicket.id === id) {
          this.selectedTicket.status = 'RESOLVED'
          this.selectedTicket.resolvedAt = new Date().toISOString()
        }
        alert('티켓이 해결 처리되었습니다.')
      } catch (err) {
        console.error('Error resolving ticket:', err)
        alert('티켓 해결 처리에 실패했습니다.')
      }
    },
    
    getPriorityClass(priority) {
      const classes = {
        LOW: 'bg-gray-500/10 text-gray-700 dark:text-gray-400',
        NORMAL: 'bg-blue-500/10 text-blue-700 dark:text-blue-400',
        HIGH: 'bg-orange-500/10 text-orange-700 dark:text-orange-400',
        URGENT: 'bg-red-500/10 text-red-700 dark:text-red-400'
      }
      return classes[priority] || classes.NORMAL
    },
    
    getStatusClass(status) {
      const classes = {
        OPEN: 'bg-yellow-500/10 text-yellow-700 dark:text-yellow-400',
        IN_PROGRESS: 'bg-blue-500/10 text-blue-700 dark:text-blue-400',
        RESOLVED: 'bg-green-500/10 text-green-700 dark:text-green-400',
        CLOSED: 'bg-gray-500/10 text-gray-700 dark:text-gray-400'
      }
      return classes[status] || classes.OPEN
    },
    
    formatDate(dateString) {
      if (!dateString) return '-'
      return new Date(dateString).toLocaleString('ko-KR')
    }
  }
}
</script>

