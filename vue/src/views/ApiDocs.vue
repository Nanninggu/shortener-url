<template>
  <div class="container mx-auto px-4 py-12 max-w-7xl">
    <!-- Loading State -->
    <div v-if="loading" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      <p class="mt-4 text-muted-foreground">API 문서를 불러오는 중...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-destructive/10 border border-destructive/20 rounded-lg p-6">
      <div class="flex items-center gap-2">
        <Warning class="h-5 w-5 text-destructive" />
        <p class="text-destructive">{{ error }}</p>
      </div>
      <button
        @click="loadApiSpec"
        class="mt-4 inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2"
      >
        다시 시도
      </button>
    </div>

    <!-- API Documentation -->
    <div v-else-if="apiSpec" class="space-y-8">
      <!-- Header -->
      <div class="bg-card border rounded-lg shadow-sm p-6 sm:p-8">
        <div class="text-center mb-6">
          <h1 class="text-3xl sm:text-4xl font-bold text-foreground mb-4">
            {{ apiSpec.info?.title || 'API 문서' }}
          </h1>
          <p class="text-lg text-muted-foreground mb-4">
            {{ apiSpec.info?.description || 'H-Link URL Shortener API를 사용하여 URL 단축 서비스를 통합하세요' }}
          </p>
          <div class="inline-flex items-center gap-2 rounded-md bg-muted px-4 py-2">
            <span class="text-sm font-medium text-muted-foreground">Base URL:</span>
            <code class="text-sm font-mono text-foreground">{{ baseUrl }}</code>
          </div>
          <div v-if="apiSpec.info?.version" class="mt-2">
            <span class="text-sm text-muted-foreground">Version: {{ apiSpec.info.version }}</span>
          </div>
        </div>
      </div>

      <!-- Authentication Info -->
      <div v-if="hasAuth" class="bg-primary/10 border border-primary/20 rounded-lg p-6">
        <div class="flex items-start gap-3">
          <Key class="h-5 w-5 text-primary mt-0.5" />
          <div>
            <h3 class="text-sm font-semibold text-foreground mb-2">인증 필요</h3>
            <p class="text-sm text-muted-foreground mb-2">
              일부 API는 인증이 필요합니다. JWT 토큰을 <code class="bg-muted px-1 rounded text-xs">Authorization: Bearer {token}</code> 헤더에 포함하세요.
            </p>
            <button
              v-if="!isAuthenticated"
              @click="$router.push('/login')"
              class="text-sm text-primary hover:underline"
            >
              로그인하여 토큰 받기 →
            </button>
          </div>
        </div>
      </div>

      <!-- API Endpoints by Tag -->
      <div v-for="tag in sortedTags" :key="tag" class="space-y-4">
        <div class="bg-card border rounded-lg shadow-sm p-6">
          <h2 class="text-2xl font-bold text-foreground mb-6">{{ tag }}</h2>
          
          <div class="space-y-6">
            <div
              v-for="endpoint in getEndpointsByTag(tag)"
              :key="`${endpoint.method}-${endpoint.path}`"
              class="border-b last:border-b-0 pb-6 last:pb-0"
            >
              <!-- Endpoint Header -->
              <div class="flex flex-wrap items-center gap-3 mb-4">
                <span
                  :class="getMethodBadgeClass(endpoint.method)"
                  class="inline-flex items-center rounded-md px-2.5 py-0.5 text-xs font-medium"
                >
                  {{ endpoint.method }}
                </span>
                <code class="text-lg font-mono text-foreground break-all">{{ endpoint.path }}</code>
                <span v-if="endpoint.summary" class="text-sm text-muted-foreground">
                  {{ endpoint.summary }}
                </span>
              </div>

              <!-- Description -->
              <p v-if="endpoint.description" class="text-muted-foreground mb-4">
                {{ endpoint.description }}
              </p>

              <!-- Parameters -->
              <div v-if="endpoint.parameters && endpoint.parameters.length > 0" class="mb-4">
                <h3 class="text-sm font-semibold text-foreground mb-2">Parameters</h3>
                <div class="bg-muted rounded-md p-4 space-y-2">
                  <div
                    v-for="param in endpoint.parameters"
                    :key="param.name"
                    class="flex flex-col sm:flex-row sm:items-start gap-2"
                  >
                    <div class="flex items-center gap-2 min-w-[200px]">
                      <code class="text-sm font-mono text-primary">{{ param.name }}</code>
                      <span
                        v-if="param.required"
                        class="text-xs text-destructive font-medium"
                      >
                        (required)
                      </span>
                      <span v-else class="text-xs text-muted-foreground">(optional)</span>
                    </div>
                    <div class="flex-1">
                      <span class="text-sm text-muted-foreground">{{ param.description || param.schema?.type || '' }}</span>
                      <span v-if="param.schema?.type" class="text-xs text-muted-foreground ml-2">
                        ({{ param.schema.type }})
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Request Body -->
              <div v-if="endpoint.requestBody" class="mb-4">
                <h3 class="text-sm font-semibold text-foreground mb-2">Request Body</h3>
                <div class="bg-muted rounded-md p-4 overflow-x-auto">
                  <pre class="text-sm"><code>{{ formatRequestBody(endpoint.requestBody) }}</code></pre>
                </div>
              </div>

              <!-- Responses -->
              <div v-if="endpoint.responses" class="mb-4">
                <h3 class="text-sm font-semibold text-foreground mb-2">Responses</h3>
                <div class="space-y-2">
                  <div
                    v-for="(response, statusCode) in endpoint.responses"
                    :key="statusCode"
                    class="bg-muted rounded-md p-4"
                  >
                    <div class="flex items-center gap-2 mb-2">
                      <span
                        :class="getStatusCodeClass(statusCode)"
                        class="inline-flex items-center rounded-md px-2 py-0.5 text-xs font-medium"
                      >
                        {{ statusCode }}
                      </span>
                      <span class="text-sm text-muted-foreground">{{ response.description }}</span>
                    </div>
                    <div v-if="response.content" class="mt-2">
                      <div
                        v-for="(content, contentType) in response.content"
                        :key="contentType"
                        class="space-y-2"
                      >
                        <div v-if="content.schema" class="overflow-x-auto">
                          <pre class="text-sm"><code>{{ formatSchema(content.schema) }}</code></pre>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Try it out -->
              <div class="mt-4 pt-4 border-t">
                <button
                  @click="toggleTryItOut(endpoint)"
                  class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-secondary text-secondary-foreground hover:bg-secondary/80 h-9 px-3"
                >
                  <CaretRight class="h-4 w-4" />
                  {{ tryItOutEndpoints.has(`${endpoint.method}-${endpoint.path}`) ? '닫기' : 'Try it out' }}
                </button>

                <!-- Try it out form -->
                <div
                  v-if="tryItOutEndpoints.has(`${endpoint.method}-${endpoint.path}`)"
                  class="mt-4 bg-muted rounded-md p-4 space-y-4"
                >
                  <div v-if="endpoint.parameters && endpoint.parameters.length > 0">
                    <h4 class="text-sm font-semibold text-foreground mb-2">Parameters</h4>
                    <div class="space-y-2">
                      <div
                        v-for="param in endpoint.parameters"
                        :key="param.name"
                        class="space-y-1"
                      >
                        <label class="text-sm font-medium text-foreground">
                          {{ param.name }}
                          <span v-if="param.required" class="text-destructive">*</span>
                        </label>
                        <input
                          v-model="tryItOutParams[`${endpoint.method}-${endpoint.path}-${param.name}`]"
                          type="text"
                          :placeholder="param.schema?.type || 'value'"
                          class="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                        />
                      </div>
                    </div>
                  </div>

                  <div v-if="endpoint.requestBody">
                    <h4 class="text-sm font-semibold text-foreground mb-2">Request Body</h4>
                    <textarea
                      v-model="tryItOutBody[`${endpoint.method}-${endpoint.path}`]"
                      rows="6"
                      placeholder="JSON 형식으로 입력하세요"
                      class="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 font-mono"
                    ></textarea>
                  </div>

                  <div class="flex gap-2">
                    <button
                      @click="executeRequest(endpoint)"
                      :disabled="executing"
                      class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-9 px-4"
                    >
                      <ArrowRight class="h-4 w-4" />
                      {{ executing ? '실행 중...' : '실행' }}
                    </button>
                    <button
                      @click="clearTryItOut(endpoint)"
                      class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-secondary text-secondary-foreground hover:bg-secondary/80 h-9 px-4"
                    >
                      초기화
                    </button>
                  </div>

                  <!-- Response -->
                  <div v-if="tryItOutResponse[`${endpoint.method}-${endpoint.path}`]">
                    <h4 class="text-sm font-semibold text-foreground mb-2">Response</h4>
                    <div class="bg-background rounded-md p-4 overflow-x-auto border">
                      <pre class="text-sm"><code>{{ tryItOutResponse[`${endpoint.method}-${endpoint.path}`] }}</code></pre>
                    </div>
                  </div>
                </div>
              </div>
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
      </div>
    </div>
  </div>
</template>

<script>
import { openApiService } from '../services/api'
import { ArrowLeft, Warning, Key, CaretRight, ArrowRight } from '@element-plus/icons-vue'

export default {
  name: 'ApiDocs',
  components: {
    ArrowLeft,
    Warning,
    Key,
    CaretRight,
    ArrowRight
  },
  data() {
    return {
      loading: true,
      error: null,
      apiSpec: null,
      tryItOutEndpoints: new Set(),
      tryItOutParams: {},
      tryItOutBody: {},
      tryItOutResponse: {},
      executing: false,
      isAuthenticated: false
    }
  },
  computed: {
    baseUrl() {
      if (import.meta.env.PROD && window.location.hostname !== 'localhost') {
        return `${window.location.protocol}//${window.location.hostname}/api`
      }
      return 'http://localhost:8080/api'
    },
    hasAuth() {
      return this.apiSpec?.components?.securitySchemes && Object.keys(this.apiSpec.components.securitySchemes).length > 0
    },
    sortedTags() {
      if (!this.apiSpec?.tags) return []
      return this.apiSpec.tags.map(t => t.name).sort()
    }
  },
  mounted() {
    this.checkAuth()
    this.loadApiSpec()
  },
  methods: {
    checkAuth() {
      this.isAuthenticated = !!localStorage.getItem('authToken')
    },
    async loadApiSpec() {
      this.loading = true
      this.error = null
      try {
        this.apiSpec = await openApiService.getOpenApiSpec()
      } catch (err) {
        console.error('Failed to load API spec:', err)
        this.error = 'API 문서를 불러오는데 실패했습니다. 서버가 실행 중인지 확인해주세요.'
      } finally {
        this.loading = false
      }
    },
    getEndpointsByTag(tag) {
      if (!this.apiSpec?.paths) return []
      
      const endpoints = []
      for (const [path, methods] of Object.entries(this.apiSpec.paths)) {
        for (const [method, details] of Object.entries(methods)) {
          if (['get', 'post', 'put', 'delete', 'patch'].includes(method.toLowerCase())) {
            const endpointTags = details.tags || []
            if (endpointTags.includes(tag)) {
              endpoints.push({
                method: method.toUpperCase(),
                path: path,
                ...details
              })
            }
          }
        }
      }
      return endpoints.sort((a, b) => {
        const methodOrder = { GET: 1, POST: 2, PUT: 3, PATCH: 4, DELETE: 5 }
        return (methodOrder[a.method] || 99) - (methodOrder[b.method] || 99)
      })
    },
    getMethodBadgeClass(method) {
      const classes = {
        GET: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
        POST: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
        PUT: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
        PATCH: 'bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200',
        DELETE: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
      }
      return classes[method] || 'bg-muted text-muted-foreground'
    },
    getStatusCodeClass(statusCode) {
      const code = parseInt(statusCode)
      if (code >= 200 && code < 300) {
        return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
      } else if (code >= 400 && code < 500) {
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
      } else if (code >= 500) {
        return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
      }
      return 'bg-muted text-muted-foreground'
    },
    formatRequestBody(requestBody) {
      if (!requestBody?.content) return '{}'
      const content = Object.values(requestBody.content)[0]
      if (content?.schema) {
        return this.formatSchema(content.schema)
      }
      return '{}'
    },
    formatSchema(schema) {
      if (schema.example) {
        return JSON.stringify(schema.example, null, 2)
      }
      if (schema.properties) {
        const example = {}
        for (const [key, prop] of Object.entries(schema.properties)) {
          if (prop.example !== undefined) {
            example[key] = prop.example
          } else if (prop.type === 'string') {
            example[key] = 'string'
          } else if (prop.type === 'number' || prop.type === 'integer') {
            example[key] = 0
          } else if (prop.type === 'boolean') {
            example[key] = false
          } else if (prop.type === 'array') {
            example[key] = []
          } else if (prop.type === 'object') {
            example[key] = {}
          }
        }
        return JSON.stringify(example, null, 2)
      }
      return JSON.stringify({}, null, 2)
    },
    toggleTryItOut(endpoint) {
      const key = `${endpoint.method}-${endpoint.path}`
      if (this.tryItOutEndpoints.has(key)) {
        this.tryItOutEndpoints.delete(key)
        delete this.tryItOutResponse[key]
      } else {
        this.tryItOutEndpoints.add(key)
        // Initialize default values
        if (endpoint.parameters) {
          endpoint.parameters.forEach(param => {
            const paramKey = `${key}-${param.name}`
            if (!this.tryItOutParams[paramKey]) {
              this.tryItOutParams[paramKey] = ''
            }
          })
        }
        if (endpoint.requestBody) {
          if (!this.tryItOutBody[key]) {
            this.tryItOutBody[key] = this.formatRequestBody(endpoint.requestBody)
          }
        }
      }
    },
    clearTryItOut(endpoint) {
      const key = `${endpoint.method}-${endpoint.path}`
      delete this.tryItOutResponse[key]
      if (endpoint.parameters) {
        endpoint.parameters.forEach(param => {
          const paramKey = `${key}-${param.name}`
          delete this.tryItOutParams[paramKey]
        })
      }
      delete this.tryItOutBody[key]
    },
    async executeRequest(endpoint) {
      this.executing = true
      const key = `${endpoint.method}-${endpoint.path}`
      
      try {
        // Build URL with path parameters
        let url = endpoint.path
        if (endpoint.parameters) {
          endpoint.parameters.forEach(param => {
            if (param.in === 'path') {
              const paramKey = `${key}-${param.name}`
              const value = this.tryItOutParams[paramKey] || ''
              url = url.replace(`{${param.name}}`, value)
            }
          })
        }

        // Build query parameters
        const queryParams = new URLSearchParams()
        if (endpoint.parameters) {
          endpoint.parameters.forEach(param => {
            if (param.in === 'query') {
              const paramKey = `${key}-${param.name}`
              const value = this.tryItOutParams[paramKey]
              if (value) {
                queryParams.append(param.name, value)
              }
            }
          })
        }
        if (queryParams.toString()) {
          url += `?${queryParams.toString()}`
        }

        // Build headers
        const headers = {
          'Content-Type': 'application/json'
        }
        
        // Add auth token if available
        const token = localStorage.getItem('authToken')
        if (token) {
          headers['Authorization'] = `Bearer ${token}`
        }

        // Build request options
        const options = {
          method: endpoint.method,
          headers
        }

        // Add body for POST, PUT, PATCH
        if (['POST', 'PUT', 'PATCH'].includes(endpoint.method)) {
          const body = this.tryItOutBody[key] || '{}'
          try {
            options.body = JSON.stringify(JSON.parse(body))
          } catch (e) {
            this.tryItOutResponse[key] = `Error: Invalid JSON in request body\n${e.message}`
            return
          }
        }

        // Execute request
        const fullUrl = `${this.baseUrl}${url}`
        const response = await fetch(fullUrl, options)
        const responseText = await response.text()
        
        let formattedResponse = `Status: ${response.status} ${response.statusText}\n\n`
        try {
          const jsonResponse = JSON.parse(responseText)
          formattedResponse += JSON.stringify(jsonResponse, null, 2)
        } catch (e) {
          formattedResponse += responseText
        }
        
        this.tryItOutResponse[key] = formattedResponse
      } catch (error) {
        this.tryItOutResponse[key] = `Error: ${error.message}`
      } finally {
        this.executing = false
      }
    }
  }
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
