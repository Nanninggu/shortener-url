<template>
  <div class="container mx-auto px-4 py-12 max-w-7xl">
    <div class="bg-card border rounded-lg shadow-sm p-6 mb-6">
      <div>
        <h1 class="text-3xl font-bold text-foreground mb-2">Swagger UI</h1>
        <p class="text-muted-foreground">
          API 문서 및 테스트 도구입니다
        </p>
      </div>
    </div>

    <!-- Swagger UI iframe -->
    <div class="bg-card border rounded-lg shadow-sm overflow-hidden">
      <iframe
        ref="swaggerIframe"
        :src="swaggerUrl"
        class="w-full h-[calc(100vh-250px)] min-h-[800px] border-0"
        frameborder="0"
        title="Swagger UI API Documentation"
        @load="handleIframeLoad"
        @error="handleIframeError"
      ></iframe>
    </div>

    <!-- 로딩 상태 -->
    <div v-if="loading" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      <p class="mt-4 text-muted-foreground">Swagger UI를 불러오는 중...</p>
    </div>

    <!-- 에러 상태 -->
    <div v-if="error" class="bg-destructive/10 border border-destructive/20 rounded-lg p-6 mt-6">
      <div class="flex items-center gap-2">
        <Warning class="h-5 w-5 text-destructive" />
        <p class="text-destructive">{{ error }}</p>
      </div>
      <button
        @click="reloadIframe"
        class="mt-4 inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2"
      >
        다시 시도
      </button>
    </div>
  </div>
</template>

<script>
import { Warning } from '@element-plus/icons-vue'

export default {
  name: 'Swagger',
  components: {
    Warning
  },
  data() {
    return {
      loading: true,
      error: null
    }
  },
  computed: {
    swaggerUrl() {
      // 환경에 따라 다른 Swagger UI URL 사용
      if (import.meta.env.PROD && window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1') {
        // 프로덕션: Nginx를 통해 접근 (같은 origin)
        return '/swagger-ui/index.html'
      } else {
        // 개발 환경: Vite 프록시를 통해 접근 (같은 origin으로 처리)
        // Vite 프록시 설정이 필요하므로 /swagger-ui 경로도 프록시에 추가해야 함
        // 일단 직접 접근하되, X-Frame-Options: SAMEORIGIN이므로 다른 origin에서는 작동하지 않을 수 있음
        // 대안: Vite 프록시에 /swagger-ui 경로 추가
        return '/swagger-ui/index.html'
      }
    }
  },
  mounted() {
    // 모든 사용자가 접근 가능하므로 권한 체크 제거
  },
  methods: {
    handleIframeLoad() {
      this.loading = false
      this.error = null
    },
    handleIframeError() {
      this.loading = false
      this.error = 'Swagger UI를 불러오는데 실패했습니다. 서버가 실행 중인지 확인해주세요.'
    },
    reloadIframe() {
      this.loading = true
      this.error = null
      // iframe을 다시 로드하기 위해 src를 재설정
      const iframe = this.$refs.swaggerIframe
      if (iframe) {
        iframe.src = this.swaggerUrl
      }
    }
  }
}
</script>

<style scoped>
iframe {
  display: block;
}
</style>

