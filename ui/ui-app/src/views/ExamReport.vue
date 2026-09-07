<script setup>
  import {onMounted, ref, computed} from 'vue'
  import {useRoute, useRouter} from 'vue-router'
  import {showToast} from 'vant'
  import appointmentApi from '@/api/appointment.js'
  import chatApi from '@/api/chat.js'

  const route = useRoute()
  const router = useRouter()

  //加载状态
  const loading = ref(true)

  //报告对应的预约
  const appointment = ref({})

  //报告的体检项目明细列表
  const reportItems = ref([])

  onMounted(() => {
    loadReport()
  })

  //加载报告：先从预约列表中找到该预约，再拉取项目明细
  //注意：后端Long序列化为字符串，id比较时统一转成字符串
  const loadReport = () => {
    const id = String(route.params.id)
    appointmentApi.list().then(result => {
      if (result.code === 1) {
        appointment.value = (result.data || []).find(item => String(item.id) === id) || {}
      }
    }).finally(() => {
      appointmentApi.listItems(id).then(result => {
        if (result.code === 1) {
          reportItems.value = result.data || []
        } else {
          showToast(result.msg)
        }
      }).finally(() => {
        loading.value = false
      })
    })
  }

  //项目状态（与后端一致：0待检查 1正常 2异常 3未完成）
  const itemStatusOptions = [
    {value: 0, label: '待检查', color: '#969799'},
    {value: 1, label: '正常', color: '#07c160'},
    {value: 2, label: '异常', color: '#ee0a24'},
    {value: 3, label: '未完成', color: '#ff976a'}
  ]
  const itemStatusText = (status) => {
    const item = itemStatusOptions.find(i => i.value === status)
    return item ? item.label : '未知'
  }
  const itemStatusColor = (status) => {
    const item = itemStatusOptions.find(i => i.value === status)
    return item ? item.color : '#969799'
  }

  //格式化单项的体检结果：数值型显示数值+单位，文本型显示文本
  const formatResult = (item) => {
    if (item.resultType === 1) {
      return item.resultValue != null ? `${item.resultValue}${item.unit ? ' ' + item.unit : ''}` : '-'
    }
    return item.resultText || '-'
  }

  //格式化参考范围
  const formatReference = (item) => {
    if (item.referenceMin == null && item.referenceMax == null) {
      return '-'
    }
    return `${item.referenceMin ?? '-'} ~ ${item.referenceMax ?? '-'}${item.referenceUnit ? ' ' + item.referenceUnit : ''}`
  }

  //异常项目数量汇总
  const abnormalCount = computed(() => reportItems.value.filter(item => item.abnormal === 1).length)

  //AI健康分析：弹层展示 + 流式接收分析内容
  const analysisShow = ref(false)
  const analysisLoading = ref(false)
  const analysisText = ref('')

  const startAnalysis = () => {
    if (analysisLoading.value) {
      return
    }
    analysisShow.value = true
    analysisLoading.value = true
    analysisText.value = ''
    chatApi.reportAnalysisStream(String(route.params.id), {
      onMessage: chunk => {
        analysisText.value += chunk
      },
      onDone: () => {
        analysisLoading.value = false
      },
      onError: () => {
        analysisLoading.value = false
        showToast('分析获取失败，请稍后再试')
      }
    })
  }
</script>

<template>
  <div class="report-page">
    <van-nav-bar title="体检报告" left-arrow fixed placeholder @click-left="router.back()"/>

    <van-loading v-if="loading" class="page-loading" size="24" vertical>报告加载中...</van-loading>

    <template v-else>
      <!--预约信息-->
      <div class="report-info">
        <div class="info-line">
          <span class="label">体检套餐</span>
          <span class="value">{{ appointment.packageName }}</span>
        </div>
        <div class="info-line">
          <span class="label">体检人</span>
          <span class="value">{{ appointment.elderName }}</span>
        </div>
        <div class="info-line">
          <span class="label">体检日期</span>
          <span class="value">{{ appointment.appointmentDate }} {{ appointment.appointmentTime }}</span>
        </div>
        <!--异常项汇总-->
        <div class="summary" :class="abnormalCount > 0 ? 'warn' : 'ok'">
          <template v-if="reportItems.length">
            共 {{ reportItems.length }} 项检查，
            <template v-if="abnormalCount > 0">{{ abnormalCount }} 项指标异常，请及时咨询医生</template>
            <template v-else>各项指标均正常</template>
          </template>
        </div>
        <!--AI健康分析入口-->
        <van-button v-if="reportItems.length" class="analysis-btn" type="primary" size="small" round block plain
                    icon="chat-o" :loading="analysisLoading" @click="startAnalysis">AI健康分析</van-button>
      </div>

      <!--项目明细列表-->
      <div v-if="reportItems.length" class="report-list">
        <div v-for="item in reportItems" :key="item.id" class="report-item">
          <div class="item-top">
            <span class="item-name">{{ item.itemName }}</span>
            <span class="item-status" :style="{color: itemStatusColor(item.status)}">{{ itemStatusText(item.status) }}</span>
          </div>
          <div class="item-body">
            <div class="item-line">
              <span class="label">体检结果</span>
              <span class="value" :style="item.abnormal === 1 ? 'color:#ee0a24;font-weight:bold' : ''">{{ formatResult(item) }}</span>
            </div>
            <div class="item-line">
              <span class="label">参考范围</span>
              <span class="value">{{ formatReference(item) }}</span>
            </div>
            <div v-if="item.remark" class="item-line">
              <span class="label">备注</span>
              <span class="value">{{ item.remark }}</span>
            </div>
          </div>
        </div>
      </div>
      <van-empty v-else description="暂无体检项目明细" :image-size="80"/>
    </template>

    <!--AI健康分析弹层：流式展示分析内容-->
    <van-popup v-model:show="analysisShow" position="bottom" round>
      <div class="analysis-popup">
        <div class="analysis-title">AI 健康分析</div>
        <div class="analysis-body">
          <template v-if="analysisText">{{ analysisText }}</template>
          <van-loading v-else-if="analysisLoading" size="20" vertical>小智正在分析报告...</van-loading>
        </div>
        <div class="analysis-footer">
          <span>内容由AI生成，仅供参考，不构成医疗建议</span>
          <van-button type="primary" size="small" round plain :loading="analysisLoading"
                      @click="startAnalysis">重新分析</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<style scoped lang="scss">
  .report-page {
    min-height: 100vh;
    padding-bottom: 20px;
    background-color: #f5f6f8;
  }

  .page-loading {
    padding: 60px 0;
  }

  //预约信息 + 汇总
  .report-info {
    margin: 12px 16px 0;
    padding: 12px 14px;
    border-radius: 12px;
    background-color: #fff;

    .info-line {
      display: flex;
      justify-content: space-between;
      font-size: 13px;
      padding: 5px 0;

      .label {
        color: #969799;
      }

      .value {
        color: #323233;
        font-weight: bold;
      }
    }

    .summary {
      margin-top: 8px;
      padding: 8px 12px;
      border-radius: 8px;
      font-size: 12px;

      //有异常项
      &.warn {
        background-color: #fff2ee;
        color: #ee0a24;
      }

      //全部正常
      &.ok {
        background-color: #f0fff4;
        color: #07c160;
      }
    }

    //AI健康分析按钮
    .analysis-btn {
      margin-top: 10px;
    }
  }

  //AI健康分析弹层
  .analysis-popup {
    display: flex;
    flex-direction: column;
    max-height: 70vh;
    padding: 20px 16px 30px;

    .analysis-title {
      text-align: center;
      font-size: 16px;
      font-weight: bold;
      color: #323233;
      margin-bottom: 12px;
    }

    .analysis-body {
      flex: 1;
      min-height: 120px;
      overflow-y: auto;
      font-size: 14px;
      line-height: 1.8;
      color: #323233;
      //保留AI输出中的换行
      white-space: pre-wrap;
    }

    .analysis-footer {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 8px;
      margin-top: 12px;
      padding-top: 10px;
      border-top: 1px solid #f2f3f5;
      font-size: 11px;
      color: #969799;
    }
  }

  //项目明细列表
  .report-list {
    padding: 12px 16px 0;

    .report-item {
      padding: 10px 12px;
      margin-bottom: 10px;
      border-radius: 10px;
      background-color: #fff;

      .item-top {
        display: flex;
        align-items: center;
        justify-content: space-between;

        .item-name {
          font-size: 14px;
          font-weight: bold;
          color: #323233;
        }

        .item-status {
          font-size: 12px;
          font-weight: bold;
        }
      }

      .item-body {
        margin-top: 6px;

        .item-line {
          display: flex;
          justify-content: space-between;
          font-size: 12px;
          padding: 3px 0;

          .label {
            color: #969799;
          }

          .value {
            color: #323233;
            text-align: right;
            word-break: break-all;
          }
        }
      }
    }
  }
</style>
