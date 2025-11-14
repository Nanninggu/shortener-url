<template>
  <div class="container mx-auto px-4 py-12 max-w-6xl">
    <div class="space-y-8">
      <!-- Header -->
      <div class="text-center mb-12">
        <h1 class="text-4xl font-bold text-foreground mb-4">API 문서</h1>
        <p class="text-lg text-muted-foreground">
          H-Link URL Shortener API를 사용하여 URL 단축 서비스를 통합하세요
        </p>
        <div class="mt-4 inline-flex items-center gap-2 rounded-md bg-muted px-4 py-2">
          <span class="text-sm font-medium text-muted-foreground">Base URL:</span>
          <code class="text-sm font-mono text-foreground">http://localhost:8080</code>
        </div>
      </div>

      <!-- Authentication Section -->
      <div class="bg-card border rounded-lg shadow-sm p-6">
        <h2 class="text-2xl font-bold text-foreground mb-6">인증 (Authentication)</h2>
        
        <!-- Login -->
        <div class="mb-8 pb-8 border-b">
          <div class="flex items-center gap-3 mb-4">
            <span class="inline-flex items-center rounded-md bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200 px-2.5 py-0.5 text-xs font-medium">
              POST
            </span>
            <code class="text-lg font-mono text-foreground">/api/auth/login</code>
            <span class="text-sm text-muted-foreground">관리자 로그인</span>
          </div>
          
          <p class="text-muted-foreground mb-4">
            관리자 계정으로 로그인하여 JWT 토큰을 발급받습니다. 관리자 API를 사용하려면 이 토큰이 필요합니다.
          </p>

          <div class="space-y-4">
            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Request Body</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm"><code>{{ JSON.stringify({
  username: "admin",
  password: "admin123"
}, null, 2) }}</code></pre>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Response (200 OK)</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm"><code>{{ JSON.stringify({
  token: "eyJhbGciOiJIUzUxMiJ9...",
  username: "admin",
  role: "ADMIN"
}, null, 2) }}</code></pre>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">cURL 예시</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm text-foreground"><code>curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'</code></pre>
              </div>
            </div>

            <div class="bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-md p-4">
              <p class="text-sm text-yellow-800 dark:text-yellow-200">
                <strong>인증 헤더:</strong> 관리자 API를 사용할 때는 응답으로 받은 토큰을 <code class="bg-yellow-100 dark:bg-yellow-900 px-1 rounded">Authorization: Bearer {token}</code> 헤더에 포함해야 합니다.
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Public API Section -->
      <div class="bg-card border rounded-lg shadow-sm p-6">
        <h2 class="text-2xl font-bold text-foreground mb-6">공개 API (Public APIs)</h2>

        <!-- Create Short URL -->
        <div class="mb-8 pb-8 border-b">
          <div class="flex items-center gap-3 mb-4">
            <span class="inline-flex items-center rounded-md bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200 px-2.5 py-0.5 text-xs font-medium">
              POST
            </span>
            <code class="text-lg font-mono text-foreground">/api/urls</code>
            <span class="text-sm text-muted-foreground">단축 URL 생성</span>
          </div>
          
          <p class="text-muted-foreground mb-4">
            긴 URL을 짧은 단축 URL로 변환합니다. 커스텀 코드를 지정할 수 있습니다.
          </p>

          <div class="space-y-4">
            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Request Body</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm"><code>{{ JSON.stringify({
  originalUrl: "https://example.com/very/long/url",
  expirationDays: 30,
  customCode: "my-custom-code"
}, null, 2) }}</code></pre>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Parameters</h3>
              <div class="space-y-2">
                <div class="flex gap-4 text-sm">
                  <code class="font-mono text-primary min-w-[140px]">originalUrl</code>
                  <span class="text-muted-foreground">(필수) 단축할 원본 URL (HTTP/HTTPS)</span>
                </div>
                <div class="flex gap-4 text-sm">
                  <code class="font-mono text-primary min-w-[140px]">expirationDays</code>
                  <span class="text-muted-foreground">(선택) 만료일 (일 수)</span>
                </div>
                <div class="flex gap-4 text-sm">
                  <code class="font-mono text-primary min-w-[140px]">customCode</code>
                  <span class="text-muted-foreground">(선택) 커스텀 단축 코드 (중복 불가)</span>
                </div>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Response (201 Created)</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm"><code>{{ JSON.stringify({
  id: 1,
  originalUrl: "https://example.com/very/long/url",
  shortUrl: "http://localhost:8080/MTMzMDQz",
  shortCode: "MTMzMDQz",
  clickCount: 0,
  createdAt: "2024-11-10T18:06:42.4441",
  expiresAt: "2024-12-10T18:06:42.4441"
}, null, 2) }}</code></pre>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">cURL 예시</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm text-foreground"><code>curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://example.com/very/long/url",
    "expirationDays": 30,
    "customCode": "my-link"
  }'</code></pre>
              </div>
            </div>
          </div>
        </div>

        <!-- Get URL Info -->
        <div class="mb-8 pb-8 border-b">
          <div class="flex items-center gap-3 mb-4">
            <span class="inline-flex items-center rounded-md bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200 px-2.5 py-0.5 text-xs font-medium">
              GET
            </span>
            <code class="text-lg font-mono text-foreground">/api/urls/{shortCode}</code>
            <span class="text-sm text-muted-foreground">단축 URL 정보 조회</span>
          </div>
          
          <p class="text-muted-foreground mb-4">
            단축 코드로 URL 정보를 조회합니다.
          </p>

          <div class="space-y-4">
            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Path Parameters</h3>
              <div class="space-y-2">
                <div class="flex gap-4 text-sm">
                  <code class="font-mono text-primary min-w-[140px]">shortCode</code>
                  <span class="text-muted-foreground">단축 URL 코드</span>
                </div>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Response (200 OK)</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm"><code>{{ JSON.stringify({
  id: 1,
  originalUrl: "https://example.com/very/long/url",
  shortUrl: "http://localhost:8080/MTMzMDQz",
  shortCode: "MTMzMDQz",
  clickCount: 5,
  createdAt: "2024-11-10T18:06:42.4441",
  expiresAt: null
}, null, 2) }}</code></pre>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">cURL 예시</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm text-foreground"><code>curl http://localhost:8080/api/urls/MTMzMDQz</code></pre>
              </div>
            </div>
          </div>
        </div>

        <!-- Get All URLs -->
        <div class="mb-8 pb-8 border-b">
          <div class="flex items-center gap-3 mb-4">
            <span class="inline-flex items-center rounded-md bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200 px-2.5 py-0.5 text-xs font-medium">
              GET
            </span>
            <code class="text-lg font-mono text-foreground">/api/urls</code>
            <span class="text-sm text-muted-foreground">모든 단축 URL 목록 조회</span>
          </div>
          
          <p class="text-muted-foreground mb-4">
            생성된 모든 단축 URL 목록을 조회합니다.
          </p>

          <div class="space-y-4">
            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Response (200 OK)</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm"><code>{{ JSON.stringify([
  {
    id: 1,
    originalUrl: "https://example.com/very/long/url",
    shortUrl: "http://localhost:8080/MTMzMDQz",
    shortCode: "MTMzMDQz",
    clickCount: 5,
    createdAt: "2024-11-10T18:06:42.4441",
    expiresAt: null
  }
], null, 2) }}</code></pre>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">cURL 예시</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm text-foreground"><code>curl http://localhost:8080/api/urls</code></pre>
              </div>
            </div>
          </div>
        </div>

        <!-- Redirect -->
        <div class="mb-8 pb-8 border-b">
          <div class="flex items-center gap-3 mb-4">
            <span class="inline-flex items-center rounded-md bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200 px-2.5 py-0.5 text-xs font-medium">
              GET
            </span>
            <code class="text-lg font-mono text-foreground">/{shortCode}</code>
            <span class="text-sm text-muted-foreground">단축 URL 리다이렉트</span>
          </div>
          
          <p class="text-muted-foreground mb-4">
            단축 URL로 접근하면 원본 URL로 리다이렉트됩니다. 클릭 수가 자동으로 증가하고 통계가 기록됩니다.
          </p>

          <div class="space-y-4">
            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Path Parameters</h3>
              <div class="space-y-2">
                <div class="flex gap-4 text-sm">
                  <code class="font-mono text-primary min-w-[140px]">shortCode</code>
                  <span class="text-muted-foreground">단축 URL 코드</span>
                </div>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Response (302 Found)</h3>
              <div class="bg-muted rounded-md p-4">
                <p class="text-sm text-muted-foreground">
                  Location 헤더에 원본 URL이 포함되어 리다이렉트됩니다.
                </p>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">예시</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm text-foreground"><code>http://localhost:8080/MTMzMDQz</code></pre>
              </div>
            </div>
          </div>
        </div>

        <!-- QR Code -->
        <div class="mb-8 pb-8 border-b">
          <div class="flex items-center gap-3 mb-4">
            <span class="inline-flex items-center rounded-md bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200 px-2.5 py-0.5 text-xs font-medium">
              GET
            </span>
            <code class="text-lg font-mono text-foreground">/api/qrcode/{shortCode}</code>
            <span class="text-sm text-muted-foreground">QR 코드 생성</span>
          </div>
          
          <p class="text-muted-foreground mb-4">
            단축 URL의 QR 코드 이미지를 생성합니다. PNG 형식으로 반환됩니다.
          </p>

          <div class="space-y-4">
            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Path Parameters</h3>
              <div class="space-y-2">
                <div class="flex gap-4 text-sm">
                  <code class="font-mono text-primary min-w-[140px]">shortCode</code>
                  <span class="text-muted-foreground">단축 URL 코드</span>
                </div>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Response (200 OK)</h3>
              <div class="bg-muted rounded-md p-4">
                <p class="text-sm text-muted-foreground">
                  Content-Type: image/png<br>
                  QR 코드 이미지 (PNG 바이너리)
                </p>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">예시</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm text-foreground"><code>&lt;img src="http://localhost:8080/api/qrcode/MTMzMDQz" alt="QR Code" /&gt;</code></pre>
              </div>
            </div>
          </div>
        </div>

        <!-- URL Statistics -->
        <div class="mb-8">
          <div class="flex items-center gap-3 mb-4">
            <span class="inline-flex items-center rounded-md bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200 px-2.5 py-0.5 text-xs font-medium">
              GET
            </span>
            <code class="text-lg font-mono text-foreground">/api/stats/{shortCode}</code>
            <span class="text-sm text-muted-foreground">URL 통계 조회</span>
          </div>
          
          <p class="text-muted-foreground mb-4">
            단축 URL의 상세 통계를 조회합니다. 시간대별, 국가별, 일별, 디바이스별, 브라우저별, OS별, 리퍼러별, 도시별 클릭 통계를 제공합니다.
          </p>

          <div class="space-y-4">
            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Path Parameters</h3>
              <div class="space-y-2">
                <div class="flex gap-4 text-sm">
                  <code class="font-mono text-primary min-w-[140px]">shortCode</code>
                  <span class="text-muted-foreground">단축 URL 코드</span>
                </div>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Query Parameters</h3>
              <div class="space-y-2">
                <div class="flex gap-4 text-sm">
                  <code class="font-mono text-primary min-w-[140px]">days</code>
                  <span class="text-muted-foreground">(선택, 기본값: 7) 통계 조회 기간 (일 수)</span>
                </div>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Response (200 OK)</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm"><code>{{ JSON.stringify({
  clicksByHour: [
    { hour: 9, count: 5 },
    { hour: 10, count: 12 },
    { hour: 14, count: 8 }
  ],
  clicksByCountry: [
    { country: "KR", count: 15 },
    { country: "US", count: 8 },
    { country: "Unknown", count: 2 }
  ],
  clicksByDate: [
    { date: "2024-11-10", count: 10 },
    { date: "2024-11-09", count: 15 }
  ],
  clicksByDeviceType: [
    { deviceType: "Desktop", count: 20 },
    { deviceType: "Mobile", count: 10 },
    { deviceType: "Tablet", count: 5 }
  ],
  clicksByBrowser: [
    { browser: "Chrome", count: 18 },
    { browser: "Safari", count: 12 },
    { browser: "Firefox", count: 5 }
  ],
  clicksByOS: [
    { os: "Windows", count: 15 },
    { os: "macOS", count: 10 },
    { os: "iOS", count: 8 },
    { os: "Android", count: 2 }
  ],
  clicksByReferer: [
    { referer: "https://google.com", count: 12 },
    { referer: "https://facebook.com", count: 8 },
    { referer: "Direct", count: 15 }
  ],
  clicksByCity: [
    { city: "Seoul", count: 10 },
    { city: "New York", count: 8 },
    { city: "Tokyo", count: 5 }
  ]
}, null, 2) }}</code></pre>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">cURL 예시</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm text-foreground"><code>curl "http://localhost:8080/api/stats/MTMzMDQz?days=30"</code></pre>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Admin API Section -->
      <div class="bg-card border rounded-lg shadow-sm p-6">
        <h2 class="text-2xl font-bold text-foreground mb-6">관리자 API (Admin APIs)</h2>
        <p class="text-muted-foreground mb-6">
          관리자 API는 인증이 필요합니다. <code class="bg-muted px-1 rounded">Authorization: Bearer {token}</code> 헤더를 포함해야 합니다.
        </p>

        <!-- Admin Stats -->
        <div class="mb-8 pb-8 border-b">
          <div class="flex items-center gap-3 mb-4">
            <span class="inline-flex items-center rounded-md bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200 px-2.5 py-0.5 text-xs font-medium">
              GET
            </span>
            <code class="text-lg font-mono text-foreground">/api/admin/stats</code>
            <span class="text-sm text-muted-foreground">전체 통계 조회</span>
          </div>
          
          <p class="text-muted-foreground mb-4">
            시스템 전체 통계를 조회합니다.
          </p>

          <div class="space-y-4">
            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Headers</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm text-foreground"><code>Authorization: Bearer {token}</code></pre>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Response (200 OK)</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm"><code>{{ JSON.stringify({
  totalUrls: 150,
  totalClicks: 3250,
  activeUrls: 120,
  expiredUrls: 30
}, null, 2) }}</code></pre>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">cURL 예시</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm text-foreground"><code>curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/admin/stats</code></pre>
              </div>
            </div>
          </div>
        </div>

        <!-- Admin Get All URLs -->
        <div class="mb-8 pb-8 border-b">
          <div class="flex items-center gap-3 mb-4">
            <span class="inline-flex items-center rounded-md bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200 px-2.5 py-0.5 text-xs font-medium">
              GET
            </span>
            <code class="text-lg font-mono text-foreground">/api/admin/urls</code>
            <span class="text-sm text-muted-foreground">관리자용 URL 목록</span>
          </div>
          
          <p class="text-muted-foreground mb-4">
            모든 단축 URL 목록을 조회합니다 (관리자 전용).
          </p>

          <div class="space-y-4">
            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Headers</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm text-foreground"><code>Authorization: Bearer {token}</code></pre>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Response (200 OK)</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm"><code>{{ JSON.stringify([
  {
    id: 1,
    originalUrl: "https://example.com/very/long/url",
    shortUrl: "http://localhost:8080/MTMzMDQz",
    shortCode: "MTMzMDQz",
    clickCount: 5,
    createdAt: "2024-11-10T18:06:42.4441",
    expiresAt: null
  }
], null, 2) }}</code></pre>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">cURL 예시</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm text-foreground"><code>curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/admin/urls</code></pre>
              </div>
            </div>
          </div>
        </div>

        <!-- Delete URL -->
        <div class="mb-8 pb-8 border-b">
          <div class="flex items-center gap-3 mb-4">
            <span class="inline-flex items-center rounded-md bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200 px-2.5 py-0.5 text-xs font-medium">
              DELETE
            </span>
            <code class="text-lg font-mono text-foreground">/api/admin/urls/{id}</code>
            <span class="text-sm text-muted-foreground">URL 삭제</span>
          </div>
          
          <p class="text-muted-foreground mb-4">
            특정 URL을 삭제합니다.
          </p>

          <div class="space-y-4">
            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Path Parameters</h3>
              <div class="space-y-2">
                <div class="flex gap-4 text-sm">
                  <code class="font-mono text-primary min-w-[140px]">id</code>
                  <span class="text-muted-foreground">URL ID</span>
                </div>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Headers</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm text-foreground"><code>Authorization: Bearer {token}</code></pre>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Response (204 No Content)</h3>
              <div class="bg-muted rounded-md p-4">
                <p class="text-sm text-muted-foreground">
                  성공 시 응답 본문 없음
                </p>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">cURL 예시</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm text-foreground"><code>curl -X DELETE \
  -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/admin/urls/1</code></pre>
              </div>
            </div>
          </div>
        </div>

        <!-- Delete Expired URLs -->
        <div class="mb-8">
          <div class="flex items-center gap-3 mb-4">
            <span class="inline-flex items-center rounded-md bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200 px-2.5 py-0.5 text-xs font-medium">
              DELETE
            </span>
            <code class="text-lg font-mono text-foreground">/api/admin/urls/expired</code>
            <span class="text-sm text-muted-foreground">만료된 URL 일괄 삭제</span>
          </div>
          
          <p class="text-muted-foreground mb-4">
            만료된 모든 URL을 일괄 삭제합니다.
          </p>

          <div class="space-y-4">
            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Headers</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm text-foreground"><code>Authorization: Bearer {token}</code></pre>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">Response (204 No Content)</h3>
              <div class="bg-muted rounded-md p-4">
                <p class="text-sm text-muted-foreground">
                  성공 시 응답 본문 없음
                </p>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-foreground mb-2">cURL 예시</h3>
              <div class="bg-muted rounded-md p-4 overflow-x-auto">
                <pre class="text-sm text-foreground"><code>curl -X DELETE \
  -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/admin/urls/expired</code></pre>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Error Responses -->
      <div class="bg-card border rounded-lg shadow-sm p-6">
        <h2 class="text-2xl font-bold text-foreground mb-4">에러 응답</h2>
        
        <div class="space-y-4">
          <div>
            <h3 class="text-sm font-semibold text-foreground mb-2">400 Bad Request</h3>
            <div class="bg-muted rounded-md p-4 overflow-x-auto">
              <pre class="text-sm"><code>{{ JSON.stringify({
  message: "URL은 필수입니다"
}, null, 2) }}</code></pre>
            </div>
          </div>

          <div>
            <h3 class="text-sm font-semibold text-foreground mb-2">401 Unauthorized</h3>
            <div class="bg-muted rounded-md p-4 overflow-x-auto">
              <pre class="text-sm"><code>{{ JSON.stringify({
  message: "Invalid credentials"
}, null, 2) }}</code></pre>
            </div>
          </div>

          <div>
            <h3 class="text-sm font-semibold text-foreground mb-2">404 Not Found</h3>
            <div class="bg-muted rounded-md p-4 overflow-x-auto">
              <pre class="text-sm"><code>{{ JSON.stringify({
  message: "단축 URL을 찾을 수 없습니다"
}, null, 2) }}</code></pre>
            </div>
          </div>

          <div>
            <h3 class="text-sm font-semibold text-foreground mb-2">429 Too Many Requests</h3>
            <div class="bg-muted rounded-md p-4 overflow-x-auto">
              <pre class="text-sm"><code>{{ JSON.stringify({
  error: "Too many requests. Please try again later."
}, null, 2) }}</code></pre>
            </div>
            <p class="text-sm text-muted-foreground mt-2">
              Rate Limiting: 분당 100개 요청 제한
            </p>
          </div>

          <div>
            <h3 class="text-sm font-semibold text-foreground mb-2">500 Internal Server Error</h3>
            <div class="bg-muted rounded-md p-4 overflow-x-auto">
              <pre class="text-sm"><code>{{ JSON.stringify({
  message: "An error occurred during login"
}, null, 2) }}</code></pre>
            </div>
          </div>
        </div>
      </div>

      <!-- Quick Start -->
      <div class="bg-primary/10 border border-primary/20 rounded-lg shadow-sm p-6">
        <h2 class="text-2xl font-bold text-foreground mb-4">빠른 시작</h2>
        
        <div class="space-y-4">
          <div>
            <h3 class="text-sm font-semibold text-foreground mb-2">JavaScript (Fetch API)</h3>
            <div class="bg-background rounded-md p-4 overflow-x-auto">
              <pre class="text-sm text-foreground"><code>// 로그인
const loginResponse = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'admin',
    password: 'admin123'
  })
});
const { token } = await loginResponse.json();

// 단축 URL 생성
const response = await fetch('http://localhost:8080/api/urls', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    originalUrl: 'https://example.com/very/long/url',
    expirationDays: 30,
    customCode: 'my-link'
  })
});

const data = await response.json();
console.log(data.shortUrl);</code></pre>
            </div>
          </div>

          <div>
            <h3 class="text-sm font-semibold text-foreground mb-2">Python (requests)</h3>
            <div class="bg-background rounded-md p-4 overflow-x-auto">
              <pre class="text-sm text-foreground"><code>import requests

# 로그인
login_response = requests.post(
    'http://localhost:8080/api/auth/login',
    json={'username': 'admin', 'password': 'admin123'}
)
token = login_response.json()['token']

# 단축 URL 생성
response = requests.post(
    'http://localhost:8080/api/urls',
    json={
        'originalUrl': 'https://example.com/very/long/url',
        'expirationDays': 30,
        'customCode': 'my-link'
    }
)

data = response.json()
print(data['shortUrl'])

# 관리자 API 사용
admin_response = requests.get(
    'http://localhost:8080/api/admin/stats',
    headers={'Authorization': f'Bearer {token}'}
)
print(admin_response.json())</code></pre>
            </div>
          </div>

          <div>
            <h3 class="text-sm font-semibold text-foreground mb-2">Node.js (axios)</h3>
            <div class="bg-background rounded-md p-4 overflow-x-auto">
              <pre class="text-sm text-foreground"><code>const axios = require('axios');

// 로그인
const loginResponse = await axios.post('http://localhost:8080/api/auth/login', {
  username: 'admin',
  password: 'admin123'
});
const token = loginResponse.data.token;

// 단축 URL 생성
const response = await axios.post('http://localhost:8080/api/urls', {
  originalUrl: 'https://example.com/very/long/url',
  expirationDays: 30,
  customCode: 'my-link'
});

console.log(response.data.shortUrl);

// 관리자 API 사용
const adminResponse = await axios.get('http://localhost:8080/api/admin/stats', {
  headers: { Authorization: `Bearer ${token}` }
});
console.log(adminResponse.data);</code></pre>
            </div>
          </div>
        </div>
      </div>

      <!-- Navigation -->
      <div class="flex justify-center gap-4">
        <router-link
          to="/"
          class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-secondary text-secondary-foreground hover:bg-secondary/80 h-10 px-4 py-2"
        >
          <ArrowLeft class="h-4 w-4" />
          홈으로
        </router-link>
        <router-link
          to="/urls"
          class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2"
        >
          URL 목록 보기
          <ArrowRight class="h-4 w-4" />
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ApiDocs'
}
</script>

<style scoped>
code {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', 'source-code-pro', monospace;
}

pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
