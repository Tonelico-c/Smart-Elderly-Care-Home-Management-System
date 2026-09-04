<script setup>

  import {ref} from "vue";
  import bedApi from "@/api/bed.js";
  import buildingApi from "@/api/building.js";
  import roomApi from "@/api/room.js";
  import {Delete, Plus, Refresh} from "@element-plus/icons-vue";
  import {ElMessage, ElMessageBox} from "element-plus";
  import checkInApi from "@/api/checkin.js";

  const list = ref([])

  // 顶部导航：楼栋 -> 房间 级联选择
  const buildingId = ref('')
  const roomId = ref('')
  const buildingList = ref([])
  const roomList = ref([])

  const loadBuildings = () => {
    buildingApi.list({page: 1, limit: 1000}).then(result => {
      buildingList.value = result.data.records
    })
  }
  loadBuildings()

  // 选择楼栋后，加载该楼栋的所有房间
  const onBuildingChange = () => {
    roomId.value = ''
    roomList.value = []
    list.value = []
    if (!buildingId.value) {
      return
    }
    roomApi.list({page: 1, limit: 1000, buildingId: buildingId.value}).then(result => {
      roomList.value = result.data.records
    })
  }

  // 选择房间后，加载该房间的床位信息
  const onRoomChange = () => {
    list.value = []
    if (!roomId.value) {
      return
    }
    bedApi.listByRoom(roomId.value).then(result => {
      list.value = result.data || []
    })
  }
  const loadBeds = () => {
    onRoomChange()
  }

  const resetNav = () => {
    buildingId.value = ''
    roomId.value = ''
    roomList.value = []
    list.value = []
  }

  // 删除
  const deleteById = (id) => {
    ElMessageBox.confirm(
        '您确认要删除么?',
        '警告',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
        }
    ).then(() => {
      bedApi.deleteById(id).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadBeds()
        } else {
          ElMessage.error(result.msg)
        }
      })
    })
  }

  // 添加、修改
  const bed = ref({})
  const dialogFormVisible = ref(false)
  const title = ref('')

  const showAddDialog = () => {
    if (!roomId.value) {
      ElMessage.warning('请先选择房间')
      return
    }
    title.value = '添加'
    dialogFormVisible.value = true
    bed.value = {roomId: roomId.value, status: 0}
  }
  const showUpdateDialog = (id) => {
    title.value = '修改'
    dialogFormVisible.value = true
    bedApi.selectById(id).then(result => {
      bed.value = result.data || {}
    })
  }
  const addOrUpdate = () => {
    if(bed.value.id){
      bedApi.update(bed.value.id,bed.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadBeds()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }else {
      bedApi.add(bed.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadBeds()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }
  }

  // 床位状态选项
  const statusOptions = [
    {value: 0, label: '空闲'},
    {value: 1, label: '入住'},
    {value: 2, label: '维修'},
    {value: 3, label: '停用'},
    {value: 4, label: '请假'},
  ]
  const statusName = (status) => statusOptions.find(item => item.value === status)?.label
  const statusTagType = (status) => {
    if (status === 0) return 'success'
    if (status === 1) return 'danger'
    if (status === 2) return 'warning'
    if (status === 4) return 'primary'
    return 'info'
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
</script>

<template>
  <div class="bed-container">

    <!--顶部导航：楼栋 -> 房间 级联选择-->
    <el-card class="nav-card">
      <div class="nav-wrapper">
        <span class="nav-label">楼栋：</span>
        <el-select
            v-model="buildingId"
            filterable
            clearable
            placeholder="请选择楼栋"
            style="width: 220px"
            @change="onBuildingChange"
        >
          <el-option
              v-for="item in buildingList"
              :key="item.id"
              :label="item.buildingName"
              :value="item.id"
          />
        </el-select>

        <span class="nav-label">房间：</span>
        <el-select
            v-model="roomId"
            filterable
            clearable
            :disabled="!buildingId"
            placeholder="请先选择楼栋"
            style="width: 220px"
            @change="onRoomChange"
        >
          <el-option
              v-for="item in roomList"
              :key="item.id"
              :label="item.roomNo"
              :value="item.id"
          />
        </el-select>

        <el-button :icon="Refresh" @click="resetNav">重置</el-button>
      </div>
    </el-card>

    <!--床位信息-->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>床位信息</span>
          <el-button type="primary" :icon="Plus" :disabled="!roomId" @click="showAddDialog">添加床位</el-button>
        </div>
      </template>

      <el-empty v-if="!roomId" description="请先选择楼栋和房间，查看对应房间的床位信息"/>

      <template v-else>
        <el-table :data="list" border style="width: 100%" show-overflow-tooltip>
          <el-table-column fixed prop="id" label="ID"/>
          <el-table-column prop="buildingName" label="楼栋名" min-width="120"/>
          <el-table-column prop="roomNo" label="房间编号" min-width="120"/>
          <el-table-column prop="bedNo" label="床位编号" min-width="120"/>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)">
                {{ statusName(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="费用/月" width="120"/>
          <el-table-column prop="elderName" label="入住老人" min-width="120">
            <template #default="{ row }">{{ row.elderName || '-' }}</template>
          </el-table-column>
          <el-table-column prop="checkInTime" label="入住时间" width="200px">
            <template #default="{ row }">{{ row.checkInTime || '-' }}</template>
          </el-table-column>
          <el-table-column align="center" width="200px" fixed="right" label="操作">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="showUpdateDialog(row.id)" >编辑</el-button>
              <el-button :disabled="row.status !== 1" size="small" type="warning" @click="showCheckoutDialog(row)">退住</el-button>
              <el-button size="small" type="danger" @click="deleteById(row.id)" >删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-card>
  </div>

  <!--添加、编辑弹出框-->
  <el-dialog v-model="dialogFormVisible" :title="title" width="500" :lock-scroll="false" :close-on-click-modal="false">
    <el-form :model="bed">
      <el-form-item label="床位编号" :label-width="80">
        <el-input v-model="bed.bedNo" placeholder="请输入床位编号，如：1号床" autocomplete="off" />
      </el-form-item>
      <el-form-item label="床位状态" :label-width="80">
        <el-select v-model="bed.status">
          <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="费用/月" :label-width="80">
        <el-input-number
            v-model="bed.price"
            :min="0"
            :precision="2"
            controls-position="right"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="addOrUpdate">
          确认
        </el-button>
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
</template>

<style scoped>
.bed-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.nav-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-label {
  color: var(--el-text-color-regular);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
