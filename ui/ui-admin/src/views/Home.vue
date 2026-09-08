<script setup>

  import {ref, computed, onMounted, onUnmounted, nextTick} from "vue";
  import {useRouter} from "vue-router";
  import * as echarts from "echarts";
  import dashboardApi from "@/api/dashboard.js";
  import {UserInfoStore} from '@/store/userInfo.js'
  import {
    OfficeBuilding, House, Grid, CircleCheck, User, UserFilled,
    Bell, FirstAidKit, Calendar, ArrowRight
  } from "@element-plus/icons-vue";

  const router = useRouter()
  const userInfoStore = UserInfoStore();

  // 问候语
  const greeting = (() => {
    const hour = new Date().getHours()
    if (hour < 6) return '凌晨好'
    if (hour < 9) return '早上好'
    if (hour < 12) return '上午好'
    if (hour < 14) return '中午好'
    if (hour < 18) return '下午好'
    return '晚上好'
  })()

  const today = new Date().toLocaleDateString('zh-CN', {
    year: 'numeric', month: 'long', day: 'numeric', weekday: 'long'
  })

  // 统计数据
  const stats = ref({})
  const loadStats = () => {
    dashboardApi.stats().then(result => {
      stats.value = result.data || {}
    })
  }
  loadStats()

  // 统计卡片
  const statCards = [
    {label: '老人总数', key: 'elderCount', icon: UserFilled, color: '#0d9488', bg: '#e4f4f2'},
    {label: '楼栋数量', key: 'buildingCount', icon: OfficeBuilding, color: '#3b82f6', bg: '#e8f1fe'},
    {label: '房间总数', key: 'roomCount', icon: House, color: '#f59e0b', bg: '#fdf3e0'},
    {label: '床位总数', key: 'bedCount', icon: Grid, color: '#8b5cf6', bg: '#f1ecfd'},
    {label: '入住老人', key: 'occupiedCount', icon: User, color: '#ef4444', bg: '#fdecec'},
    {label: '用户总数', key: 'userCount', icon: User, color: '#64748b', bg: '#eef2f6'},
  ]

  // 待办事项
  const todos = computed(() => [
    {label: '待执行护理任务', count: stats.value.pendingCareTaskCount || 0, icon: FirstAidKit, color: '#0d9488', bg: '#e4f4f2', path: '/care-task'},
    {label: '待体检预约', count: stats.value.pendingExamCount || 0, icon: Calendar, color: '#3b82f6', bg: '#e8f1fe', path: '/exam-appointment'},
    {label: '待审批请假', count: stats.value.pendingLeaveCount || 0, icon: ArrowRight, color: '#3b82f6', bg: '#e8f1fe', path: '/leave-approval'},
  ])

  // 快捷入口
  const quickLinks = [
    {label: '老人管理', icon: UserFilled, path: '/elder', color: '#0d9488'},
    {label: '入住分配', icon: House, path: '/checkin', color: '#f59e0b'},
    {label: '护理计划', icon: FirstAidKit, path: '/care-plan', color: '#8b5cf6'},
    {label: '体检套餐', icon: Calendar, path: '/exam-package', color: '#3b82f6'},
    {label: '楼栋管理', icon: OfficeBuilding, path: '/building', color: '#ef4444'},
    {label: '请假记录', icon: Grid, path: '/elder-leave', color: '#64748b'},
  ]

  // 入住率
  const occupancyRate = computed(() => {
    const bedCount = Number(stats.value.bedCount) || 0
    const occupied = Number(stats.value.occupiedCount) || 0
    if (!bedCount) return 0
    return Math.round(occupied / bedCount * 100)
  })

  // 老人年龄分布（ECharts环形饼图）
  const ageChartRef = ref(null)
  const hasAgeData = ref(false)
  let ageChart = null

  const loadAgeDistribution = () => {
    dashboardApi.ageDistribution().then(result => {
      //人数为0的年龄段不展示，避免图例出现空项
      const data = (result.data || []).filter(item => item.value > 0)
      hasAgeData.value = data.length > 0
      if (!hasAgeData.value) return
      //等容器渲染出实际尺寸后再初始化/重置画布，避免在隐藏状态(display:none)下初始化导致图表缩成一点
      nextTick(() => {
        if (!ageChart) {
          ageChart = echarts.init(ageChartRef.value)
        }
        ageChart.resize()
        ageChart.setOption({
          color: ['#0d9488', '#3b82f6', '#f59e0b', '#8b5cf6', '#ef4444', '#94a3b8'],
          tooltip: {trigger: 'item', formatter: '{b}：{c}人（{d}%）'},
          legend: {bottom: 0, icon: 'circle'},
          series: [{
            name: '老人年龄分布',
            type: 'pie',
            radius: ['50%', '74%'],
            center: ['50%', '46%'],
            avoidLabelOverlap: true,
            itemStyle: {borderRadius: 6, borderColor: '#fff', borderWidth: 2},
            label: {formatter: '{b}\n{c}人'},
            data
          }]
        })
      })
    })
  }

  //窗口尺寸变化时图表自适应
  const onChartResize = () => ageChart?.resize()

  onMounted(() => {
    loadAgeDistribution()
    window.addEventListener('resize', onChartResize)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', onChartResize)
    ageChart?.dispose()
    ageChart = null
  })
</script>

<template>
  <div class="home-page">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-text">
        <div class="welcome-title">{{ greeting }}，{{ userInfoStore.user.name }}</div>
        <div class="welcome-sub">欢迎使用智慧养老社区管理系统 · {{ today }}</div>
      </div>
      <el-icon :size="72" class="welcome-icon"><Bell/></el-icon>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div v-for="card in statCards" :key="card.key" class="stat-card">
        <div class="stat-icon" :style="{color: card.color, backgroundColor: card.bg}">
          <el-icon :size="26"><component :is="card.icon"/></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats[card.key] ?? 0 }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </div>
    </div>

    <div class="main-grid">
      <!-- 老人年龄分布 -->
      <el-card class="section-card chart-card">
        <template #header>
          <span class="section-title">老人年龄分布</span>
        </template>
        <div v-show="hasAgeData" ref="ageChartRef" class="age-chart"></div>
        <el-empty v-if="!hasAgeData" description="暂无老人数据" :image-size="80"/>
      </el-card>

      <!-- 右侧：今日待办 + 入住概况 -->
      <div class="side-col">
        <el-card class="section-card todo-card">
          <template #header>
            <span class="section-title">今日待办</span>
          </template>
          <div v-for="todo in todos" :key="todo.label" class="todo-item" @click="router.push(todo.path)">
            <div class="todo-icon" :style="{color: todo.color, backgroundColor: todo.bg}">
              <el-icon :size="22"><component :is="todo.icon"/></el-icon>
            </div>
            <span class="todo-label">{{ todo.label }}</span>
            <el-badge :value="todo.count" :type="todo.count > 0 ? 'danger' : 'info'" class="todo-badge"/>
            <el-icon class="todo-arrow"><ArrowRight/></el-icon>
          </div>
        </el-card>

        <el-card class="section-card">
          <template #header>
            <span class="section-title">入住概况</span>
          </template>
          <div class="occupancy-compact">
            <div class="occupancy-top">
              <span class="occupancy-num">{{ occupancyRate }}%</span>
              <span class="occupancy-label">当前入住率</span>
            </div>
            <el-progress :percentage="occupancyRate" :show-text="false" :stroke-width="10" color="#0d9488"/>
            <div class="occupancy-row">
              <div class="occupancy-cell">
                <div class="occ-text">{{ stats.occupiedCount ?? 0 }}</div>
                <div class="cell-label">已入住床位</div>
              </div>
              <div class="occupancy-cell">
                <div class="free-text">{{ stats.freeBedCount ?? 0 }}</div>
                <div class="cell-label">空闲床位</div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 快捷入口 -->
    <el-card class="section-card">
      <template #header>
        <span class="section-title">快捷入口</span>
      </template>
      <div class="quick-grid">
        <div v-for="link in quickLinks" :key="link.path" class="quick-item" @click="router.push(link.path)">
          <el-icon :size="30" :style="{color: link.color}"><component :is="link.icon"/></el-icon>
          <span class="quick-label">{{ link.label }}</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.home-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 欢迎横幅：青色渐变 */
.welcome-banner {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28px 32px;
  border-radius: 12px;
  color: #fff;
  background: linear-gradient(120deg, #0f766e 0%, #14b8a6 60%, #2dd4bf 100%);
  box-shadow: 0 8px 24px rgba(13, 148, 136, 0.25);
  overflow: hidden;

  //装饰圆
  &::before,
  &::after {
    content: '';
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.12);
  }

  &::before {
    width: 200px;
    height: 200px;
    right: 80px;
    top: -100px;
  }

  &::after {
    width: 130px;
    height: 130px;
    right: -40px;
    bottom: -70px;
  }

  .welcome-title {
    font-size: 24px;
    font-weight: 700;
    letter-spacing: 1px;
  }

  .welcome-sub {
    margin-top: 8px;
    font-size: 13px;
    color: rgba(255, 255, 255, 0.85);
  }

  .welcome-icon {
    color: rgba(255, 255, 255, 0.5);
    z-index: 1;
  }
}

/* 统计卡片网格 */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;

  @media (max-width: 1400px) {
    grid-template-columns: repeat(3, 1fr);
  }

  .stat-card {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 18px 20px;
    border-radius: 10px;
    background-color: #fff;
    box-shadow: 0 2px 12px rgba(20, 60, 55, 0.06);
    cursor: default;
    transition: transform 0.2s;

    &:hover {
      transform: translateY(-3px);
    }

    .stat-icon {
      width: 52px;
      height: 52px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .stat-value {
      font-size: 26px;
      font-weight: 700;
      color: #1f3835;
      line-height: 1.1;
    }

    .stat-label {
      margin-top: 4px;
      font-size: 13px;
      color: #8aa5a1;
    }
  }
}

/* 主区域：左侧图表 + 右侧信息栏 */
.main-grid {
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 16px;

  @media (max-width: 1100px) {
    grid-template-columns: 1fr;
  }
}

.side-col {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;

  //待办卡片撑满剩余高度
  .todo-card {
    flex: 1;
  }
}

.section-card {
  .section-title {
    font-size: 15px;
    font-weight: 600;
    color: #1f3835;
  }
}

/* 待办事项 */
.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background-color: #f3f8f7;
  }

  .todo-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .todo-label {
    flex: 1;
    font-size: 14px;
    color: #374b48;
  }

  .todo-arrow {
    color: #b5c9c6;
  }
}

/* 入住概况（紧凑版） */
.occupancy-compact {
  display: flex;
  flex-direction: column;
  gap: 14px;

  .occupancy-top {
    display: flex;
    align-items: baseline;
    gap: 8px;
  }

  .occupancy-num {
    font-size: 34px;
    font-weight: 700;
    color: #0d9488;
    line-height: 1;
  }

  .occupancy-label {
    font-size: 13px;
    color: #8aa5a1;
  }

  .occupancy-row {
    display: flex;
    gap: 12px;

    .occupancy-cell {
      flex: 1;
      padding: 10px 14px;
      border-radius: 8px;
      background-color: #f6f9f8;
      text-align: center;
    }

    .occ-text {
      font-size: 20px;
      font-weight: 700;
      color: #0d9488;
    }

    .free-text {
      font-size: 20px;
      font-weight: 700;
      color: #64748b;
    }

    .cell-label {
      margin-top: 2px;
      font-size: 12px;
      color: #8aa5a1;
    }
  }
}

/* 老人年龄分布图表 */
.age-chart {
  width: 100%;
  height: 460px;
}

/* 快捷入口 */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;

  @media (max-width: 1000px) {
    grid-template-columns: repeat(3, 1fr);
  }

  .quick-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    padding: 20px 8px;
    border-radius: 10px;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
      background-color: #f3f8f7;
    }

    .quick-label {
      font-size: 13px;
      color: #374b48;
    }
  }
}
</style>
