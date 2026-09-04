<script setup>

  import {ref} from "vue";
  import checkInApi from "@/api/checkin.js";
  import buildingApi from "@/api/building.js";
  import {Plus, Search, Refresh} from "@element-plus/icons-vue";
  import {ElMessage, ElMessageBox} from "element-plus";

  // ==================== 列表查询 ====================
  const list = ref([])
  const total = ref(0)

  const checkInRecordQuery = ref({
    elderName: '',
    buildingId: '',
    status: '',
    page: 1,
    limit: 10
  })

  const loadData = () => {
    checkInApi.list(checkInRecordQuery.value).then(result => {
      list.value = result.data.records
      total.value = result.data.total
    })
  }
  loadData()

  // 下拉框数据：楼栋、护理等级
  const buildingList = ref([])
  const loadOptions = () => {
    buildingApi.list({page: 1, limit: 1000}).then(result => {
      buildingList.value = result.data.records
    })
  }
  loadOptions()

  const onSearch = () => {
    checkInRecordQuery.value.page = 1
    loadData()
  }
  const resetSearch = () => {
    checkInRecordQuery.value = {
      elderName: '',
      buildingId: '',
      status: '',
      page: 1,
      limit: 10
    }
    loadData()
  }

  // 状态选项（与后端 check_in_record.status 对应）
  const statusOptions = [
    {value: 1, label: '入住中'},
    {value: 0, label: '已退住'},
    {value: 2, label: '请假中'},
  ]
  const statusName = (status) => statusOptions.find(item => item.value === status)?.label
  const statusTagType = (status) => {
    if (status === 1) return 'success'
    if (status === 2) return 'warning'
    return 'info'
  }

  // ==================== 办理入住（三步向导） ====================
  const addDialogVisible = ref(false)
  const activeStep = ref(0)

  // 待提交的入住信息
  const checkInRecord = ref({})

  const showAddDialog = () => {
    activeStep.value = 0
    checkInRecord.value = {
      elderId: '',
      bedId: '',
      checkInTime: ''
    }
    addDialogVisible.value = true
  }

  const nextStep = () => {
    // 第一步必须选择老人，第二步必须选择床位
    if (activeStep.value === 0 && !checkInRecord.value.elderId) {
      ElMessage.warning('请先选择一位老人')
      return
    }
    if (activeStep.value === 1 && !checkInRecord.value.bedId) {
      ElMessage.warning('请先选择一个床位')
      return
    }
    activeStep.value++
  }
  const prevStep = () => {
    activeStep.value--
  }

  // -------- 第一步：选择老人 --------
  const selectedElder = ref(null)
  // 可入住老人列表
  const availableElderList = ref([])
  const loadAvailableElders = () => {
    checkInApi.listAvailableElders().then(result => {
      availableElderList.value = result.data
    })
  }

  // 单选老人：点击行选中
  const handleElderChange = (row) => {
    selectedElder.value = row
    checkInRecord.value.elderId = row?.id
  }

  // -------- 第二步：选择床位 --------
  const selectedBed = ref(null)
  const availableBedList = ref([])
  const bedQuery = ref({
    buildingId: '',
    roomId: ''
  })
  const loadAvailableBeds = () => {
    checkInApi.listAvailableBeds(bedQuery.value).then(result => {
      availableBedList.value = result.data
    })
  }

  // 单选床位：点击行选中
  const handleBedChange = (row) => {
    selectedBed.value = row
    checkInRecord.value.bedId = row?.id
  }
  // 打开弹窗时预加载（也可以在进入第二步时再加载）
  const openAddDialog = () => {
    showAddDialog()
    loadAvailableElders()
    loadAvailableBeds()
  }

  // -------- 第三步：确认提交 --------
  const submitAdd = () => {
    if (!checkInRecord.value.checkInTime) {
      ElMessage.warning('请选择入住时间')
      return
    }
    checkInApi.add(checkInRecord.value).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        addDialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(result.msg)
      }
    })
  }

  // ==================== 退住 ====================
  const checkoutDialogVisible = ref(false)
  const checkoutForm = ref({})

  const showCheckoutDialog = (row) => {
    checkoutForm.value = {
      id: row.id,
      bedId: row.bedId,
      checkOutTime: ''
    }
    checkoutDialogVisible.value = true
  }

  const submitCheckout = () => {
    if (!checkoutForm.value.checkOutTime) {
      ElMessage.warning('请选择退住时间')
      return
    }
    checkInApi.checkout(checkoutForm.value.id, checkoutForm.value).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        checkoutDialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(result.msg)
      }
    })
  }

  // ==================== 换房 ====================
  const changeRoomDialogVisible = ref(false)
  // 当前行（弹窗中展示老人姓名、原床位信息）
  const changeRoomRow = ref(null)
  // 待提交的换房信息：记录id + 老人id + 新床位id
  const changeRoomForm = ref({})
  // 新床位列表筛选与选中项
  const changeRoomBedQuery = ref({buildingId: ''})
  const changeRoomBedList = ref([])
  const selectedNewBed = ref(null)

  const loadChangeRoomBeds = () => {
    checkInApi.listAvailableBeds(changeRoomBedQuery.value).then(result => {
      changeRoomBedList.value = result.data
    })
  }

  const showChangeRoomDialog = (row) => {
    changeRoomRow.value = row
    // 注意：请求体不能带id，带了会被MyBatis-Plus当成主键插入导致主键冲突；记录id只放在URL路径上
    changeRoomForm.value = {
      elderId: row.elderId,
      bedId: ''
    }
    changeRoomBedQuery.value = {buildingId: ''}
    selectedNewBed.value = null
    changeRoomDialogVisible.value = true
    loadChangeRoomBeds()
  }

  // 单选新床位：点击行选中
  const handleNewBedChange = (row) => {
    selectedNewBed.value = row
    changeRoomForm.value.bedId = row?.id
  }

  const submitChangeRoom = () => {
    if (!changeRoomForm.value.bedId) {
      ElMessage.warning('请先选择一个新床位')
      return
    }
    checkInApi.updateRoom(changeRoomRow.value.id, changeRoomForm.value).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        changeRoomDialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(result.msg)
      }
    })
  }
</script>

<template>
  <el-card class="">
    <template #header>
      <el-form :inline="true" class="search-form" @submit.prevent>
        <el-form-item label="老人姓名">
          <el-input
              v-model="checkInRecordQuery.elderName"
              placeholder="请输入老人姓名"
              clearable
              style="width: 180px"
              @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="楼栋">
          <el-select v-model="checkInRecordQuery.buildingId" clearable filterable placeholder="请选择楼栋" style="width: 150px">
            <el-option
                v-for="item in buildingList"
                :key="item.id"
                :label="item.buildingName"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="checkInRecordQuery.status" clearable placeholder="请选择状态" style="width: 150px">
            <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </template>
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="openAddDialog">办理入住</el-button>
    </div>
    <el-table :data="list" border style="width: 100%" show-overflow-tooltip>
      <el-table-column fixed prop="id" label="ID" width="80"/>
      <el-table-column prop="elderName" label="老人姓名" min-width="100"/>
      <el-table-column prop="idCardNo" label="身份证号" min-width="180"/>
      <el-table-column prop="phone" label="联系电话" min-width="120"/>
      <el-table-column prop="buildingName" label="楼栋" min-width="100"/>
      <el-table-column prop="roomNo" label="房间号" width="100"/>
      <el-table-column prop="bedNo" label="床位号" width="100"/>
      <el-table-column prop="checkInTime" label="入住时间" min-width="180"/>
      <el-table-column prop="checkOutTime" label="退住时间" min-width="180"/>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">
            {{ statusName(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" width="160px" fixed="right" label="操作">
        <template #default="{ row }">
          <template v-if="row.status === 1">
            <el-button size="small" type="warning" @click="showCheckoutDialog(row)">退住</el-button>
            <el-button size="small" type="primary" @click="showChangeRoomDialog(row)">换房</el-button>
          </template>
          <span v-else>—</span>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="checkInRecordQuery.page"
          v-model:page-size="checkInRecordQuery.limit"
          :page-sizes="[10, 20, 30, 40]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @change="loadData"
      />
    </div>
  </el-card>

  <!--办理入住：三步向导-->
  <el-dialog v-model="addDialogVisible" title="办理入住" width="800" :lock-scroll="false" :close-on-click-modal="false">
    <el-steps :active="activeStep" finish-status="success" align-center style="margin-bottom: 20px">
      <el-step title="选择老人"/>
      <el-step title="选择床位"/>
      <el-step title="入住信息"/>
    </el-steps>

    <!--第一步：选择老人-->
    <div v-if="activeStep === 0">
      <el-table :data="availableElderList" border style="width: 100%" highlight-current-row @current-change="handleElderChange">
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="name" label="姓名" min-width="100"/>
        <el-table-column prop="idCardNo" label="身份证号" min-width="180"/>
        <el-table-column prop="phone" label="联系电话" min-width="120"/>
        <el-table-column prop="status" label="状态" min-width="120"/>
      </el-table>
    </div>

    <!--第二步：选择床位-->
    <div v-if="activeStep === 1">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="楼栋">
          <el-select v-model="bedQuery.buildingId" clearable filterable placeholder="请选择楼栋" style="width: 150px" @change="loadAvailableBeds">
            <el-option
                v-for="item in buildingList"
                :key="item.id"
                :label="item.buildingName"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <el-table :data="availableBedList" border style="width: 100%" highlight-current-row @current-change="handleBedChange">
        <el-table-column prop="buildingName" label="楼栋" min-width="100"/>
        <el-table-column prop="roomNo" label="房间号" width="100"/>
        <el-table-column prop="bedNo" label="床位号" width="100"/>
        <el-table-column prop="price" label="费用/月" width="120"/>
      </el-table>
    </div>

    <!--第三步：入住信息-->
    <div v-if="activeStep === 2">
      <el-descriptions :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="老人姓名">{{ selectedElder?.name }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ selectedElder?.phone }}</el-descriptions-item>
        <el-descriptions-item label="床位">{{ selectedBed?.buildingName }} {{ selectedBed?.roomNo }} - {{ selectedBed?.bedNo }}</el-descriptions-item>
        <el-descriptions-item label="费用/月">￥{{ selectedBed?.price }}/月</el-descriptions-item>
      </el-descriptions>
      <el-form :model="checkInRecord" label-width="100px">
        <el-form-item label="入住时间">
          <el-date-picker
              v-model="checkInRecord.checkInTime"
              type="datetime"
              placeholder="请选择入住时间"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="activeStep > 0" @click="prevStep">上一步</el-button>
        <el-button v-if="activeStep < 2" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-if="activeStep === 2" type="primary" @click="submitAdd">确认提交</el-button>
      </div>
    </template>
  </el-dialog>

  <!--退住弹窗-->
  <el-dialog v-model="checkoutDialogVisible" title="退住办理" width="500" :lock-scroll="false" :close-on-click-modal="false">
    <el-form :model="checkoutForm">
      <el-form-item label="退住时间" :label-width="80">
        <el-date-picker
            v-model="checkoutForm.checkOutTime"
            type="datetime"
            placeholder="请选择退住时间"
            value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="checkoutDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCheckout">确认退住</el-button>
      </div>
    </template>
  </el-dialog>

  <!--换房弹窗：选择新床位-->
  <el-dialog v-model="changeRoomDialogVisible" title="换房办理" width="700" :lock-scroll="false" :close-on-click-modal="false">
    <el-descriptions :column="2" border style="margin-bottom: 16px">
      <el-descriptions-item label="老人姓名">{{ changeRoomRow?.elderName }}</el-descriptions-item>
      <el-descriptions-item label="当前床位">{{ changeRoomRow?.buildingName }} {{ changeRoomRow?.roomNo }} - {{ changeRoomRow?.bedNo }}</el-descriptions-item>
    </el-descriptions>
    <el-form :inline="true" @submit.prevent>
      <el-form-item label="楼栋">
        <el-select v-model="changeRoomBedQuery.buildingId" clearable filterable placeholder="请选择楼栋" style="width: 150px" @change="loadChangeRoomBeds">
          <el-option
              v-for="item in buildingList"
              :key="item.id"
              :label="item.buildingName"
              :value="item.id"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <el-table :data="changeRoomBedList" border style="width: 100%" highlight-current-row @current-change="handleNewBedChange">
      <el-table-column prop="buildingName" label="楼栋" min-width="100"/>
      <el-table-column prop="roomNo" label="房间号" width="100"/>
      <el-table-column prop="bedNo" label="床位号" width="100"/>
      <el-table-column prop="price" label="费用/月" width="120"/>
    </el-table>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="changeRoomDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitChangeRoom">确认换房</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px 18px;
}

.search-form :deep(.el-form-item) {
  margin: 0;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
