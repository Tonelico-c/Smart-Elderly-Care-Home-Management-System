<script setup>
  import {computed, onMounted, ref} from 'vue'
  import {showConfirmDialog, showLoadingToast, showToast} from 'vant'
  import {useRouter} from 'vue-router'
  import leaveApi from '@/api/leave.js'

  const router = useRouter()

  //加载状态
  const loading = ref(true)
  //请假记录列表
  const list = ref([])

  const loadList = () => {
    loading.value = true
    leaveApi.list().then(result => {
      if (result.code === 1) {
        list.value = result.data || []
      } else {
        showToast(result.msg)
      }
    }).finally(() => {
      loading.value = false
    })
  }

  onMounted(() => {
    loadList()
  })

  //状态（与后端一致：0待审批 1请假中 2已销假 3已驳回）
  const statusOptions = [
    {value: 0, label: '待审批', color: '#1989fa'},
    {value: 1, label: '请假中', color: '#ff976a'},
    {value: 2, label: '已销假', color: '#07c160'},
    {value: 3, label: '已驳回', color: '#969799'}
  ]
  const statusText = (status) => {
    const statusObj = statusOptions.find(item => item.value === status)
    return statusObj ? statusObj.label : '未知'
  }
  const statusColor = (status) => {
    const statusObj = statusOptions.find(item => item.value === status)
    return statusObj ? statusObj.color : '#969799'
  }

  const formatTime = (time) => {
    return time || '-'
  }

  //销假弹层：默认实际返回时间为当前时间，可在弹层内改选（单弹层内先选日期再选时间，避免弹层开关冲突）
  const checkoutShow = ref(false)
  const currentLeave = ref(null)
  const actualReturnTime = ref('')
  const pickerStep = ref('form') // 'form' | 'date' | 'time'
  const submitting = ref(false)
  let pickedDate = ''

  //当前时间字符串 yyyy-MM-dd HH:mm:ss
  const nowString = () => {
    const d = new Date()
    const p = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
  }

  //可选日期范围：外出当天起至当前时间（返回时间不能晚于现在）
  const pickerMinDate = computed(() => {
    const t = currentLeave.value?.beginTime
    return t ? new Date(String(t).replace(' ', 'T')) : new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
  })
  const pickerMaxDate = computed(() => new Date())

  const openCheckout = (leave) => {
    currentLeave.value = leave
    //默认实际返回时间为当前时间，不修改直接确认即按当前时间销假
    actualReturnTime.value = nowString()
    pickerStep.value = 'form'
    checkoutShow.value = true
  }

  const onDateConfirm = ({selectedValues}) => {
    pickedDate = selectedValues.join('-')
    //同一弹层内切换到时间选择，避免弹层开关冲突
    pickerStep.value = 'time'
  }

  const onTimeConfirm = ({selectedValues}) => {
    //columns-type只有时/分两列，补上秒拼成yyyy-MM-dd HH:mm:ss（与后端解析格式一致）
    actualReturnTime.value = `${pickedDate} ${selectedValues.slice(0, 2).join(':')}:00`
    pickerStep.value = 'form'
  }

  const confirmCheckout = () => {
    //校验返回时间不能晚于当前时间
    if (new Date(actualReturnTime.value.replace(' ', 'T')) > new Date()) {
      showToast('实际返回时间不能晚于当前时间')
      return
    }
    submitting.value = true
    leaveApi.checkout(currentLeave.value.id, actualReturnTime.value).then(result => {
      if (result.code === 1) {
        showToast('销假成功')
        checkoutShow.value = false
        loadList()
      } else {
        showToast(result.msg)
      }
    }).finally(() => {
      submitting.value = false
    })
  }

  //取消待审批的请假申请（后端会删除该记录）
  const cancelLeave = (leave) => {
    showConfirmDialog({
      title: '取消申请',
      message: `确定取消【${leave.reason || '请假申请'}】的请假申请吗？取消后记录将被删除。`
    }).then(() => {
      const toast = showLoadingToast({message: '取消中...', forbidClick: true, duration: 0})
      leaveApi.cancel(leave.id).then(result => {
        if (result.code === 1) {
          showToast('已取消')
          loadList()
        } else {
          showToast(result.msg)
        }
      }).finally(() => {
        toast.close()
      })
    }).catch(() => {})
  }
</script>

<template>
  <div class="leave-page">
    <van-nav-bar title="我的请假" fixed placeholder>
      <template #right>
        <span class="nav-apply" @click="router.push('/leave-form')">我要请假</span>
      </template>
    </van-nav-bar>

    <van-loading v-if="loading" class="page-loading" size="24" vertical>加载中...</van-loading>

    <van-pull-refresh v-else :model-value="false" @refresh="loadList">
      <!--请假记录列表-->
      <div v-if="list.length">
        <div v-for="leave in list" :key="leave.id" class="leave-card">
          <div class="card-top">
            <div class="reason">{{ leave.reason || '请假申请' }}</div>
            <div class="status" :style="{color: statusColor(leave.status)}">{{ statusText(leave.status) }}</div>
          </div>
          <div class="card-info">
            <div class="info-row">
              <van-icon name="location-o"/>
              <span>去向：{{ leave.destination || '-' }}</span>
            </div>
            <div class="info-row">
              <van-icon name="phone-o"/>
              <span>联系电话：{{ leave.contactPhone || '-' }}</span>
            </div>
            <div class="info-row">
              <van-icon name="clock-o"/>
              <span>外出时间：{{ formatTime(leave.beginTime) }}</span>
            </div>
            <div class="info-row">
              <van-icon name="underway-o"/>
              <span>预计返回：{{ formatTime(leave.endTime) }}</span>
            </div>
            <!--销假后显示实际返回时间-->
            <div v-if="leave.status === 2" class="info-row">
              <van-icon name="passed"/>
              <span>实际返回：{{ formatTime(leave.actualReturnTime) }}</span>
            </div>
            <!--驳回时显示驳回理由-->
            <div v-if="leave.status === 3 && leave.rejectReason" class="info-row reject">
              <van-icon name="warning-o"/>
              <span>驳回理由：{{ leave.rejectReason }}</span>
            </div>
            <!--审批后显示审批人-->
            <div v-if="leave.approverName" class="info-row">
              <van-icon name="manager-o"/>
              <span>审批人：{{ leave.approverName }}</span>
            </div>
          </div>
          <div class="card-footer">
            <span class="apply-time">申请时间：{{ formatTime(leave.createTime) }}</span>
            <!--待审批的记录显示取消按钮-->
            <van-button v-if="leave.status === 0" size="small" plain round type="danger" @click="cancelLeave(leave)">
              取消申请
            </van-button>
            <!--请假中的记录显示销假按钮-->
            <van-button v-if="leave.status === 1" size="small" plain round type="success" @click="openCheckout(leave)">
              销假
            </van-button>
          </div>
        </div>
      </div>

      <!--空状态-->
      <van-empty description="暂无请假记录">
        <van-button round type="primary" size="small" style="width: 120px" @click="router.push('/leave-form')">
          我要请假
        </van-button>
      </van-empty>
    </van-pull-refresh>

    <!--销假弹层：单弹层内切换 表单/日期/时间，避免弹层开关冲突-->
    <van-popup v-model:show="checkoutShow" position="bottom" round>
      <div v-if="pickerStep === 'form'" class="checkout-panel">
        <div class="checkout-title">销假</div>
        <div class="checkout-reason">{{ currentLeave?.reason || '请假申请' }}</div>
        <van-cell-group inset>
          <van-field label="外出时间" :model-value="formatTime(currentLeave?.beginTime)" readonly/>
          <van-field label="预计返回" :model-value="formatTime(currentLeave?.endTime)" readonly/>
          <van-field
              v-model="actualReturnTime"
              label="实际返回"
              placeholder="默认当前时间"
              readonly
              is-link
              @click="pickerStep = 'date'"
          />
        </van-cell-group>
        <div class="checkout-tip">不修改实际返回时间则默认按当前时间销假</div>
        <div class="checkout-actions">
          <van-button block round plain @click="checkoutShow = false">取消</van-button>
          <van-button block round type="success" :loading="submitting" @click="confirmCheckout">确认销假</van-button>
        </div>
      </div>
      <van-date-picker
          v-else-if="pickerStep === 'date'"
          title="选择返回日期"
          :min-date="pickerMinDate"
          :max-date="pickerMaxDate"
          @confirm="onDateConfirm"
          @cancel="pickerStep = 'form'"
      />
      <van-time-picker
          v-else
          title="选择返回时间"
          :columns-type="['hour', 'minute']"
          @confirm="onTimeConfirm"
          @cancel="pickerStep = 'form'"
      />
    </van-popup>
  </div>
</template>

<style scoped lang="scss">
  .leave-page {
    min-height: 100vh;
    padding-bottom: 20px;
    background-color: #f5f6f8;
  }

  .nav-apply {
    color: #1989fa;
    font-size: 13px;
  }

  .page-loading {
    padding: 60px 0;
  }

  .leave-card {
    margin: 12px 16px 0;
    padding: 12px 14px;
    border-radius: 12px;
    background-color: #fff;

    .card-top {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .reason {
        font-size: 15px;
        font-weight: bold;
        color: #323233;
      }

      .status {
        font-size: 12px;
        font-weight: bold;
      }
    }

    .card-info {
      margin-top: 10px;
      display: flex;
      flex-direction: column;
      gap: 6px;

      .info-row {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 12px;
        color: #646566;

        .van-icon {
          color: #969799;
        }

        //驳回理由红字提示
        &.reject {
          color: #ee0a24;
        }
      }
    }

    .card-footer {
      margin-top: 10px;
      padding-top: 10px;
      border-top: 1px solid #f2f3f5;
      display: flex;
      align-items: center;
      justify-content: space-between;

      .apply-time {
        font-size: 11px;
        color: #969799;
      }
    }
  }

  //销假弹层
  .checkout-panel {
    padding: 20px 0 16px;

    .checkout-title {
      text-align: center;
      font-size: 16px;
      font-weight: bold;
      color: #323233;
    }

    .checkout-reason {
      margin: 8px 0 12px;
      text-align: center;
      font-size: 13px;
      color: #969799;
    }

    .checkout-tip {
      margin: 10px 24px 0;
      font-size: 11px;
      color: #969799;
    }

    .checkout-actions {
      display: flex;
      gap: 12px;
      padding: 16px 16px 0;
    }
  }
</style>
