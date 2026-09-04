<script setup>
  import {ref} from 'vue'
  import {useRouter} from 'vue-router'
  import {showToast} from 'vant'
  import leaveApi from '@/api/leave.js'
  import {elderElderInfoStore} from '@/store/elderInfo.js'

  const router = useRouter()
  const elderInfoStore = elderElderInfoStore()

  //请假表单
  const leave = ref({
    reason: '',
    destination: '',
    phone: elderInfoStore.elder.phone || '',
    beginTime: '',
    endTime: ''
  })

  //时间选择弹层：单个弹层内先选日期再选时间（两步），pickerTarget记录当前在选"外出"还是"返回"时间
  //注意不能用两个弹层串联（一关一开会导致遮罩冲突、点击无响应）
  const pickerShow = ref(false)
  const pickerStep = ref('date') // 'date' | 'time'
  const pickerTarget = ref('begin')
  const minDate = new Date()
  const maxDate = new Date(Date.now() + 90 * 24 * 60 * 60 * 1000)
  let pickedDate = ''

  const openPicker = (target) => {
    pickerTarget.value = target
    pickerStep.value = 'date'
    pickerShow.value = true
  }

  const onDateConfirm = ({selectedValues}) => {
    pickedDate = selectedValues.join('-')
    //同一弹层内切换到时间选择，避免弹层开关冲突
    pickerStep.value = 'time'
  }

  const onTimeConfirm = ({selectedValues}) => {
    //columns-type只有时/分两列，补上秒拼成yyyy-MM-dd HH:mm:ss（与后端jackson日期格式一致）
    const time = selectedValues.slice(0, 2).join(':')
    leave.value[pickerTarget.value] = `${pickedDate} ${time}:00`
    pickerShow.value = false
  }

  //提交请假申请
  const submitting = ref(false)
  const submit = () => {
    if (!leave.value.reason) {
      showToast('请输入请假原因')
      return
    }
    if (!leave.value.destination) {
      showToast('请输入外出去向')
      return
    }
    /*if (!/^1\d{10}$/.test(leave.value.phone)) {
      showToast('请输入正确的手机号')
      return
    }*/
    if (!leave.value.beginTime) {
      showToast('请选择预计外出时间')
      return
    }
    if (!leave.value.endTime) {
      showToast('请选择预计返回时间')
      return
    }
    if (leave.value.endTime <= leave.value.beginTime) {
      showToast('预计返回时间必须晚于外出时间')
      return
    }
    submitting.value = true
    leaveApi.add({
      reason: leave.value.reason,
      destination: leave.value.destination,
      phone: leave.value.phone,
      beginTime: leave.value.beginTime,
      endTime: leave.value.endTime
    }).then(result => {
      if (result.code === 1) {
        showToast('申请已提交，等待审批')
        router.replace('/leave')
      } else {
        showToast(result.msg)
      }
    }).finally(() => {
      submitting.value = false
    })
  }
</script>

<template>
  <div class="form-page">
    <van-nav-bar title="我要请假" left-arrow fixed placeholder @click-left="router.back()"/>

    <!--请假表单-->
    <van-cell-group inset class="form-card">
      <van-field
          v-model="leave.reason"
          label="请假原因"
          placeholder="请输入请假原因"
          left-icon="edit"
          maxlength="100"
      />
      <van-field
          v-model="leave.destination"
          label="外出去向"
          placeholder="请输入外出去向"
          left-icon="location-o"
          maxlength="50"
      />
      <van-field
          v-model="leave.phone"
          type="tel"
          label="联系电话"
          placeholder="请输入外出期间联系电话"
          left-icon="phone-o"
      />
      <van-field
          v-model="leave.beginTime"
          label="预计外出时间"
          placeholder="请选择外出时间"
          left-icon="clock-o"
          readonly
          is-link
          @click="openPicker('beginTime')"
      />
      <van-field
          v-model="leave.endTime"
          label="预计返回时间"
          placeholder="请选择返回时间"
          left-icon="underway-o"
          readonly
          is-link
          @click="openPicker('endTime')"
      />
    </van-cell-group>

    <!--说明-->
    <div class="tips">
      <div class="tips-title">温馨提示</div>
      <div class="tips-content">
        1. 请假申请提交后需等待工作人员审批<br>
        2. 审批通过后外出期间请保持电话畅通<br>
        3. 返回后请联系工作人员办理销假<br>
        4. 请在"我的请假"中查看审批进度
      </div>
    </div>

    <!--提交栏-->
    <van-action-bar>
      <van-action-bar-button type="primary" text="提交申请" :loading="submitting" @click="submit"/>
    </van-action-bar>

    <!--时间选择弹层：单弹层内先选日期再选时间-->
    <van-popup v-model:show="pickerShow" position="bottom" round>
      <van-date-picker
          v-if="pickerStep === 'date'"
          title="选择日期"
          :min-date="minDate"
          :max-date="maxDate"
          @confirm="onDateConfirm"
          @cancel="pickerShow = false"
      />
      <van-time-picker
          v-else
          title="选择时间"
          :columns-type="['hour', 'minute']"
          @confirm="onTimeConfirm"
          @cancel="pickerShow = false"
      />
    </van-popup>
  </div>
</template>

<style scoped lang="scss">
  .form-page {
    padding-bottom: 90px;
  }

  .form-card {
    margin-top: 12px;
  }

  .tips {
    margin: 16px 16px 0;
    padding: 12px 14px;
    border-radius: 12px;
    background-color: #f0f7ff;

    .tips-title {
      font-size: 13px;
      font-weight: bold;
      color: #1989fa;
      margin-bottom: 6px;
    }

    .tips-content {
      font-size: 11px;
      color: #969799;
      line-height: 1.8;
    }
  }
</style>
