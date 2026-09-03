<script setup>
  import {computed, onMounted, ref} from 'vue'
  import {showConfirmDialog, showLoadingToast, showToast} from 'vant'
  import {useRouter} from 'vue-router'
  import appointmentApi from '@/api/appointment.js'
  import {useAppointmentStore} from '@/store/appointment.js'
  const router = useRouter()
  const appointmentStore = useAppointmentStore();

  //加载状态
  const loading = ref(true)

  //拉取后端预约列表
  const loadList = () => {
    loading.value = true
    appointmentApi.list().then(result => {
      if (result.code === 1) {
        appointmentStore.setAppointmentList(result.data)
      }
    }).finally(() => {
      loading.value = false
    })
  }

  onMounted(() => {
    //列表数据变化过（提交/取消预约）或第一次进入时拉取
    if (appointmentStore.dirty) {
      loadList()
    } else {
      loading.value = false
    }
  })

  //状态筛选Tab：全部/待体检/已完成/已取消
  const activeTab = ref(0)
  const statusTabs = [
    {title: '全部'},
    {title: '待体检', status: 0},
    {title: '已完成', status: 2},
    {title: '已取消', status: 3}
  ]

  const filteredList = computed(() => {
    const status = statusTabs[activeTab.value].status
    if (status === undefined) {
      return appointmentStore.appointmentList
    }
    return appointmentStore.appointmentList.filter(item => item.status === status)
  })

  //状态（与后端一致：0待体检 1体检中 2已完成 3已取消 4已过期）
  const statusOptions = [
    {value: 0, label: '待体检', color: '#1989fa'},
    {value: 1, label: '体检中', color: '#ff976a'},
    {value: 2, label: '已完成', color: '#07c160'},
    {value: 3, label: '已取消', color: '#969799'},
    {value: 4, label: '已过期', color: '#969799'}
  ]

  const statusText = (status) => {
    const statusObj = statusOptions.find(item => item.value === status)
    return statusObj ? statusObj.label : '未知'
  }

  const statusColor = (status) => {
    const statusObj = statusOptions.find(item => item.value === status)
    return statusObj ? statusObj.color : '#969799'
  }

  //取消预约
  const cancelAppointment = (appointment) => {
    showConfirmDialog({
      title: '取消预约',
      message: `确定要取消【${appointment.packageName}】的预约吗？`
    }).then(() => {
      const toast = showLoadingToast({message: '取消中...', forbidClick: true, duration: 0})
      appointmentApi.cancel(appointment.id).then(result => {
        if (result.code == 1) {
          showToast('已取消')
          //标记数据已变化，重新拉取列表
          appointmentStore.markDirty()
          loadList()
        } else {
          showToast(result.msg)
        }
      }).finally(() => {
        toast.close()
      })
    }).catch(() => {})
  }

  //体检报告弹层
  const reportShow = ref(false)
  const reportLoading = ref(false)
  //当前查看报告的预约
  const reportAppointment = ref({})
  //报告的体检项目明细列表
  const reportItems = ref([])

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
      return item.resultValue != null ? `${item.resultValue}` : '-'
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

  //查看报告
  const showReport = (appointment) => {
    reportAppointment.value = appointment
    reportShow.value = true
    reportLoading.value = true
    reportItems.value = []
    appointmentApi.listItems(appointment.id).then(result => {
      if (result.code == 1) {
        reportItems.value = result.data || []
      } else {
        showToast(result.msg)
      }
    }).finally(() => {
      reportLoading.value = false
    })
  }
</script>

<template>
  <div class="appointment-page">
    <van-nav-bar title="我的预约" fixed placeholder/>

    <van-tabs v-model:active="activeTab" sticky offset-top="46px">
      <van-tab v-for="tab in statusTabs" :key="tab.title" :title="tab.title"/>
    </van-tabs>

    <van-loading v-if="loading" class="page-loading" size="24" vertical>加载中...</van-loading>

    <van-pull-refresh v-else :model-value="false" @refresh="loadList">
    <!--预约列表-->
    <div v-if="filteredList.length">
      <div v-for="appointment in filteredList" :key="appointment.id" class="appointment-card">
        <div class="card-top" @click="router.push('/package/' + appointment.packageId)">
          <div class="package-name">{{ appointment.packageName }}</div>
          <div class="status" :style="{color: statusColor(appointment.status)}">{{ statusText(appointment.status) }}</div>
        </div>
        <div class="card-info">
          <div class="info-row">
            <van-icon name="friends-o"/>
            <span>体检人：{{ appointment.elderName }}</span>
          </div>
          <div class="info-row">
            <van-icon name="calendar-o"/>
            <span>{{ appointment.appointmentDate }} {{ appointment.appointmentTime }}</span>
          </div>
          <div class="info-row">
            <van-icon name="apps-o"/>
            <span>共{{ appointment.examItemCount }}项检查</span>
          </div>
        </div>
        <div class="card-footer">
          <span class="price">¥{{ appointment.price }}</span>
          <van-button v-if="appointment.status === 0" size="small" plain round type="danger" @click="cancelAppointment(appointment)">
            取消预约
          </van-button>
          <van-button v-else-if="appointment.status === 2" size="small" plain round type="primary" @click="showReport(appointment)">
            查看报告
          </van-button>
        </div>
      </div>
    </div>

    <!--空状态-->
    <van-empty v-else description="暂无预约，去挑选一个体检套餐吧">
      <van-button round type="primary" size="small" style="width: 120px" @click="router.push('/package')">
        去预约
      </van-button>
    </van-empty>
    </van-pull-refresh>

    <!--体检报告弹层-->
    <van-popup v-model:show="reportShow" position="bottom" round class="report-popup">
      <!--报告头部-->
      <div class="report-header">
        <div class="report-title">体检报告</div>
        <van-icon name="cross" size="18" color="#969799" @click="reportShow = false"/>
      </div>

      <van-loading v-if="reportLoading" class="report-loading" size="24" vertical>报告加载中...</van-loading>

      <template v-else>
        <!--预约信息-->
        <div class="report-info">
          <div class="info-line">
            <span class="label">体检套餐</span>
            <span class="value">{{ reportAppointment.packageName }}</span>
          </div>
          <div class="info-line">
            <span class="label">体检人</span>
            <span class="value">{{ reportAppointment.elderName }}</span>
          </div>
          <div class="info-line">
            <span class="label">体检日期</span>
            <span class="value">{{ reportAppointment.appointmentDate }} {{ reportAppointment.appointmentTime }}</span>
          </div>
          <!--异常项汇总-->
          <div class="summary" :class="abnormalCount > 0 ? 'warn' : 'ok'">
            <template v-if="reportItems.length">
              共 {{ reportItems.length }} 项检查，
              <template v-if="abnormalCount > 0">{{ abnormalCount }} 项指标异常，请及时咨询医生</template>
              <template v-else>各项指标均正常</template>
            </template>
          </div>
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
    </van-popup>
  </div>
</template>

<style scoped lang="scss">
  .appointment-page {
    padding-bottom: 20px;
  }

  .page-loading {
    padding: 60px 0;
  }

  .appointment-card {
    margin: 12px 16px 0;
    padding: 12px 14px;
    border-radius: 12px;
    background-color: #fff;

    .card-top {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .package-name {
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
      }
    }

    .card-footer {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-top: 10px;
      padding-top: 10px;
      border-top: 1px solid #f2f3f5;

      .price {
        font-size: 16px;
        font-weight: bold;
        color: #ee0a24;
      }
    }
  }

  //体检报告弹层
  .report-popup {
    max-height: 75vh;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .report-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px 16px 8px;

      .report-title {
        font-size: 16px;
        font-weight: bold;
        color: #323233;
      }
    }

    .report-loading {
      padding: 40px 0;
    }

    //预约信息 + 汇总
    .report-info {
      padding: 8px 16px 0;

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
    }

    //项目明细列表
    .report-list {
      flex: 1;
      overflow-y: auto;
      padding: 12px 16px 20px;

      .report-item {
        padding: 10px 12px;
        margin-bottom: 10px;
        border-radius: 10px;
        background-color: #f7f8fa;

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
  }
</style>
