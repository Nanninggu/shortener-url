<template>
  <div class="container mx-auto px-4 py-12 max-w-4xl">
    <div class="space-y-8">
      <!-- Header -->
      <div class="text-center mb-12">
        <h1 class="text-4xl font-bold text-foreground mb-4">사용법 안내</h1>
        <p class="text-lg text-muted-foreground">
          H-Link URL Shortener를 쉽고 빠르게 사용하는 방법을 알아보세요
        </p>
      </div>

      <!-- Quick Start -->
      <div class="bg-primary/10 border border-primary/20 rounded-lg shadow-sm p-6">
        <h2 class="text-2xl font-bold text-foreground mb-4">🚀 빠른 시작</h2>
        <div class="space-y-4">
          <div class="flex items-start gap-4">
            <div class="flex-shrink-0 w-8 h-8 rounded-full bg-primary text-primary-foreground flex items-center justify-center font-bold">
              1
            </div>
            <div>
              <h3 class="font-semibold text-foreground mb-2">홈 페이지에서 URL 입력</h3>
              <p class="text-sm text-muted-foreground">
                원본 URL을 입력 필드에 붙여넣기하세요. 예: <code class="bg-muted px-1 rounded">https://example.com/very/long/url</code>
              </p>
            </div>
          </div>
          <div class="flex items-start gap-4">
            <div class="flex-shrink-0 w-8 h-8 rounded-full bg-primary text-primary-foreground flex items-center justify-center font-bold">
              2
            </div>
            <div>
              <h3 class="font-semibold text-foreground mb-2">단축 URL 생성</h3>
              <p class="text-sm text-muted-foreground">
                "단축 URL 생성" 버튼을 클릭하면 짧은 URL이 생성됩니다.
              </p>
            </div>
          </div>
          <div class="flex items-start gap-4">
            <div class="flex-shrink-0 w-8 h-8 rounded-full bg-primary text-primary-foreground flex items-center justify-center font-bold">
              3
            </div>
            <div>
              <h3 class="font-semibold text-foreground mb-2">단축 URL 사용</h3>
              <p class="text-sm text-muted-foreground">
                생성된 단축 URL을 복사하여 사용하세요. 클릭하면 원본 URL로 자동 리다이렉트됩니다.
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Basic Usage -->
      <div class="bg-card border rounded-lg shadow-sm p-6">
        <h2 class="text-2xl font-bold text-foreground mb-4">📝 기본 사용법</h2>
        
        <div class="space-y-6">
          <div>
            <h3 class="text-lg font-semibold text-foreground mb-3">1. URL 단축하기</h3>
            <div class="bg-muted rounded-md p-4 space-y-3">
              <div class="flex items-center gap-2">
                <span class="text-sm font-medium text-foreground">원본 URL:</span>
                <code class="text-sm bg-background px-2 py-1 rounded">https://example.com/very/long/url/path</code>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-sm font-medium text-foreground">단축 URL:</span>
                <code class="text-sm bg-background px-2 py-1 rounded">http://localhost:8080/MTMzMDQz</code>
              </div>
            </div>
            <p class="text-sm text-muted-foreground mt-3">
              긴 URL을 짧고 기억하기 쉬운 URL로 변환할 수 있습니다. 단축 URL을 클릭하면 원본 URL로 자동 이동합니다.
            </p>
          </div>

          <div>
            <h3 class="text-lg font-semibold text-foreground mb-3">2. 커스텀 코드 사용</h3>
            <div class="bg-muted rounded-md p-4 space-y-3">
              <p class="text-sm text-muted-foreground">
                원하는 단축 코드를 직접 지정할 수 있습니다. 예를 들어, <code class="bg-background px-1 rounded">my-link</code> 같은 기억하기 쉬운 코드를 사용할 수 있습니다.
              </p>
              <div class="flex items-center gap-2">
                <span class="text-sm font-medium text-foreground">커스텀 코드:</span>
                <code class="text-sm bg-background px-2 py-1 rounded">my-link</code>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-sm font-medium text-foreground">단축 URL:</span>
                <code class="text-sm bg-background px-2 py-1 rounded">http://localhost:8080/my-link</code>
              </div>
            </div>
            <div class="bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-md p-3 mt-3">
              <p class="text-sm text-yellow-800 dark:text-yellow-200">
                ⚠️ <strong>주의:</strong> 커스텀 코드는 중복될 수 없습니다. 이미 사용 중인 코드는 다른 코드를 선택해야 합니다.
              </p>
            </div>
          </div>

          <div>
            <h3 class="text-lg font-semibold text-foreground mb-3">3. 만료일 설정</h3>
            <div class="bg-muted rounded-md p-4 space-y-3">
              <p class="text-sm text-muted-foreground">
                단축 URL에 만료일을 설정할 수 있습니다. 만료일이 지나면 해당 URL은 더 이상 사용할 수 없습니다.
              </p>
              <div class="flex items-center gap-2">
                <span class="text-sm font-medium text-foreground">만료일:</span>
                <code class="text-sm bg-background px-2 py-1 rounded">30일 후</code>
              </div>
            </div>
            <p class="text-sm text-muted-foreground mt-3">
              만료일을 설정하지 않으면 URL은 영구적으로 유효합니다.
            </p>
          </div>
        </div>
      </div>

      <!-- Advanced Features -->
      <div class="bg-card border rounded-lg shadow-sm p-6">
        <h2 class="text-2xl font-bold text-foreground mb-4">✨ 고급 기능</h2>
        
        <div class="space-y-6">
          <div>
            <h3 class="text-lg font-semibold text-foreground mb-3">📊 통계 확인</h3>
            <p class="text-sm text-muted-foreground mb-3">
              각 단축 URL의 클릭 통계를 확인할 수 있습니다. URL 목록에서 통계 아이콘(📊)을 클릭하면 상세 통계를 볼 수 있습니다.
            </p>
            <div class="bg-muted rounded-md p-4">
              <ul class="space-y-2 text-sm text-muted-foreground">
                <li>• <strong>시간대별 통계:</strong> 시간대별 클릭 수 확인</li>
                <li>• <strong>국가별 통계:</strong> 접속 국가별 클릭 수 확인</li>
                <li>• <strong>일별 통계:</strong> 날짜별 클릭 수 추이 확인</li>
                <li>• <strong>디바이스별 통계:</strong> 데스크톱, 모바일, 태블릿별 클릭 수 확인</li>
                <li>• <strong>브라우저별 통계:</strong> Chrome, Safari, Firefox 등 브라우저별 클릭 수 확인</li>
                <li>• <strong>OS별 통계:</strong> Windows, macOS, iOS, Android 등 운영체제별 클릭 수 확인</li>
                <li>• <strong>리퍼러별 통계:</strong> 유입 경로(검색 엔진, 소셜 미디어 등)별 클릭 수 확인</li>
                <li>• <strong>도시별 통계:</strong> 접속 도시별 클릭 수 확인</li>
              </ul>
            </div>
          </div>

          <div>
            <h3 class="text-lg font-semibold text-foreground mb-3">📱 QR 코드 생성</h3>
            <p class="text-sm text-muted-foreground mb-3">
              단축 URL 생성 시 자동으로 QR 코드가 생성됩니다. QR 코드를 스캔하면 바로 원본 URL로 이동할 수 있습니다.
            </p>
            <div class="bg-muted rounded-md p-4 flex items-center gap-4">
              <div class="w-24 h-24 bg-background border rounded flex items-center justify-center">
                <span class="text-xs text-muted-foreground">QR Code</span>
              </div>
              <div class="flex-1">
                <p class="text-sm text-muted-foreground">
                  QR 코드를 이미지로 다운로드하거나 인쇄하여 사용할 수 있습니다.
                </p>
                <code class="text-xs bg-background px-2 py-1 rounded mt-2 inline-block">
                  /api/qrcode/{shortCode}
                </code>
              </div>
            </div>
          </div>

          <div>
            <h3 class="text-lg font-semibold text-foreground mb-3">🔗 URL 목록 관리</h3>
            <p class="text-sm text-muted-foreground mb-3">
              생성한 모든 단축 URL을 한 곳에서 관리할 수 있습니다. URL 목록 페이지에서 확인할 수 있습니다.
            </p>
            <div class="bg-muted rounded-md p-4">
              <ul class="space-y-2 text-sm text-muted-foreground">
                <li>• 생성된 모든 단축 URL 확인</li>
                <li>• 각 URL의 클릭 수 확인</li>
                <li>• 만료일 확인</li>
                <li>• 통계 페이지로 이동</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <!-- Admin Features -->
      <div class="bg-card border rounded-lg shadow-sm p-6">
        <h2 class="text-2xl font-bold text-foreground mb-4">👨‍💼 관리자 기능</h2>
        
        <div class="space-y-6">
          <div>
            <h3 class="text-lg font-semibold text-foreground mb-3">관리자 페이지 접근</h3>
            <div class="bg-muted rounded-md p-4 space-y-3">
              <p class="text-sm text-muted-foreground">
                관리자 페이지에서는 시스템 전체 통계와 모든 URL을 관리할 수 있습니다.
              </p>
              <div class="flex items-center gap-2">
                <span class="text-sm font-medium text-foreground">기본 계정:</span>
                <code class="text-sm bg-background px-2 py-1 rounded">admin</code>
                <span class="text-sm text-muted-foreground">/</span>
                <code class="text-sm bg-background px-2 py-1 rounded">admin123</code>
              </div>
            </div>
          </div>

          <div>
            <h3 class="text-lg font-semibold text-foreground mb-3">관리자 기능</h3>
            <div class="bg-muted rounded-md p-4">
              <ul class="space-y-2 text-sm text-muted-foreground">
                <li>• <strong>전체 통계:</strong> 시스템 전체 URL 수, 총 클릭 수, 활성/만료 URL 수 확인</li>
                <li>• <strong>URL 관리:</strong> 모든 URL 목록 확인 및 삭제</li>
                <li>• <strong>만료 URL 정리:</strong> 만료된 URL 일괄 삭제</li>
                <li>• <strong>검색 및 필터:</strong> URL 검색 및 상태별 필터링</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <!-- API Usage -->
      <div class="bg-card border rounded-lg shadow-sm p-6">
        <h2 class="text-2xl font-bold text-foreground mb-4">🔌 API 사용</h2>
        
        <div class="space-y-4">
          <p class="text-muted-foreground">
            프로그래밍 방식으로 URL 단축 서비스를 사용할 수 있습니다. 자세한 API 문서는 <router-link to="/api-docs" class="text-primary hover:underline">API 문서 페이지</router-link>를 참조하세요.
          </p>
          
          <div class="bg-muted rounded-md p-4">
            <h4 class="font-semibold text-foreground mb-2">간단한 예시:</h4>
            <pre class="text-sm text-foreground overflow-x-auto"><code>curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://example.com/very/long/url",
    "expirationDays": 30,
    "customCode": "my-link"
  }'</code></pre>
          </div>
        </div>
      </div>

      <!-- FAQ -->
      <div class="bg-card border rounded-lg shadow-sm p-6">
        <h2 class="text-2xl font-bold text-foreground mb-4">❓ 자주 묻는 질문 (FAQ)</h2>
        
        <div class="space-y-6">
          <div>
            <h3 class="text-lg font-semibold text-foreground mb-2">Q: 단축 URL은 얼마나 오래 유효한가요?</h3>
            <p class="text-sm text-muted-foreground">
              A: 만료일을 설정하지 않으면 영구적으로 유효합니다. 만료일을 설정한 경우, 해당 날짜가 지나면 더 이상 사용할 수 없습니다.
            </p>
          </div>

          <div>
            <h3 class="text-lg font-semibold text-foreground mb-2">Q: 커스텀 코드는 어떤 형식이어야 하나요?</h3>
            <p class="text-sm text-muted-foreground">
              A: 영문자, 숫자, 하이픈(-), 언더스코어(_)를 사용할 수 있습니다. 중복된 코드는 사용할 수 없습니다.
            </p>
          </div>

          <div>
            <h3 class="text-lg font-semibold text-foreground mb-2">Q: 단축 URL을 삭제할 수 있나요?</h3>
            <p class="text-sm text-muted-foreground">
              A: 관리자 페이지에서 URL을 삭제할 수 있습니다. 삭제된 URL은 더 이상 사용할 수 없습니다.
            </p>
          </div>

          <div>
            <h3 class="text-lg font-semibold text-foreground mb-2">Q: 통계는 어떻게 확인하나요?</h3>
            <p class="text-sm text-muted-foreground">
              A: URL 목록 페이지에서 각 URL 옆의 통계 아이콘(📊)을 클릭하면 상세 통계를 확인할 수 있습니다.
            </p>
          </div>

          <div>
            <h3 class="text-lg font-semibold text-foreground mb-2">Q: Rate Limiting이 있나요?</h3>
            <p class="text-sm text-muted-foreground">
              A: 네, IP당 분당 100개 요청으로 제한됩니다. 이 제한을 초과하면 429 (Too Many Requests) 에러가 발생합니다.
            </p>
          </div>

          <div>
            <h3 class="text-lg font-semibold text-foreground mb-2">Q: 어떤 URL을 단축할 수 있나요?</h3>
            <p class="text-sm text-muted-foreground">
              A: HTTP 또는 HTTPS 프로토콜을 사용하는 유효한 URL만 단축할 수 있습니다. 로컬호스트나 사설 IP 주소는 보안상 차단됩니다.
            </p>
          </div>
        </div>
      </div>

      <!-- Tips -->
      <div class="bg-primary/10 border border-primary/20 rounded-lg shadow-sm p-6">
        <h2 class="text-2xl font-bold text-foreground mb-4">💡 사용 팁</h2>
        
        <div class="space-y-3">
          <div class="flex items-start gap-3">
            <span class="text-primary text-xl">💡</span>
            <p class="text-sm text-muted-foreground">
              <strong>기억하기 쉬운 커스텀 코드 사용:</strong> 브랜드명이나 제품명을 포함한 커스텀 코드를 사용하면 더 효과적입니다.
            </p>
          </div>
          <div class="flex items-start gap-3">
            <span class="text-primary text-xl">💡</span>
            <p class="text-sm text-muted-foreground">
              <strong>QR 코드 활용:</strong> 오프라인 마케팅이나 인쇄물에 QR 코드를 사용하면 접근성이 향상됩니다.
            </p>
          </div>
          <div class="flex items-start gap-3">
            <span class="text-primary text-xl">💡</span>
            <p class="text-sm text-muted-foreground">
              <strong>통계 분석:</strong> 정기적으로 통계를 확인하여 어떤 시간대나 지역에서 가장 많은 클릭이 발생하는지 파악하세요.
            </p>
          </div>
          <div class="flex items-start gap-3">
            <span class="text-primary text-xl">💡</span>
            <p class="text-sm text-muted-foreground">
              <strong>만료일 설정:</strong> 이벤트나 프로모션용 URL은 만료일을 설정하여 자동으로 정리되도록 하세요.
            </p>
          </div>
        </div>
      </div>

      <!-- Navigation -->
      <div class="flex justify-center gap-4">
        <router-link
          to="/"
          class="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-secondary text-secondary-foreground hover:bg-secondary/80 h-10 px-4 py-2"
        >
          ← 홈으로
        </router-link>
        <router-link
          to="/api-docs"
          class="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2"
        >
          API 문서 보기 →
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Guide'
}
</script>

<style scoped>
code {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', 'source-code-pro', monospace;
}
</style>

