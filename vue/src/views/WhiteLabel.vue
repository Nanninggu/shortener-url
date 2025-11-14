<template>
  <div class="container mx-auto px-4 py-12 max-w-6xl">
    <div class="space-y-8">
      <!-- Header -->
      <div>
        <h1 class="text-3xl font-bold text-foreground mb-2">화이트 라벨 설정</h1>
        <p class="text-muted-foreground">브랜드 도메인 및 커스터마이징 설정</p>
      </div>

      <!-- Settings Form -->
      <div class="bg-card border rounded-lg shadow-sm p-6 sm:p-8">
        <form @submit.prevent="saveSettings" class="space-y-6">
          <div class="space-y-4">
            <h2 class="text-xl font-semibold text-foreground border-b pb-2">브랜드 설정</h2>
            
            <div class="space-y-2">
              <label class="text-sm font-medium text-foreground">브랜드 도메인 *</label>
              <input
                v-model="settings.domain"
                type="text"
                placeholder="example.com"
                required
                class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              />
            </div>

            <div class="space-y-2">
              <label class="text-sm font-medium text-foreground">브랜드 이름</label>
              <input
                v-model="settings.brandName"
                type="text"
                placeholder="My Brand"
                class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              />
            </div>

            <div class="space-y-2">
              <label class="text-sm font-medium text-foreground">로고 URL</label>
              <input
                v-model="settings.logoUrl"
                type="url"
                placeholder="https://example.com/logo.png"
                class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              />
            </div>
          </div>

          <div class="space-y-4">
            <h2 class="text-xl font-semibold text-foreground border-b pb-2">색상 설정</h2>
            
            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-2">
                <label class="text-sm font-medium text-foreground">주요 색상</label>
                <input
                  v-model="settings.primaryColor"
                  type="color"
                  class="flex h-10 w-full rounded-md border border-input bg-background"
                />
              </div>
              <div class="space-y-2">
                <label class="text-sm font-medium text-foreground">보조 색상</label>
                <input
                  v-model="settings.secondaryColor"
                  type="color"
                  class="flex h-10 w-full rounded-md border border-input bg-background"
                />
              </div>
            </div>
          </div>

          <div class="space-y-4">
            <h2 class="text-xl font-semibold text-foreground border-b pb-2">커스텀 CSS</h2>
            
            <div class="space-y-2">
              <label class="text-sm font-medium text-foreground">CSS 코드</label>
              <textarea
                v-model="settings.customCss"
                rows="10"
                placeholder=".custom-class { color: #000; }"
                class="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm font-mono"
              ></textarea>
            </div>
          </div>

          <div class="flex gap-4">
            <button
              type="submit"
              :disabled="saving"
              class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2 flex-1"
            >
              <Loading v-if="saving" class="h-4 w-4 animate-spin" />
              <Check v-else class="h-4 w-4" />
              {{ saving ? '저장 중...' : '설정 저장' }}
            </button>
            <button
              type="button"
              @click="loadSettings"
              class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium bg-secondary text-secondary-foreground hover:bg-secondary/80 h-10 px-4 py-2"
            >
              <RefreshRight class="h-4 w-4" />
              새로고침
            </button>
          </div>
        </form>

        <!-- Success Message -->
        <div v-if="success" class="mt-4 rounded-md bg-primary/10 border border-primary/20 p-4">
          <p class="text-sm text-foreground">설정이 저장되었습니다.</p>
        </div>

        <!-- Error Message -->
        <div v-if="error" class="mt-4 rounded-md bg-destructive/10 border border-destructive/20 p-4">
          <p class="text-sm text-destructive">{{ error }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { whiteLabelService } from '../services/api'

export default {
  name: 'WhiteLabel',
  data() {
    return {
      settings: {
        domain: '',
        brandName: '',
        logoUrl: '',
        primaryColor: '#000000',
        secondaryColor: '#ffffff',
        customCss: ''
      },
      saving: false,
      success: false,
      error: ''
    }
  },
  mounted() {
    this.loadSettings()
  },
  methods: {
    async loadSettings() {
      // authToken이 없으면 로그인 페이지로 리다이렉트 (alert 없이)
      if (!localStorage.getItem('authToken')) {
        this.$router.push('/login')
        return
      }
      
      const userId = localStorage.getItem('userId')
      if (!userId) {
        console.warn('userId가 없습니다.')
        return
      }
      try {
        const response = await whiteLabelService.getSettingsByUser(parseInt(userId))
        if (response.data) {
          this.settings = {
            domain: response.data.domain || '',
            brandName: response.data.brandName || '',
            logoUrl: response.data.logoUrl || '',
            primaryColor: response.data.primaryColor || '#000000',
            secondaryColor: response.data.secondaryColor || '#ffffff',
            customCss: response.data.customCss || ''
          }
        }
      } catch (err) {
        if (err.response?.status !== 404) {
          console.error('Error loading settings:', err)
        }
      }
    },
    
    async saveSettings() {
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
      
      this.saving = true
      this.success = false
      this.error = ''
      
      try {
        await whiteLabelService.createOrUpdateSettings(this.settings, parseInt(userId))
        this.success = true
        setTimeout(() => {
          this.success = false
        }, 3000)
      } catch (err) {
        this.error = err.response?.data?.message || '설정 저장에 실패했습니다.'
        console.error('Error saving settings:', err)
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

