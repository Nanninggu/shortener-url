<template>
  <div class="container mx-auto px-4 py-12 max-w-7xl">
    <div class="space-y-8">
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-foreground mb-2">API 키 관리</h1>
          <p class="text-muted-foreground">API 키를 생성하고 관리합니다</p>
        </div>
        <button
          @click="showCreateModal = true"
          class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2"
        >
          <Plus class="h-4 w-4" />
          API 키 생성
        </button>
      </div>

      <!-- API Keys List -->
      <div class="bg-card border rounded-lg shadow-sm p-6">
        <h2 class="text-xl font-semibold text-foreground mb-4">내 API 키 목록</h2>
        
        <div v-if="loading" class="text-center py-12">
          <p class="text-muted-foreground">로딩 중...</p>
        </div>

        <div v-else-if="apiKeys.length === 0" class="text-center py-12">
          <p class="text-muted-foreground">API 키가 없습니다. 새 API 키를 생성해보세요.</p>
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full border-collapse">
            <thead>
              <tr class="border-b bg-muted/50">
                <th class="h-12 px-4 text-left font-medium text-muted-foreground">이름</th>
                <th class="h-12 px-4 text-left font-medium text-muted-foreground">API 키</th>
                <th class="h-12 px-4 text-left font-medium text-muted-foreground">Rate Limit</th>
                <th class="h-12 px-4 text-left font-medium text-muted-foreground">상태</th>
                <th class="h-12 px-4 text-left font-medium text-muted-foreground">생성일</th>
                <th class="h-12 px-4 text-center font-medium text-muted-foreground">작업</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="key in apiKeys"
                :key="key.id"
                class="border-b hover:bg-muted/50"
              >
                <td class="p-4">{{ key.keyName }}</td>
                <td class="p-4">
                  <code class="text-sm font-mono bg-muted px-2 py-1 rounded">{{ key.apiKey }}</code>
                  <button
                    @click="copyToClipboard(key.apiKey)"
                    class="inline-flex items-center gap-1 ml-2 text-xs text-primary hover:underline"
                  >
                    <CopyDocument class="h-3 w-3" />
                    복사
                  </button>
                </td>
                <td class="p-4">{{ key.rateLimit || 'N/A' }}</td>
                <td class="p-4">
                  <span
                    :class="key.isActive ? 'bg-green-500/10 text-green-700 dark:text-green-400' : 'bg-red-500/10 text-red-700 dark:text-red-400'"
                    class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium"
                  >
                    {{ key.isActive ? '활성' : '비활성' }}
                  </span>
                </td>
                <td class="p-4 text-sm text-muted-foreground">
                  {{ formatDate(key.createdAt) }}
                </td>
                <td class="p-4 text-center">
                  <div class="flex gap-2 justify-center">
                    <button
                      v-if="key.isActive"
                      @click="deactivateKey(key.id)"
                      class="inline-flex items-center gap-1 text-xs px-2 py-1 bg-secondary text-secondary-foreground rounded-md hover:bg-secondary/80 ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                    >
                      <Lock class="h-3 w-3" />
                      비활성화
                    </button>
                    <button
                      v-else
                      @click="activateKey(key.id)"
                      class="inline-flex items-center gap-1 text-xs px-2 py-1 bg-green-600 text-white rounded-md hover:bg-green-700 ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                    >
                      <Unlock class="h-3 w-3" />
                      활성화
                    </button>
                    <button
                      @click="deleteKey(key.id)"
                      class="inline-flex items-center gap-1 text-xs px-2 py-1 bg-destructive text-destructive-foreground rounded hover:bg-destructive/90"
                    >
                      <Delete class="h-3 w-3" />
                      삭제
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Create API Key Modal -->
      <div v-if="showCreateModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showCreateModal = false">
        <div class="bg-card border rounded-lg shadow-lg p-6 max-w-md w-full mx-4">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-xl font-semibold text-foreground">API 키 생성</h2>
            <button
              @click="showCreateModal = false"
              class="text-muted-foreground hover:text-foreground transition-colors"
            >
              <Close class="h-5 w-5" />
            </button>
          </div>
          <form @submit.prevent="createApiKey" class="space-y-4">
            <div>
              <label class="text-sm font-medium text-foreground">키 이름 *</label>
              <input
                v-model="newKey.keyName"
                type="text"
                required
                class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
              />
            </div>
            <div>
              <label class="text-sm font-medium text-foreground">팀 (선택사항)</label>
              <select
                v-model.number="newKey.teamId"
                class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
              >
                <option :value="null">팀 없음 (개인용)</option>
                <option
                  v-for="team in teams"
                  :key="team.id"
                  :value="team.id"
                >
                  {{ team.name }} (ID: {{ team.id }})
                </option>
              </select>
              <p class="text-xs text-muted-foreground mt-1">API 키를 특정 팀에 연결하려면 팀을 선택하세요.</p>
            </div>
            <div>
              <label class="text-sm font-medium text-foreground">Rate Limit</label>
              <input
                v-model.number="newKey.rateLimit"
                type="number"
                placeholder="예: 1000"
                class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
              />
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
import { apiKeyService, teamService, userService } from '../services/api'
import { Plus, CopyDocument, Lock, Unlock, Delete, Close, Check, Loading } from '@element-plus/icons-vue'

export default {
  name: 'ApiKeyManagement',
  components: {
    Plus, CopyDocument, Lock, Unlock, Delete, Close, Check, Loading
  },
  data() {
    return {
      apiKeys: [],
      teams: [],
      loading: false,
      loadingTeams: false,
      showCreateModal: false,
      newKey: {
        keyName: '',
        teamId: null,
        rateLimit: null
      },
      creating: false
    }
  },
  mounted() {
    this.loadApiKeys()
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
        }
      }
      
      return null
    },
    
    async loadApiKeys() {
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
        const response = await apiKeyService.getApiKeysByUser(parseInt(userId))
        this.apiKeys = response.data
      } catch (err) {
        console.error('Error loading API keys:', err)
        alert('API 키 목록을 불러오는데 실패했습니다.')
      } finally {
        this.loading = false
      }
    },
    
    async loadTeams() {
      const userId = await this.getUserId()
      if (!userId) {
        return
      }
      this.loadingTeams = true
      try {
        const response = await teamService.getTeamsByOwner(parseInt(userId))
        this.teams = response.data
      } catch (err) {
        console.error('Error loading teams:', err)
        // 팀 목록 로딩 실패는 조용히 처리 (선택사항이므로)
      } finally {
        this.loadingTeams = false
      }
    },
    
    async createApiKey() {
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
        const response = await apiKeyService.createApiKey(
          this.newKey.keyName,
          parseInt(userId),
          this.newKey.teamId || null,
          null,
          this.newKey.rateLimit || null
        )
        this.showCreateModal = false
        this.newKey = { keyName: '', teamId: null, rateLimit: null }
        await this.loadApiKeys()
        alert('API 키가 생성되었습니다. 키: ' + response.data.apiKey)
      } catch (err) {
        console.error('Error creating API key:', err)
        alert('API 키 생성에 실패했습니다.')
      } finally {
        this.creating = false
      }
    },
    
    async deactivateKey(id) {
      if (!confirm('이 API 키를 비활성화하시겠습니까?')) return
      
      try {
        await apiKeyService.deactivateApiKey(id)
        await this.loadApiKeys()
        alert('API 키가 비활성화되었습니다.')
      } catch (err) {
        console.error('Error deactivating API key:', err)
        alert('API 키 비활성화에 실패했습니다.')
      }
    },
    
    async activateKey(id) {
      if (!confirm('이 API 키를 활성화하시겠습니까?')) return
      
      try {
        await apiKeyService.activateApiKey(id)
        await this.loadApiKeys()
        alert('API 키가 활성화되었습니다.')
      } catch (err) {
        console.error('Error activating API key:', err)
        alert('API 키 활성화에 실패했습니다.')
      }
    },
    
    async deleteKey(id) {
      if (!confirm('이 API 키를 삭제하시겠습니까?')) return
      
      try {
        await apiKeyService.deleteApiKey(id)
        await this.loadApiKeys()
        alert('API 키가 삭제되었습니다.')
      } catch (err) {
        console.error('Error deleting API key:', err)
        alert('API 키 삭제에 실패했습니다.')
      }
    },
    
    async copyToClipboard(text) {
      try {
        await navigator.clipboard.writeText(text)
        alert('클립보드에 복사되었습니다.')
      } catch (err) {
        console.error('Failed to copy:', err)
      }
    },
    
    formatDate(dateString) {
      if (!dateString) return '-'
      return new Date(dateString).toLocaleDateString('ko-KR')
    }
  }
}
</script>

