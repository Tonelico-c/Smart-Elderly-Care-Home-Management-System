<script setup>

  import {ref, computed} from "vue";
  import {useRouter} from "vue-router";
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
    {label: '入住老人', key: 'elderCount', icon: UserFilled, color: '#0d9488', bg: '#e4f4f2'},
    {label: '楼栋数量', key: 'buildingCount', icon: OfficeBuilding, color: '#3b82f6', bg: '#e8f1fe'},
    {label: '房间总数', key: 'roomCount', icon: House, color: '#f59e0b', bg: '#fdf3e0'},
    {label: '床位总数', key: 'bedCount', icon: Grid, color: '#8b5cf6', bg: '#f1ecfd'},
    {label: '入住人数', key: 'occupiedCount', icon: User, color: '#ef4444', bg: '#fdecec'},
    {label: '空闲床位', key: 'freeBedCount', icon: CircleCheck, color: '#64748b', bg: '#eef2f6'},
  ]

  // 待办事项
  const todos = computed(() => [
    {label: '待执行护理任务', count: stats.value.pendingCareTaskCount || 0, icon: FirstAidKit, color: '#0d9488', path: '/care-task'},
    {label: '待体检预约', count: stats.value.pendingExamCount || 0, icon: Calendar, color: '#3b82f6', path: '/exam-appointment'},
  ])

  // 快捷入口
  const quickLinks = [
    {label: '老人管理', icon: UserFilled, path: '/elder', color: '#0d9488'},
    {label: '入住分配', icon: House, path: '/checkin', color: '#f59e0b'},
    {label: '护理计划', icon: FirstAidKit, path: '/care-plan', color: '#8b5cf6'},
    {label: '体检套餐', icon: Calendar, path: '/exam-package', color: '#3b82f6'},
    {label: '楼栋管理', icon: OfficeBuilding, path: '/building', color: '#ef4444'},
    {label: '床位管理', icon: Grid, path: '/bed', color: '#64748b'},
  ]

  // 入住率
  const occupancyRate = computed(() => {
    const bedCount = Number(stats.value.bedCount) || 0
    const occupied = Number(stats.value.occupiedCount) || 0
    if (!bedCount) return 0
    return Math.round(occupied / bedCount * 100)
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

    <div class="section-grid">
      <!-- 待办事项 -->
      <el-card class="section-card">
        <template #header>
          <span class="section-title">今日待办</span>
        </template>
        <div v-for="todo in todos" :key="todo.label" class="todo-item" @click="router.push(todo.path)">
          <div class="todo-icon" :style="{color: todo.color, backgroundColor: '#f0f4f3'}">
            <el-icon :size="22"><component :is="todo.icon"/></el-icon>
          </div>
          <span class="todo-label">{{ todo.label }}</span>
          <el-badge :value="todo.count" :type="todo.count > 0 ? 'danger' : 'info'" class="todo-badge"/>
          <el-icon class="todo-arrow"><ArrowRight/></el-icon>
        </div>
        <el-empty v-if="todos.length === 0" description="暂无待办" :image-size="60"/>
      </el-card>

      <!-- 入住概况 -->
      <el-card class="section-card">
        <template #header>
          <span class="section-title">入住概况</span>
        </template>
        <div class="occupancy-box">
          <el-progress type="dashboard" :percentage="occupancyRate" :width="150"
                       :color="[{color: '#0d9488', percentage: 100}]">
            <template #default>
              <div class="occupancy-num">{{ occupancyRate }}%</div>
              <div class="occupancy-label">入住率</div>
            </template>
          </el-progress>
        </div>
        <div class="occupancy-row">
          <span>已入住床位：<b class="occ-text">{{ stats.occupiedCount ?? 0 }}</b></span>
          <span>空闲床位：<b class="free-text">{{ stats.freeBedCount ?? 0 }}</b></span>
        </div>
      </el-card>
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28px 32px;
  border-radius: 12px;
  color: #fff;
  background: linear-gradient(120deg, #0f766e 0%, #14b8a6 60%, #2dd4bf 100%);
  box-shadow: 0 8px 24px rgba(13, 148, 136, 0.25);

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

/* 双栏区块 */
.section-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;

  @media (max-width: 1000px) {
    grid-template-columns: 1fr;
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

/* 入住概况 */
.occupancy-box {
  display: flex;
  justify-content: center;
  padding: 8px 0 16px;

  .occupancy-num {
    font-size: 28px;
    font-weight: 700;
    color: #0d9488;
  }

  .occupancy-label {
    margin-top: 4px;
    font-size: 13px;
    color: #8aa5a1;
  }
}

.occupancy-row {
  display: flex;
  justify-content: space-around;
  font-size: 13px;
  color: #5b7470;

  .occ-text {
    color: #0d9488;
    font-size: 16px;
  }

  .free-text {
    color: #64748b;
    font-size: 16px;
  }
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
