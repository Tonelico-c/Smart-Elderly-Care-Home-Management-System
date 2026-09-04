<script setup>
  import {onMounted, ref} from 'vue'
  import {showToast} from 'vant'
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

      .apply-time {
        font-size: 11px;
        color: #969799;
      }
    }
  }
</style>
