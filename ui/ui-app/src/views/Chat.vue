<script setup>
  import {nextTick, onMounted, ref} from 'vue'
  import {useRouter} from 'vue-router'
  import {showToast} from 'vant'
  import chatApi from '@/api/chat.js'
  import {elderElderInfoStore} from '@/store/elderInfo.js'

  const router = useRouter()
  const elderInfoStore = elderElderInfoStore()

  //消息列表：role为user(老人提问)/assistant(小智回复)
  const messages = ref([])
  const input = ref('')
  const sending = ref(false)
  const listRef = ref(null)

  onMounted(() => {
    //欢迎语 + 常见问题快捷入口
    messages.value.push({
      role: 'assistant',
      content: '您好，我是小智，您的智能健康咨询助手～可以问我体检、护理、饮食起居等养老相关问题哦。'
    })
  })

  //常见问题
  const quickQuestions = [
    '高血压老人饮食要注意什么？',
    '糖尿病患者能吃水果吗？',
    '每天什么时间锻炼身体比较好？',
    '体检前需要做哪些准备？'
  ]

  //滚动到底部
  const scrollToBottom = () => {
    nextTick(() => {
      const el = listRef.value
      if (el) {
        el.scrollTop = el.scrollHeight
      }
    })
  }

  //发送消息
  const send = (text) => {
    const message = (text ?? input.value).trim()
    if (!message) {
      showToast('请输入您想咨询的问题')
      return
    }
    if (sending.value) {
      return
    }
    input.value = ''
    messages.value.push({role: 'user', content: message})
    scrollToBottom()

    //占位的助手消息，流式回复逐段追加到这条消息上
    //注意：必须取数组中的响应式代理对象，直接用原始对象修改不会触发视图更新，
    //会导致流式内容攒到最后一次性显示
    messages.value.push({role: 'assistant', content: ''})
    const reply = messages.value[messages.value.length - 1]

    sending.value = true
    chatApi.chatStream(message, {
      onMessage: (chunk) => {
        reply.content += chunk
        scrollToBottom()
      },
      onDone: () => {
        //流异常中断且没有内容时给出兜底提示
        if (!reply.content) {
          reply.content = '抱歉，小智暂时无法回复，请稍后再试。'
        }
        finish()
      },
      onError: () => {
        if (!reply.content) {
          reply.content = '网络异常，请稍后再试。'
        }
        finish()
      }
    })
  }

  const finish = () => {
    sending.value = false
    scrollToBottom()
  }
</script>

<template>
  <div class="chat-page">
    <van-nav-bar
        title="智能咨询"
        left-arrow
        fixed
        placeholder
        @click-left="router.back()"
    />

    <!--消息列表-->
    <div class="chat-list" ref="listRef">
      <div v-for="(msg, index) in messages" :key="index" class="chat-item" :class="msg.role">
        <!--头像-->
        <div class="avatar" :class="msg.role">
          <span v-if="msg.role === 'assistant'">智</span>
          <span v-else>{{ (elderInfoStore.elder.name || '我').slice(0, 1) }}</span>
        </div>
        <!--气泡-->
        <div class="bubble">{{ msg.content }}</div>
      </div>

      <!--常见问题（仅第一轮提问前展示）-->
      <div v-if="messages.length <= 1" class="quick-box">
        <div class="quick-title">大家都在问</div>
        <div
            v-for="q in quickQuestions"
            :key="q"
            class="quick-item"
            @click="send(q)"
        >
          {{ q }}
        </div>
      </div>
    </div>

    <!--底部输入栏-->
    <div class="chat-input-bar">
      <van-field
          v-model="input"
          class="chat-field"
          placeholder="请输入您想咨询的问题"
          :disabled="sending"
          maxlength="200"
          @keyup.enter="send()"
      />
      <van-button type="primary" round class="send-btn" :loading="sending" @click="send()">
        发送
      </van-button>
    </div>
  </div>
</template>

<style scoped lang="scss">
  .chat-page {
    display: flex;
    flex-direction: column;
    height: 100vh;
    background-color: #f5f6f8;
  }

  .chat-list {
    flex: 1;
    overflow-y: auto;
    padding: 16px 12px;
  }

  .chat-item {
    display: flex;
    margin-bottom: 16px;

    .avatar {
      flex-shrink: 0;
      width: 36px;
      height: 36px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 14px;

      //小智头像
      &.assistant {
        background: linear-gradient(135deg, #1989fa 0%, #5cadff 100%);
      }

      //老人头像
      &.user {
        background: linear-gradient(135deg, #07c160 0%, #42d392 100%);
      }
    }

    .bubble {
      max-width: 72%;
      margin: 0 8px;
      padding: 10px 12px;
      border-radius: 12px;
      font-size: 14px;
      line-height: 1.6;
      word-break: break-all;
      white-space: pre-wrap;
    }

    //小智：靠左
    &.assistant {
      justify-content: flex-start;

      .bubble {
        background-color: #fff;
        color: #323233;
        border-top-left-radius: 4px;
      }
    }

    //老人：靠右
    &.user {
      justify-content: flex-end;

      .bubble {
        background: linear-gradient(135deg, #1989fa 0%, #3f9cf9 100%);
        color: #fff;
        border-top-right-radius: 4px;
      }
    }
  }

  //常见问题
  .quick-box {
    margin: 8px 44px 0;

    .quick-title {
      font-size: 12px;
      color: #969799;
      margin-bottom: 8px;
    }

    .quick-item {
      padding: 8px 12px;
      margin-bottom: 8px;
      border-radius: 8px;
      background-color: #fff;
      font-size: 13px;
      color: #323233;

      &:active {
        background-color: #f2f3f5;
      }
    }
  }

  //底部输入栏
  .chat-input-bar {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px calc(8px + env(safe-area-inset-bottom));
    background-color: #fff;
    box-shadow: 0 -1px 6px rgba(0, 0, 0, 0.04);

    .chat-field {
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
</style>
