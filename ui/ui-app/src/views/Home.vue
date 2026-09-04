<script setup>
  import homeApi from '@/api/home.js'
  import appointmentApi from '@/api/appointment.js'
  import {onMounted, ref, computed} from 'vue'
  import {elderElderInfoStore} from '@/store/elderInfo.js'
  import {useAppointmentStore} from '@/store/appointment.js'
  import {useRouter} from 'vue-router'
  const router = useRouter()
  const elderInfoStore = elderElderInfoStore();
  const appointmentStore = useAppointmentStore();

  const greeting = computed(() => '你好，' + elderInfoStore.elder.name)
  const notices = ref([])
  const noticeShow = ref(false)

  onMounted(() => {
    homeApi.notices().then(result => {
      notices.value = result.data
    })
    loadLatestExam()
  })

  //最近一次已完成体检的预约及项目明细
  const latestExam = ref(null)
  const latestItems = ref([])

  const loadLatestExam = () => {
    appointmentApi.list().then(result => {
      if (result.code !== 1) {
        return
      }
      //列表已按预约日期倒序，取最近一个已完成的预约
      latestExam.value = (result.data || []).find(item => item.status === 2) || null
      if (latestExam.value) {
        appointmentApi.listItems(latestExam.value.id).then(res => {
          latestItems.value = res.data || []
        })
      }
    })
  }

  //格式化单项结果：数值型显示数值（单位并入标签），文本型直接显示
  const formatValue = (item) => {
    if (item.resultType === 1) {
      return item.resultValue != null ? String(item.resultValue) : '-'
    }
    return item.resultText || '-'
  }

  //健康数据卡片展示的项：优先匹配 血压/心率/血糖，不足时用其他有结果的项目补齐
  const healthItems = computed(() => {
    const withResult = latestItems.value.filter(item =>
        item.resultValue != null || (item.resultText && item.resultText.trim()))
    const picked = []
    const keywordPrefs = [['血压'], ['心率', '脉搏'], ['血糖']]
    for (const keywords of keywordPrefs) {
      if (picked.length >= 3) break
      const hit = withResult.find(item =>
          !picked.includes(item) && keywords.some(k => (item.itemName || '').includes(k)))
      if (hit) {
        picked.push(hit)
      }
    }
    //数值型结果优先展示（适合大数字卡片），文本型兜底补位
    const sorted = [...withResult].sort((a, b) => (b.resultType === 1 ? 1 : 0) - (a.resultType === 1 ? 1 : 0))
    for (const item of sorted) {
      if (picked.length >= 4) break
      if (!picked.includes(item)) {
        picked.push(item)
      }

    }
    //没有可展示的结果时显示占位
    if (!picked.length) {
      return [
        {value: '-', label: '血压(mmHg)'},
        {value: '-', label: '心率(次/分)'},
        {value: '-', label: '血糖(mmol/L)'}
      ]
    }
    return picked.map(item => ({
      value: formatValue(item),
      //数值型把单位拼进标签，如 血糖(mmol/L)
      label: item.resultType === 1 && item.unit ? `${item.itemName}(${item.unit})` : item.itemName
    }))
  })

  const hasExam = computed(() => !!latestExam.value)

  //点击健康卡片：有体检报告去我的预约查看，没有去挑选套餐
  const goHealth = () => {
    router.push(hasExam.value ? '/appointment' : '/package')
  }

  //快捷入口
  const shortcuts = [
    {name: '体检预约', icon: 'calendar-o', color: '#1989fa', to: '/package'},
    {name: '我的预约', icon: 'clock-o', color: '#07c160', to: '/appointment'},
    {name: '健康档案', icon: 'records', color: '#ff976a', to: '/profile'},
    {name: '智能咨询', icon: 'chat-o', color: '#ee0a24', to: '/chat'}
  ]
</script>

<template>
  <div class="home">
    <!--头部问候-->
    <div class="home-header">
      <div class="greeting">{{ greeting }}</div>
      <div class="date">{{ elderInfoStore.elder.name }} · 今天也要注意身体哦</div>
    </div>

    <!--健康数据卡片：展示最近一次体检结果-->
    <div class="health-card" @click="goHealth">
      <div class="health-title">
        <span>{{ hasExam ? '最近体检数据' : '今日健康数据' }}</span>
        <span v-if="hasExam" class="health-date">{{ latestExam.appointmentDate }}</span>
      </div>
      <div class="health-grid">
        <div class="health-item" v-for="(item, index) in healthItems" :key="index">
          <div class="value">{{ item.value }}</div>
          <div class="label">{{ item.label }}</div>
        </div>
      </div>
      <div class="health-source">
        {{ hasExam ? '数据来自最近一次体检报告，点击查看' : '暂无体检报告，点击去预约' }}
        <van-icon name="arrow"/>
      </div>
    </div>

    <!--快捷入口-->
    <div class="shortcuts">
      <div v-for="item in shortcuts" :key="item.name" class="shortcut-item" @click="item.to && router.push(item.to)">
        <van-icon :name="item.icon" :color="item.color" size="26"/>
        <span>{{ item.name }}</span>
      </div>
    </div>

    <!--公告-->
    <van-cell-group inset class="notice-cell">
      <van-notice-bar left-icon="volume-o" :text="notices[0] ? notices[0].title : '暂无公告'" @click="noticeShow = true" color="#1989fa" background="#f0f7ff"/>
    </van-cell-group>

    <!--推荐套餐-->
    <div class="section">
      <div class="section-title">
        <span class="bar"></span>
        热门体检套餐
      </div>
      <div class="package-row" @click="router.push('/package/1')">
        <div class="package-thumb" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">基础</div>
        <div class="package-info">
          <div class="name">基础体检套餐</div>
          <div class="desc">血常规、尿常规、心电图等5项基础检查</div>
        </div>
        <div class="price">¥199</div>
      </div>
      <div class="package-row" @click="router.push('/package/3')">
        <div class="package-thumb" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">深度</div>
        <div class="package-info">
          <div class="name">深度体检套餐</div>
          <div class="desc">15项检查，含肿瘤标志物、心脑血管专项筛查</div>
        </div>
        <div class="price">¥699</div>
      </div>
    </div>

    <!--待体检提醒-->
    <van-cell-group inset v-if="appointmentStore.pendingList.length">
      <van-cell title="您有预约待体检" :value="appointmentStore.pendingList.length + '个'" is-link @click="router.push('/appointment')"/>
    </van-cell-group>

    <!--公告弹层-->
    <van-popup v-model:show="noticeShow" position="bottom" round>
      <div class="notice-popup">
        <div class="notice-popup-title">社区公告</div>
        <div v-for="notice in notices" :key="notice.id" class="notice-item">
          <div class="notice-content">{{ notice.title }}</div>
          <div class="notice-date">{{ notice.date }}</div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<style scoped lang="scss">
  .home {
    padding-bottom: 20px;
  }

  .home-header {
    padding: 20px 16px 40px;
    background: linear-gradient(180deg, #1989fa 0%, #5cadff 60%, #f5f6f8 100%);

    .greeting {
      font-size: 20px;
      font-weight: bold;
      color: #fff;
    }

    .date {
      margin-top: 6px;
      font-size: 12px;
      color: rgba(255, 255, 255, 0.9);
    }
  }

  .health-card {
    margin: -24px 16px 0;
    padding: 16px;
    border-radius: 12px;
    background-color: #fff;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

    .health-title {
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 15px;
      font-weight: bold;
      color: #323233;
      margin-bottom: 12px;

      .health-date {
        font-size: 11px;
        font-weight: normal;
        color: #969799;
      }
    }

    .health-source {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 2px;
      margin-top: 12px;
      padding-top: 10px;
      border-top: 1px solid #f2f3f5;
      font-size: 11px;
      color: #969799;
    }

    .health-grid {
      display: flex;

      .health-item {
        flex: 1;
        min-width: 0; //允许内容收缩，防止长文本撑破布局
        padding: 0 4px;
        text-align: center;

        .value {
          font-size: 18px;
          font-weight: bold;
          color: #1989fa;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .label {
          margin-top: 4px;
          font-size: 11px;
          color: #969799;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
    }
  }

  .shortcuts {
    display: flex;
    margin: 16px 16px 0;
    padding: 16px 0;
    border-radius: 12px;
    background-color: #fff;

    .shortcut-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      font-size: 12px;
      color: #323233;
    }
  }

  .notice-cell {
    margin-top: 16px;
  }

  .section {
    margin: 16px 16px 0;

    .section-title {
      display: flex;
      align-items: center;
      font-size: 15px;
      font-weight: bold;
      color: #323233;
      margin-bottom: 12px;

      .bar {
        width: 4px;
        height: 16px;
        margin-right: 8px;
        border-radius: 2px;
        background-color: #1989fa;
      }
    }
  }

  .package-row {
    display: flex;
    align-items: center;
    padding: 12px;
    margin-bottom: 10px;
    border-radius: 12px;
    background-color: #fff;

    .package-thumb {
      width: 52px;
      height: 52px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 14px;
      font-weight: bold;
    }

    .package-info {
      flex: 1;
      margin: 0 12px;
      overflow: hidden;

      .name {
        font-size: 15px;
        font-weight: bold;
        color: #323233;
      }

      .desc {
        margin-top: 4px;
        font-size: 11px;
        color: #969799;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }

    .price {
      font-size: 16px;
      font-weight: bold;
      color: #ee0a24;
    }
  }

  .notice-popup {
    padding: 20px 16px 30px;

    .notice-popup-title {
      text-align: center;
      font-size: 16px;
      font-weight: bold;
      color: #323233;
      margin-bottom: 16px;
    }

    .notice-item {
      padding: 12px 0;
      border-bottom: 1px solid #f2f3f5;

      .notice-content {
        font-size: 14px;
        color: #323233;
        line-height: 1.5;
      }

      .notice-date {
        margin-top: 6px;
        font-size: 11px;
        color: #969799;
      }
    }
  }
</style>
