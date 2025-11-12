<template>
  <div id="app" class="min-h-screen bg-background flex flex-col">
    <header class="border-b bg-card sticky top-0 z-50 backdrop-blur supports-[backdrop-filter]:bg-card/95">
      <div class="container mx-auto px-4 py-4">
        <div class="flex items-center justify-between">
          <router-link to="/" class="flex items-center gap-3 hover:opacity-80 transition-opacity">
            <div class="flex flex-col">
              <h1 class="text-2xl font-bold text-foreground leading-tight">H-Link</h1>
              <p class="text-xs text-muted-foreground leading-tight">Simple is the Best!</p>
            </div>
          </router-link>
          <nav class="flex items-center gap-2">
            <router-link
              to="/"
              class="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 px-3 py-2 text-muted-foreground hover:text-foreground hover:bg-accent"
            >
              홈
            </router-link>
            <router-link
              to="/urls"
              class="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 px-3 py-2 text-muted-foreground hover:text-foreground hover:bg-accent"
            >
              URL 목록
            </router-link>
            <router-link
              to="/guide"
              class="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 px-3 py-2 text-muted-foreground hover:text-foreground hover:bg-accent"
            >
              사용법
            </router-link>
            <router-link
              to="/api-docs"
              class="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 px-3 py-2 text-muted-foreground hover:text-foreground hover:bg-accent"
            >
              API 문서
            </router-link>
            <router-link
              to="/admin"
              class="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 px-3 py-2 text-muted-foreground hover:text-foreground hover:bg-accent"
            >
              관리자
            </router-link>
          </nav>
        </div>
      </div>
    </header>
    
    <main class="flex-1">
      <router-view />
    </main>
    
    <footer class="border-t bg-card mt-auto">
      <div class="container mx-auto px-4 py-6">
        <p class="text-center text-sm text-muted-foreground">
          &copy; 2024 H-Link. URL Shortener Service
        </p>
      </div>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>
