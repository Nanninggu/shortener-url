<template>
  <div class="container mx-auto px-4 py-12 max-w-7xl">
    <div class="space-y-8">
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-foreground mb-2">팀 관리</h1>
          <p class="text-muted-foreground">팀을 생성하고 멤버를 관리합니다</p>
        </div>
        <button
          @click="showCreateModal = true"
          class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2"
        >
          <Plus class="h-4 w-4" />
          팀 생성
        </button>
      </div>

      <!-- Teams List -->
      <div class="bg-card border rounded-lg shadow-sm p-6">
        <h2 class="text-xl font-semibold text-foreground mb-4">내 팀 목록</h2>
        
        <div v-if="loading" class="text-center py-12">
          <p class="text-muted-foreground">로딩 중...</p>
        </div>

        <div v-else-if="teams.length === 0" class="text-center py-12">
          <p class="text-muted-foreground">팀이 없습니다. 새 팀을 생성해보세요.</p>
        </div>

        <div v-else class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          <div
            v-for="team in teams"
            :key="team.id"
            class="border rounded-lg p-4 hover:shadow-md transition-shadow"
          >
            <h3 class="text-lg font-semibold text-foreground mb-2">{{ team.name }}</h3>
            <p class="text-sm text-muted-foreground mb-4">ID: {{ team.id }}</p>
            <div class="flex gap-2">
              <button
                @click="viewTeamMembers(team)"
                class="inline-flex items-center gap-1 flex-1 justify-center text-sm px-3 py-1 bg-secondary text-secondary-foreground rounded hover:bg-secondary/80"
              >
                <User class="h-4 w-4" />
                멤버 관리
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Create Team Modal -->
      <div v-if="showCreateModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
        <div class="bg-card border rounded-lg shadow-lg p-6 max-w-md w-full mx-4">
          <h2 class="text-xl font-semibold text-foreground mb-4">팀 생성</h2>
          <form @submit.prevent="createTeam" class="space-y-4">
            <div>
              <label class="text-sm font-medium text-foreground">팀 이름 *</label>
              <input
                v-model="newTeam.name"
                type="text"
                placeholder="예: 개발팀, 마케팅팀"
                required
                class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
              />
              <p class="text-xs text-muted-foreground mt-1">팀 이름을 입력하세요. 소유자는 현재 로그인한 사용자로 자동 설정됩니다.</p>
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

      <!-- Team Members Modal -->
      <div v-if="selectedTeam" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
        <div class="bg-card border rounded-lg shadow-lg p-6 max-w-2xl w-full mx-4 max-h-[80vh] overflow-y-auto">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-xl font-semibold text-foreground">{{ selectedTeam.name }} - 멤버 관리</h2>
            <button
              @click="selectedTeam = null"
              class="text-muted-foreground hover:text-foreground"
            >
              <Close class="h-5 w-5" />
            </button>
          </div>
          
          <div class="space-y-4">
            <!-- Current Members List -->
            <div v-if="teamMembers.length > 0">
              <h3 class="text-sm font-semibold text-foreground mb-3">현재 멤버 ({{ teamMembers.length }})</h3>
              <div class="space-y-2">
                <div
                  v-for="member in teamMembers"
                  :key="member.id"
                  class="flex items-center justify-between p-3 bg-muted/50 rounded-md border"
                >
                  <div class="flex items-center gap-3">
                    <Avatar class="h-8 w-8 text-muted-foreground" />
                    <div>
                      <p class="text-sm font-medium text-foreground">
                        {{ member.username || `사용자 #${member.userId}` }}
                      </p>
                      <p class="text-xs text-muted-foreground">
                        ID: {{ member.userId }} | 역할: {{ member.role }}
                      </p>
                    </div>
                  </div>
                  <button
                    @click="removeMember(member.userId)"
                    class="inline-flex items-center gap-1 text-xs px-2 py-1 bg-destructive text-destructive-foreground rounded-md hover:bg-destructive/90"
                  >
                    <Delete class="h-3 w-3" />
                    제거
                  </button>
                </div>
              </div>
            </div>

            <!-- Add Member Section -->
            <div>
              <label class="text-sm font-medium text-foreground">멤버 추가</label>
              <div v-if="loadingUsers" class="text-sm text-muted-foreground mt-1 py-2">
                <Loading class="inline-block h-4 w-4 animate-spin mr-2" />
                사용자 목록 로딩 중...
              </div>
              <div v-else-if="availableUsers.length === 0 && allUsers.length > 0" class="text-sm text-muted-foreground mt-1 py-2">
                모든 사용자가 이미 팀 멤버입니다.
              </div>
              <div v-else-if="allUsers.length === 0" class="text-sm text-muted-foreground mt-1 py-2">
                사용자 목록을 불러올 수 없습니다. (관리자 권한이 필요할 수 있습니다)
              </div>
              <div v-else class="flex gap-2 mt-1">
                <select
                  v-model="newMemberId"
                  class="flex h-10 flex-1 rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                >
                  <option value="">사용자 선택</option>
                  <option
                    v-for="user in availableUsers"
                    :key="user.id"
                    :value="user.id"
                  >
                    {{ user.username }} ({{ user.email }}) - ID: {{ user.id }}
                  </option>
                </select>
                <select
                  v-model="newMemberRole"
                  class="flex h-10 rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                >
                  <option value="MEMBER">멤버</option>
                  <option value="ADMIN">관리자</option>
                </select>
                <button
                  @click="addMember"
                  :disabled="addingMember || !newMemberId"
                  class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2"
                >
                  <Loading v-if="addingMember" class="h-4 w-4 animate-spin" />
                  <Plus v-else class="h-4 w-4" />
                  추가
                </button>
              </div>
              <p class="text-xs text-muted-foreground mt-1">사용자를 선택하고 역할을 지정한 후 추가하세요.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { teamService, userService } from '../services/api'

export default {
  name: 'TeamManagement',
  data() {
    return {
      teams: [],
      loading: false,
      showCreateModal: false,
      newTeam: {
        name: ''
      },
      creating: false,
      selectedTeam: null,
      teamMembers: [],
      allUsers: [],
      loadingUsers: false,
      newMemberId: '',
      newMemberRole: 'MEMBER',
      addingMember: false
    }
  },
  computed: {
    availableUsers() {
      // 이미 팀 멤버인 사용자 제외
      const memberUserIds = this.teamMembers.map(m => m.userId)
      return this.allUsers.filter(user => !memberUserIds.includes(user.id))
    }
  },
  mounted() {
    this.loadTeams()
  },
  methods: {
    // userId를 가져오는 헬퍼 메서드 (JWT 토큰에서 추출 시도)
    async getUserId() {
      // 먼저 localStorage에서 확인
      let userId = localStorage.getItem('userId')
      if (userId) {
        return userId
      }
      
      // JWT 토큰에서 userId 추출 시도
      const token = localStorage.getItem('authToken')
      if (token) {
        try {
          const payload = JSON.parse(atob(token.split('.')[1]))
          if (payload.userId) {
            userId = payload.userId.toString()
            localStorage.setItem('userId', userId)
            return userId
          }
        } catch (err) {
          console.warn('JWT 토큰 파싱 실패:', err)
        }
      }
      
      // username으로 사용자 조회
      const username = localStorage.getItem('username')
      if (username) {
        try {
          const userResponse = await userService.getUserByUsername(username)
          if (userResponse.data && userResponse.data.id) {
            userId = userResponse.data.id.toString()
            localStorage.setItem('userId', userId)
            return userId
          }
        } catch (err) {
          // 사용자를 찾을 수 없는 경우 (하드코딩된 admin 계정일 수 있음)
          console.warn('사용자 조회 실패:', err)
          // admin 계정의 경우 userId 없이도 작동하도록 하려면 여기서 처리
        }
      }
      
      return null
    },
    
    async loadTeams() {
      // authToken이 없으면 로그인 페이지로 리다이렉트 (alert 없이)
      if (!localStorage.getItem('authToken')) {
        this.$router.push('/login')
        return
      }
      
      const userId = await this.getUserId()
      if (!userId) {
        console.warn('userId를 가져올 수 없습니다.')
        return
      }
      this.loading = true
      try {
        const response = await teamService.getTeamsByOwner(parseInt(userId))
        this.teams = response.data
      } catch (err) {
        console.error('Error loading teams:', err)
        alert('팀 목록을 불러오는데 실패했습니다.')
      } finally {
        this.loading = false
      }
    },
    
    async createTeam() {
      if (!this.newTeam.name || !this.newTeam.name.trim()) {
        alert('팀 이름을 입력해주세요.')
        return
      }
      
      // authToken이 없으면 로그인 페이지로 리다이렉트
      if (!localStorage.getItem('authToken')) {
        this.$router.push('/login')
        return
      }
      
      const userId = await this.getUserId()
      if (!userId) {
        alert('사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요.')
        return
      }

      this.creating = true
      try {
        const teamData = {
          name: this.newTeam.name.trim()
        }
        await teamService.createTeam(teamData, parseInt(userId))
        this.showCreateModal = false
        this.newTeam = { name: '' }
        await this.loadTeams()
        alert('팀이 생성되었습니다.')
      } catch (err) {
        console.error('Error creating team:', err)
        const errorMessage = err.response?.data?.message || err.message || '팀 생성에 실패했습니다.'
        alert(errorMessage)
      } finally {
        this.creating = false
      }
    },
    
    async viewTeamMembers(team) {
      this.selectedTeam = team
      this.newMemberId = ''
      this.newMemberRole = 'MEMBER'
      await this.loadTeamMembers(team.id)
      await this.loadAllUsers()
    },
    
    async loadTeamMembers(teamId) {
      try {
        const response = await teamService.getTeamMembers(teamId)
        // 팀 멤버에 사용자 정보 추가
        this.teamMembers = await Promise.all((response.data || []).map(async (member) => {
          try {
            const userResponse = await userService.getUser(member.userId)
            if (userResponse.data) {
              member.username = userResponse.data.username
              member.email = userResponse.data.email
            }
          } catch (err) {
            console.warn('Failed to fetch user info for member:', member.userId, err)
          }
          return member
        }))
      } catch (err) {
        console.error('Error loading team members:', err)
        this.teamMembers = []
      }
    },
    
    async loadAllUsers() {
      this.loadingUsers = true
      try {
        const response = await userService.getAllUsers()
        this.allUsers = response.data || []
      } catch (err) {
        console.error('Error loading users:', err)
        // ADMIN 권한이 없을 수 있으므로 조용히 처리
        this.allUsers = []
      } finally {
        this.loadingUsers = false
      }
    },
    
    async addMember() {
      if (!this.newMemberId) {
        alert('사용자를 선택해주세요.')
        return
      }
      
      const userId = parseInt(this.newMemberId)
      if (isNaN(userId) || userId <= 0) {
        alert('올바른 사용자를 선택해주세요.')
        return
      }
      
      this.addingMember = true
      try {
        await teamService.addMember(this.selectedTeam.id, userId, this.newMemberRole)
        this.newMemberId = ''
        this.newMemberRole = 'MEMBER'
        await this.loadTeamMembers(this.selectedTeam.id)
        alert('멤버가 추가되었습니다.')
      } catch (err) {
        console.error('Error adding member:', err)
        const errorMessage = err.response?.data?.message || '멤버 추가에 실패했습니다.'
        alert(errorMessage)
      } finally {
        this.addingMember = false
      }
    },
    
    async removeMember(userId) {
      if (!confirm('이 멤버를 팀에서 제거하시겠습니까?')) {
        return
      }
      
      try {
        await teamService.removeMember(this.selectedTeam.id, userId)
        await this.loadTeamMembers(this.selectedTeam.id)
        alert('멤버가 제거되었습니다.')
      } catch (err) {
        console.error('Error removing member:', err)
        alert('멤버 제거에 실패했습니다.')
      }
    }
  }
}
</script>

