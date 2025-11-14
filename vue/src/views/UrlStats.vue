<template>
  <div class="container mx-auto px-4 py-12 max-w-6xl">
    <div class="space-y-8">
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-4xl font-bold text-foreground mb-2">URL 통계</h1>
          <p class="text-muted-foreground">단축 URL의 상세 통계를 확인하세요</p>
        </div>
        <router-link
          to="/urls"
          class="inline-flex items-center gap-2 justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-secondary text-secondary-foreground hover:bg-secondary/80 h-10 px-4 py-2"
        >
          <ArrowLeft class="h-4 w-4" />
          목록으로
        </router-link>
      </div>

      <!-- URL Info -->
      <div class="bg-card border rounded-lg shadow-sm p-6" v-if="urlInfo">
        <h2 class="text-xl font-bold text-foreground mb-4">URL 정보</h2>
        <div class="space-y-2">
          <p class="text-sm">
            <span class="font-medium text-muted-foreground">원본 URL:</span>
            <span class="text-foreground ml-2">{{ urlInfo.originalUrl }}</span>
          </p>
          <p class="text-sm">
            <span class="font-medium text-muted-foreground">단축 URL:</span>
            <a :href="urlInfo.shortUrl" target="_blank" class="text-primary hover:underline ml-2">
              {{ urlInfo.shortUrl }}
            </a>
          </p>
          <p class="text-sm">
            <span class="font-medium text-muted-foreground">총 클릭 수:</span>
            <span class="text-foreground ml-2">{{ urlInfo.clickCount }}</span>
          </p>
        </div>
      </div>

      <!-- Stats -->
      <div v-if="loading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
        <p class="mt-4 text-muted-foreground">로딩 중...</p>
      </div>

      <div v-else-if="stats" class="space-y-6">
        <!-- Clicks by Date -->
        <div class="bg-card border rounded-lg shadow-sm p-6">
          <h2 class="text-xl font-bold text-foreground mb-4">일별 클릭 통계</h2>
          <div v-if="stats.clicksByDate.length > 0" class="h-64">
            <Line :data="dateChartData" :options="dateChartOptions" />
            </div>
          <p v-else class="text-sm text-muted-foreground text-center py-4">
              데이터가 없습니다
            </p>
        </div>

        <!-- Clicks by Hour -->
        <div class="bg-card border rounded-lg shadow-sm p-6">
          <h2 class="text-xl font-bold text-foreground mb-4">시간대별 클릭 통계</h2>
          <div v-if="stats.clicksByHour.length > 0" class="h-64">
            <Bar :data="hourChartData" :options="hourChartOptions" />
            </div>
          <p v-else class="text-sm text-muted-foreground text-center py-4">
              데이터가 없습니다
            </p>
        </div>

        <!-- Clicks by Country -->
        <div class="bg-card border rounded-lg shadow-sm p-6">
          <h2 class="text-xl font-bold text-foreground mb-4">국가별 클릭 통계</h2>
          <div v-if="stats.clicksByCountry.length > 0" class="flex flex-col md:flex-row gap-6">
            <div class="flex-1 h-64">
              <Doughnut :data="countryChartData" :options="pieChartOptions" />
            </div>
            <div class="flex-1 space-y-2">
            <div
              v-for="item in stats.clicksByCountry"
              :key="item.country"
              class="flex items-center justify-between p-3 bg-muted rounded-md"
            >
              <span class="text-sm font-medium text-foreground">{{ item.country }}</span>
              <span class="text-sm text-muted-foreground">{{ item.count }}회</span>
            </div>
            </div>
          </div>
          <p v-else class="text-sm text-muted-foreground text-center py-4">
            데이터가 없습니다
          </p>
        </div>

        <!-- Clicks by City -->
        <div class="bg-card border rounded-lg shadow-sm p-6">
          <h2 class="text-xl font-bold text-foreground mb-4">도시별 클릭 통계</h2>
          <div v-if="stats.clicksByCity.length > 0" class="h-64">
            <Bar :data="cityChartData" :options="barChartOptions" />
            </div>
          <p v-else class="text-sm text-muted-foreground text-center py-4">
              데이터가 없습니다
            </p>
        </div>

        <!-- Clicks by Device Type -->
        <div class="bg-card border rounded-lg shadow-sm p-6">
          <h2 class="text-xl font-bold text-foreground mb-4">기기 유형별 클릭 통계</h2>
          <div v-if="stats.clicksByDeviceType.length > 0" class="flex flex-col md:flex-row gap-6">
            <div class="flex-1 h-64">
              <Doughnut :data="deviceTypeChartData" :options="pieChartOptions" />
            </div>
            <div class="flex-1 space-y-2">
            <div
              v-for="item in stats.clicksByDeviceType"
              :key="item.deviceType"
                class="flex items-center justify-between p-3 bg-muted rounded-md"
            >
                <span class="text-sm font-medium text-foreground">{{ item.deviceType || 'Unknown' }}</span>
                <span class="text-sm text-muted-foreground">{{ item.count }}회</span>
              </div>
            </div>
          </div>
          <p v-else class="text-sm text-muted-foreground text-center py-4">
            데이터가 없습니다
          </p>
        </div>

        <!-- Clicks by Browser -->
        <div class="bg-card border rounded-lg shadow-sm p-6">
          <h2 class="text-xl font-bold text-foreground mb-4">브라우저별 클릭 통계</h2>
          <div v-if="stats.clicksByBrowser.length > 0" class="flex flex-col md:flex-row gap-6">
            <div class="flex-1 h-64">
              <Pie :data="browserChartData" :options="pieChartOptions" />
            </div>
            <div class="flex-1 space-y-2">
            <div
              v-for="item in stats.clicksByBrowser"
              :key="item.browser"
              class="flex items-center justify-between p-3 bg-muted rounded-md"
            >
              <span class="text-sm font-medium text-foreground">{{ item.browser || 'Unknown' }}</span>
              <span class="text-sm text-muted-foreground">{{ item.count }}회</span>
            </div>
            </div>
          </div>
          <p v-else class="text-sm text-muted-foreground text-center py-4">
            데이터가 없습니다
          </p>
        </div>

        <!-- Clicks by OS -->
        <div class="bg-card border rounded-lg shadow-sm p-6">
          <h2 class="text-xl font-bold text-foreground mb-4">운영체제별 클릭 통계</h2>
          <div v-if="stats.clicksByOS.length > 0" class="flex flex-col md:flex-row gap-6">
            <div class="flex-1 h-64">
              <Pie :data="osChartData" :options="pieChartOptions" />
            </div>
            <div class="flex-1 space-y-2">
            <div
              v-for="item in stats.clicksByOS"
              :key="item.os"
              class="flex items-center justify-between p-3 bg-muted rounded-md"
            >
              <span class="text-sm font-medium text-foreground">{{ item.os || 'Unknown' }}</span>
              <span class="text-sm text-muted-foreground">{{ item.count }}회</span>
            </div>
            </div>
          </div>
          <p v-else class="text-sm text-muted-foreground text-center py-4">
            데이터가 없습니다
          </p>
        </div>

        <!-- Clicks by Referer -->
        <div class="bg-card border rounded-lg shadow-sm p-6">
          <h2 class="text-xl font-bold text-foreground mb-4">유입 경로별 클릭 통계</h2>
          <div v-if="stats.clicksByReferer.length > 0" class="h-64">
            <Bar :data="refererChartData" :options="barChartOptions" />
            </div>
          <p v-else class="text-sm text-muted-foreground text-center py-4">
              데이터가 없습니다
            </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { urlService, statsService } from '../services/api'
import { ArrowLeft } from '@element-plus/icons-vue'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js'
import { Line, Bar, Pie, Doughnut } from 'vue-chartjs'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend
)

// shadcn 색상 팔레트
const chartColors = [
  'hsl(221.2 83.2% 53.3%)', // primary
  'hsl(142.1 76.2% 36.3%)', // success
  'hsl(47.9 95.8% 53.1%)',  // warning
  'hsl(0 84.2% 60.2%)',     // destructive
  'hsl(262.1 83.3% 57.8%)', // purple
  'hsl(280 100% 70%)',      // violet
  'hsl(199 89% 48%)',       // cyan
  'hsl(210 40% 96.1%)',     // muted
]

export default {
  name: 'UrlStats',
  components: {
    ArrowLeft,
    Line,
    Bar,
    Pie,
    Doughnut
  },
  data() {
    return {
      shortCode: '',
      urlInfo: null,
      stats: null,
      loading: true
    }
  },
  computed: {
    dateChartData() {
      if (!this.stats || !this.stats.clicksByDate) return null
      return {
        labels: this.stats.clicksByDate.map(item => item.date).reverse(),
        datasets: [{
          label: '클릭 수',
          data: this.stats.clicksByDate.map(item => item.count).reverse(),
          borderColor: 'hsl(221.2 83.2% 53.3%)',
          backgroundColor: 'hsla(221.2 83.2% 53.3% / 0.1)',
          tension: 0.4,
          fill: true
        }]
      }
    },
    dateChartOptions() {
      return {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            backgroundColor: 'hsl(222.2 84% 4.9%)',
            titleColor: 'hsl(210 40% 98%)',
            bodyColor: 'hsl(210 40% 98%)',
            borderColor: 'hsl(217.2 32.6% 17.5%)',
            borderWidth: 1
          }
        },
        scales: {
          x: {
            ticks: {
              color: 'hsl(215.4 16.3% 46.9%)'
            },
            grid: {
              color: 'hsl(214.3 31.8% 91.4%)'
            }
          },
          y: {
            beginAtZero: true,
            ticks: {
              color: 'hsl(215.4 16.3% 46.9%)',
              stepSize: 1
            },
            grid: {
              color: 'hsl(214.3 31.8% 91.4%)'
            }
          }
        }
      }
    },
    hourChartData() {
      if (!this.stats || !this.stats.clicksByHour) return null
      // 24시간 전체 데이터 생성 (없는 시간은 0으로)
      const hourMap = {}
      this.stats.clicksByHour.forEach(item => {
        hourMap[item.hour] = item.count
      })
      const labels = []
      const data = []
      for (let i = 0; i < 24; i++) {
        labels.push(`${i}시`)
        data.push(hourMap[i] || 0)
      }
      return {
        labels,
        datasets: [{
          label: '클릭 수',
          data,
          backgroundColor: 'hsl(221.2 83.2% 53.3%)',
          borderColor: 'hsl(221.2 83.2% 53.3%)',
          borderWidth: 1
        }]
      }
    },
    hourChartOptions() {
      return {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            backgroundColor: 'hsl(222.2 84% 4.9%)',
            titleColor: 'hsl(210 40% 98%)',
            bodyColor: 'hsl(210 40% 98%)',
            borderColor: 'hsl(217.2 32.6% 17.5%)',
            borderWidth: 1
          }
        },
        scales: {
          x: {
            ticks: {
              color: 'hsl(215.4 16.3% 46.9%)'
            },
            grid: {
              color: 'hsl(214.3 31.8% 91.4%)'
            }
          },
          y: {
            beginAtZero: true,
            ticks: {
              color: 'hsl(215.4 16.3% 46.9%)',
              stepSize: 1
            },
            grid: {
              color: 'hsl(214.3 31.8% 91.4%)'
            }
          }
        }
      }
    },
    countryChartData() {
      if (!this.stats || !this.stats.clicksByCountry) return null
      return {
        labels: this.stats.clicksByCountry.map(item => item.country),
        datasets: [{
          data: this.stats.clicksByCountry.map(item => item.count),
          backgroundColor: chartColors.slice(0, this.stats.clicksByCountry.length),
          borderColor: 'hsl(0 0% 100%)',
          borderWidth: 2
        }]
      }
    },
    cityChartData() {
      if (!this.stats || !this.stats.clicksByCity) return null
      const sorted = [...this.stats.clicksByCity].sort((a, b) => b.count - a.count).slice(0, 10)
      return {
        labels: sorted.map(item => item.city),
        datasets: [{
          label: '클릭 수',
          data: sorted.map(item => item.count),
          backgroundColor: 'hsl(142.1 76.2% 36.3%)',
          borderColor: 'hsl(142.1 76.2% 36.3%)',
          borderWidth: 1
        }]
      }
    },
    deviceTypeChartData() {
      if (!this.stats || !this.stats.clicksByDeviceType) return null
      return {
        labels: this.stats.clicksByDeviceType.map(item => item.deviceType || 'Unknown'),
        datasets: [{
          data: this.stats.clicksByDeviceType.map(item => item.count),
          backgroundColor: chartColors.slice(0, this.stats.clicksByDeviceType.length),
          borderColor: 'hsl(0 0% 100%)',
          borderWidth: 2
        }]
      }
    },
    browserChartData() {
      if (!this.stats || !this.stats.clicksByBrowser) return null
      return {
        labels: this.stats.clicksByBrowser.map(item => item.browser || 'Unknown'),
        datasets: [{
          data: this.stats.clicksByBrowser.map(item => item.count),
          backgroundColor: chartColors.slice(0, this.stats.clicksByBrowser.length),
          borderColor: 'hsl(0 0% 100%)',
          borderWidth: 2
        }]
      }
    },
    osChartData() {
      if (!this.stats || !this.stats.clicksByOS) return null
      return {
        labels: this.stats.clicksByOS.map(item => item.os || 'Unknown'),
        datasets: [{
          data: this.stats.clicksByOS.map(item => item.count),
          backgroundColor: chartColors.slice(0, this.stats.clicksByOS.length),
          borderColor: 'hsl(0 0% 100%)',
          borderWidth: 2
        }]
      }
    },
    refererChartData() {
      if (!this.stats || !this.stats.clicksByReferer) return null
      const sorted = [...this.stats.clicksByReferer].sort((a, b) => b.count - a.count).slice(0, 10)
      return {
        labels: sorted.map(item => {
          const referer = item.referer || 'Direct'
          return referer.length > 30 ? referer.substring(0, 30) + '...' : referer
        }),
        datasets: [{
          label: '클릭 수',
          data: sorted.map(item => item.count),
          backgroundColor: 'hsl(262.1 83.3% 57.8%)',
          borderColor: 'hsl(262.1 83.3% 57.8%)',
          borderWidth: 1
        }]
      }
    },
    pieChartOptions() {
      return {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              color: 'hsl(215.4 16.3% 46.9%)',
              padding: 15,
              usePointStyle: true
            }
          },
          tooltip: {
            backgroundColor: 'hsl(222.2 84% 4.9%)',
            titleColor: 'hsl(210 40% 98%)',
            bodyColor: 'hsl(210 40% 98%)',
            borderColor: 'hsl(217.2 32.6% 17.5%)',
            borderWidth: 1
          }
        }
      }
    },
    barChartOptions() {
      return {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            backgroundColor: 'hsl(222.2 84% 4.9%)',
            titleColor: 'hsl(210 40% 98%)',
            bodyColor: 'hsl(210 40% 98%)',
            borderColor: 'hsl(217.2 32.6% 17.5%)',
            borderWidth: 1
          }
        },
        scales: {
          x: {
            ticks: {
              color: 'hsl(215.4 16.3% 46.9%)'
            },
            grid: {
              color: 'hsl(214.3 31.8% 91.4%)'
            }
          },
          y: {
            beginAtZero: true,
            ticks: {
              color: 'hsl(215.4 16.3% 46.9%)',
              stepSize: 1
            },
            grid: {
              color: 'hsl(214.3 31.8% 91.4%)'
            }
          }
        }
      }
    }
  },
  mounted() {
    this.shortCode = this.$route.params.shortCode
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const [urlResponse, statsResponse] = await Promise.all([
          urlService.getUrlInfo(this.shortCode),
          statsService.getUrlStats(this.shortCode, 30)
        ])
        this.urlInfo = urlResponse.data
        this.stats = statsResponse.data
      } catch (err) {
        console.error('Error loading stats:', err)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>
