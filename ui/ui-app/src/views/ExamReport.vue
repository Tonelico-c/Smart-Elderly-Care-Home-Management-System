<script setup>
  import {nextTick, onMounted, ref, computed} from 'vue'
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

  //AI健康分析：弹层内以对话形式展示，分析完毕后可继续追问
  const analysisShow = ref(false)
  //首次分析进行中（追问期间为 false，可正常输入）
  const analysisLoading = ref(false)
  //弹层内的消息列表：role为assistant(小智)/user(老人)，分析结果是第一条assistant消息
  const messages = ref([])
  const input = ref('')
  const sending = ref(false)
  const listRef = ref(null)

  //分析完毕后的常见追问
  const quickQuestions = [
    '异常指标平时要注意什么？',
    '饮食上有什么建议？',
    '需要去医院复查吗？'
  ]

  //滚动到消息列表底部
  const scrollToBottom = () => {
    nextTick(() => {
      const el = listRef.value
      if (el) {
        el.scrollTop = el.scrollHeight
      }
    })
  }

  //打开弹层：已有分析内容时直接查看，否则开始新的分析
  const openAnalysis = () => {
    analysisShow.value = true
    if (!messages.value.length) {
      startAnalysis()
    }
  }

  //发起AI健康分析，流式内容逐段追加到第一条assistant消息上
  const startAnalysis = () => {
    if (analysisLoading.value) {
      return
    }
    analysisLoading.value = true
    //注意：必须取数组中的响应式代理对象，直接用原始对象修改不会触发视图更新
    messages.value = [{role: 'assistant', content: ''}]
    const reply = messages.value[0]
    scrollToBottom()
    chatApi.reportAnalysisStream(String(route.params.id), {
      onMessage: chunk => {
        reply.content += chunk
        scrollToBottom()
      },
      onDone: () => {
        if (!reply.content) {
          reply.content = '抱歉，小智暂时无法分析，请稍后再试。'
        }
        finishAnalysis()
      },
      onError: () => {
        if (!reply.content) {
          reply.content = '分析获取失败，请稍后再试。'
        }
        finishAnalysis()
      }
    })
  }

  const finishAnalysis = () => {
    analysisLoading.value = false
    scrollToBottom()
  }

  //分析完毕后继续追问：复用普通流式对话，后端会话记忆中已包含本次分析内容
  const send = (text) => {
    const message = (text ?? input.value).trim()
    if (!message || analysisLoading.value || sending.value) {
      return
    }
    input.value = ''
    messages.value.push({role: 'user', content: message})
    messages.value.push({role: 'assistant', content: ''})
    //取数组中的响应式代理对象，流式内容逐段追加到这条消息上
    const reply = messages.value[messages.value.length - 1]
    scrollToBottom()

    sending.value = true
    chatApi.chatStream(message, {
      onMessage: chunk => {
        reply.content += chunk
        scrollToBottom()
      },
      onDone: () => {
        //流异常中断且没有内容时给出兜底提示
        if (!reply.content) {
          reply.content = '抱歉，小智暂时无法回复，请稍后再试。'
        }
        finishSend()
      },
      onError: () => {
        if (!reply.content) {
          reply.content = '网络异常，请稍后再试。'
        }
        finishSend()
      }
    })
  }

  const finishSend = () => {
    sending.value = false
    scrollToBottom()
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
                    icon="chat-o" :loading="analysisLoading" @click="openAnalysis">AI健康分析</van-button>
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

    <!--AI健康分析弹层：流式展示分析内容，分析完毕后可继续追问-->
    <van-popup v-model:show="analysisShow" position="bottom" round>
      <div class="analysis-popup">
        <div class="analysis-title">AI 健康分析</div>
        <!--消息列表：第一条为分析结果，其后为追问对话-->
        <div class="analysis-body" ref="listRef">
          <div v-for="(msg, index) in messages" :key="index" class="msg-item" :class="msg.role">
            <div class="msg-bubble">
              <template v-if="msg.content">{{ msg.content }}</template>
              <van-loading v-else-if="analysisLoading" size="16">小智正在分析报告...</van-loading>
              <van-loading v-else-if="sending" size="16">小智正在思考...</van-loading>
            </div>
          </div>
          <!--常见追问（仅分析完毕且尚未提问时展示）-->
          <div v-if="!analysisLoading && messages.length <= 1" class="quick-box">
            <div v-for="q in quickQuestions" :key="q" class="quick-item" @click="send(q)">{{ q }}</div>
          </div>
        </div>
        <div class="analysis-footer">
          <span>内容由AI生成，仅供参考，不构成医疗建议</span>
          <van-button type="primary" size="small" round plain :loading="analysisLoading"
                      @click="startAnalysis">重新分析</van-button>
        </div>
        <!--分析完毕后可继续追问-->
        <div v-if="!analysisLoading" class="analysis-input-bar">
          <van-field v-model="input" class="analysis-field" placeholder="针对本次分析继续提问"
                     :disabled="sending" maxlength="200" @keyup.enter="send()"/>
          <van-button type="primary" round size="small" class="send-btn" :loading="sending"
                      @click="send()">发送</van-button>
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
    max-height: 75vh;
    padding: 20px 16px calc(16px + env(safe-area-inset-bottom));

    .analysis-title {
      text-align: center;
      font-size: 16px;
      font-weight: bold;
      color: #323233;
      margin-bottom: 12px;
    }

    //消息列表
    .analysis-body {
      flex: 1;
      min-height: 140px;
      overflow-y: auto;

      .msg-item {
        display: flex;
        margin-bottom: 10px;

        .msg-bubble {
          max-width: 86%;
          padding: 10px 12px;
          border-radius: 12px;
          font-size: 14px;
          line-height: 1.7;
          word-break: break-all;
          //保留AI输出中的换行
          white-space: pre-wrap;
        }

        //小智：靠左
        &.assistant {
          justify-content: flex-start;

          .msg-bubble {
            background-color: #f5f6f8;
            color: #323233;
            border-top-left-radius: 4px;
          }
        }

        //老人：靠右
        &.user {
          justify-content: flex-end;

          .msg-bubble {
            background: linear-gradient(135deg, #1989fa 0%, #3f9cf9 100%);
            color: #fff;
            border-top-right-radius: 4px;
          }
        }
      }
    }

    //常见追问
    .quick-box {
      .quick-item {
        display: inline-block;
        padding: 6px 12px;
        margin: 0 8px 8px 0;
        border-radius: 14px;
        background-color: #fff;
        border: 1px solid #dcdee0;
        font-size: 13px;
        color: #1989fa;

        &:active {
          background-color: #f2f3f5;
        }
      }
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

    //追问输入栏
    .analysis-input-bar {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: 10px;

      .analysis-field {
        flex: 1;
        border-radius: 18px;
        background-color: #f5f6f8;
        overflow: hidden;

        :deep(.van-field__control) {
          font-size: 14px;
        }
      }

      .send-btn {
        flex-shrink: 0;
        padding: 0 18px;
      }
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
