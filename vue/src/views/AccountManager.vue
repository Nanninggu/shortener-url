<template>
  <div class="container mx-auto px-4 py-12 max-w-7xl">
    <div class="space-y-8">
      <!-- Header -->
      <div>
        <h1 class="text-3xl font-bold text-foreground mb-2">계정 관리자</h1>
        <p class="text-muted-foreground">사용자 계정 관리 및 통계</p>
      </div>

      <!-- User Search -->
      <div class="bg-card border rounded-lg shadow-sm p-4">
        <div class="flex gap-4">
          <div class="flex-1 relative">
            <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
            <select
            v-model.number="searchUserId"
              @change="loadUserData"
              class="flex h-10 w-full rounded-md border border-input bg-background pl-10 pr-8 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 appearance-none"
            >
              <option :value="null">사용자 선택</option>
              <option
                v-for="user in allUsers"
                :key="user.id"
                :value="user.id"
              >
                ID: {{ user.id }} - {{ user.username }} ({{ user.email }})
              </option>
            </select>
          </div>
          <button
            @click="loadUserData"
            :disabled="!searchUserId"
            class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2"
          >
            <Search class="h-4 w-4" />
            조회
          </button>
        </div>
        <div v-if="loadingUsers" class="mt-2 text-sm text-muted-foreground">
          사용자 목록 로딩 중...
        </div>
        <div v-else-if="allUsers.length > 0" class="mt-2 text-sm text-muted-foreground">
          총 {{ allUsers.length }}명의 사용자
        </div>
      </div>

      <!-- User Info -->
      <div v-if="userAccount" class="bg-card border rounded-lg shadow-sm p-6">
        <h2 class="text-xl font-semibold text-foreground mb-4">사용자 정보</h2>
        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <p class="text-sm text-muted-foreground">사용자명</p>
            <p class="text-lg font-semibold text-foreground">{{ userAccount.username }}</p>
          </div>
          <div>
            <p class="text-sm text-muted-foreground">이메일</p>
            <p class="text-lg font-semibold text-foreground">{{ userAccount.email }}</p>
          </div>
          <div>
            <p class="text-sm text-muted-foreground">역할</p>
            <div class="flex items-center gap-2">
              <select
                v-model="selectedRole"
                @change="updateUserRole"
                :disabled="updatingRole"
                class="flex h-10 rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              >
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
              </select>
              <span
                v-if="updatingRole"
                class="text-sm text-muted-foreground"
              >
                변경 중...
              </span>
            </div>
          </div>
          <div>
            <p class="text-sm text-muted-foreground">계정 상태</p>
            <span
              :class="userAccount.enabled ? 'bg-green-500/10 text-green-700 dark:text-green-400' : 'bg-red-500/10 text-red-700 dark:text-red-400'"
              class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium"
            >
              {{ userAccount.enabled ? '활성' : '비활성' }}
            </span>
          </div>
        </div>
        <div class="mt-4 flex gap-2">
          <button
            @click="toggleUserStatus"
            class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2"
          >
            <Switch v-if="userAccount.enabled" class="h-4 w-4" />
            <Unlock v-else class="h-4 w-4" />
            계정 {{ userAccount.enabled ? '비활성화' : '활성화' }}
          </button>
        </div>
      </div>

      <!-- User Stats -->
      <div v-if="userStats" class="bg-card border rounded-lg shadow-sm p-6">
        <h2 class="text-xl font-semibold text-foreground mb-4">사용자 통계</h2>
        <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
          <div>
            <p class="text-sm text-muted-foreground">총 URL 수</p>
            <p class="text-3xl font-bold text-foreground mt-2">{{ userStats.totalUrls }}</p>
          </div>
          <div>
            <p class="text-sm text-muted-foreground">총 클릭 수</p>
            <p class="text-3xl font-bold text-foreground mt-2">{{ userStats.totalClicks }}</p>
          </div>
          <div>
            <p class="text-sm text-muted-foreground">활성 URL</p>
            <p class="text-3xl font-bold text-foreground mt-2">{{ userStats.activeUrls }}</p>
          </div>
          <div>
            <p class="text-sm text-muted-foreground">만료된 URL</p>
            <p class="text-3xl font-bold text-foreground mt-2">{{ userStats.expiredUrls }}</p>
          </div>
        </div>
      </div>

      <!-- User URLs -->
      <div v-if="userUrls.length > 0" class="bg-card border rounded-lg shadow-sm p-6">
        <h2 class="text-xl font-semibold text-foreground mb-4">사용자 URL 목록 ({{ userUrls.length }})</h2>
        <div class="overflow-x-auto">
          <table class="w-full border-collapse">
            <thead>
              <tr class="border-b bg-muted/50">
                <th class="h-12 px-4 text-left font-medium text-muted-foreground">원본 URL</th>
                <th class="h-12 px-4 text-left font-medium text-muted-foreground">단축 URL</th>
                <th class="h-12 px-4 text-center font-medium text-muted-foreground">클릭 수</th>
                <th class="h-12 px-4 text-left font-medium text-muted-foreground">생성일</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="url in userUrls"
                :key="url.id"
                class="border-b hover:bg-muted/50"
              >
                <td class="p-4">
                  <div class="max-w-xs truncate text-sm" :title="url.originalUrl">
                    {{ url.originalUrl }}
                  </div>
                </td>
                <td class="p-4">
                  <a
                    :href="url.shortUrl"
                    target="_blank"
                    class="text-sm text-primary hover:underline"
                  >
                    {{ url.shortUrl }}
                  </a>
                </td>
                <td class="p-4 text-center">
                  <span class="inline-flex items-center rounded-full bg-primary/10 px-2.5 py-0.5 text-xs font-medium text-primary">
                    {{ url.clickCount }}
                  </span>
                </td>
                <td class="p-4 text-sm text-muted-foreground">
                  {{ formatDate(url.createdAt) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- User Tickets -->
      <div v-if="userTickets.length > 0" class="bg-card border rounded-lg shadow-sm p-6">
        <h2 class="text-xl font-semibold text-foreground mb-4">사용자 지원 티켓 ({{ userTickets.length }})</h2>
        <div class="space-y-4">
          <div
            v-for="ticket in userTickets"
            :key="ticket.id"
            class="border rounded-lg p-4"
          >
            <div class="flex items-center gap-2 mb-2">
              <h3 class="font-semibold text-foreground">{{ ticket.subject }}</h3>
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
            <p class="text-sm text-muted-foreground">{{ ticket.description }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { accountManagerService, userService } from '../services/api'

export default {
  name: 'AccountManager',
  data() {
    return {
      searchUserId: null,
      userAccount: null,
      userStats: null,
      userUrls: [],
      userTickets: [],
      allUsers: [],
      loadingUsers: false,
      selectedRole: null,
      updatingRole: false
    }
  },
  mounted() {
    this.loadAllUsers()
  },
  methods: {
    async loadAllUsers() {
      this.loadingUsers = true
      try {
        const response = await userService.getAllUsers()
        this.allUsers = response.data || []
      } catch (err) {
        console.error('Error loading users:', err)
        alert('사용자 목록을 불러오는데 실패했습니다.')
      } finally {
        this.loadingUsers = false
      }
    },
    
    async loadUserData() {
      if (!this.searchUserId) {
        // 사용자가 선택되지 않았으면 데이터 초기화
        this.userAccount = null
        this.userStats = null
        this.userUrls = []
        this.userTickets = []
        return
      }

      try {
        const [accountRes, statsRes, urlsRes, ticketsRes] = await Promise.all([
          accountManagerService.getUserAccount(this.searchUserId).catch(() => ({ data: null })),
          accountManagerService.getUserStats(this.searchUserId).catch(() => ({ data: null })),
          accountManagerService.getUserUrls(this.searchUserId).catch(() => ({ data: [] })),
          accountManagerService.getUserTickets(this.searchUserId).catch(() => ({ data: [] }))
        ])

        this.userAccount = accountRes.data
        this.selectedRole = accountRes.data?.role || null
        this.userStats = statsRes.data
        this.userUrls = urlsRes.data || []
        this.userTickets = ticketsRes.data || []
      } catch (err) {
        console.error('Error loading user data:', err)
        alert('사용자 정보를 불러오는데 실패했습니다.')
      }
    },
    
    async toggleUserStatus() {
      if (!this.userAccount) return
      
      if (!confirm(`사용자 계정을 ${this.userAccount.enabled ? '비활성화' : '활성화'}하시겠습니까?`)) return
      
      try {
        await accountManagerService.toggleUserStatus(this.userAccount.id)
        await this.loadUserData()
        alert('계정 상태가 변경되었습니다.')
      } catch (err) {
        console.error('Error toggling user status:', err)
        alert('계정 상태 변경에 실패했습니다.')
      }
    },
    
    async updateUserRole() {
      if (!this.userAccount || !this.selectedRole) return
      
      // 역할이 변경되지 않았으면 무시
      if (this.selectedRole === this.userAccount.role) return
      
      const roleText = this.selectedRole === 'ADMIN' ? '관리자' : '일반 사용자'
      if (!confirm(`사용자 역할을 ${roleText}로 변경하시겠습니까?`)) {
        // 취소하면 원래 값으로 복원
        this.selectedRole = this.userAccount.role
        return
      }
      
      this.updatingRole = true
      try {
        const response = await accountManagerService.updateUserRole(this.userAccount.id, this.selectedRole)
        await this.loadUserData()
        alert(response.data?.message || '사용자 역할이 변경되었습니다.')
      } catch (err) {
        console.error('Error updating user role:', err)
        const errorMessage = err.response?.data?.error || '역할 변경에 실패했습니다.'
        alert(errorMessage)
        // 에러 발생 시 원래 값으로 복원
        this.selectedRole = this.userAccount.role
      } finally {
        this.updatingRole = false
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

