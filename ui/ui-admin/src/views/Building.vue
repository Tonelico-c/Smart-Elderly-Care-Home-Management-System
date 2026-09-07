<script setup>

  import {ref, computed} from "vue";
  import buildingApi from "@/api/building.js";
  import roomApi from "@/api/room.js";
  import {Delete, Plus, Search, Refresh} from "@element-plus/icons-vue";
  import {ElMessage, ElMessageBox} from "element-plus";

  const list = ref([])
  const total = ref(0)

  const buildingQuery=ref({
    buildingName:'',
    status:'',
    page:1,
    limit:10
  })

  const createTimeRange = ref([]);

  // 顶部统计卡片
  const stats = ref({})
  const loadStats = () => {
    buildingApi.stats().then(result => {
      stats.value = result.data || {}
    })
  }
  const statsCards = [
    {label: '楼栋数量', key: 'buildingCount', color: '#409EFF'},
    {label: '房间总数', key: 'roomCount', color: '#67C23A'},
    {label: '床位总数', key: 'bedCount', color: '#E6A23C'},
    {label: '入住总人数', key: 'occupiedCount', color: '#F56C6C'},
    {label: '空闲床位数量', key: 'freeBedCount', color: '#909399'},
  ]

  const loadData = () =>{
    buildingQuery.value.beginCreateTime = createTimeRange.value?.[0];
    buildingQuery.value.endCreateTime = createTimeRange.value?.[1];
    buildingApi.list(buildingQuery.value).then(result => {
      list.value = result.data.records
      total.value = result.data.total
    })
    loadStats()
  }
  loadData()

  const onSearch = () => {
    buildingQuery.value.page = 1
    loadData()
  }
  const resetSearch = () => {
    createTimeRange.value = []
    buildingQuery.value = {
      buildingName:'',
      status:'',
      page:1,
      limit:10
    }
    loadData()
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
      buildingApi.deleteById(id).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    })
  }
  /*let ids = []
  const handleSelectionChange = (rows) => {
    ids = rows.map(row => row.id)
    console.log(ids)
  }
  const deleteAll = () => {
    ElMessageBox.confirm(
        '您确认要删除所选中的记录吗?',
        '警告',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
        }
    ).then(() => {
      buildingApi.deleteBatch(ids).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    })
  }*/

  // 添加、修改
  const building = ref({})
  const dialogFormVisible = ref(false)
  const title = ref('')

  const showAddDialog = () => {
    title.value = '添加'
    dialogFormVisible.value = true
    building.value = {status: 1}
    roomDialogBuildingId.value = null
  }
  const showUpdateDialog = (id) => {
    title.value = '修改'
    dialogFormVisible.value = true
    roomDialogBuildingId.value = id
    buildingApi.selectById(id).then(result => {
      building.value = result.data || {}
    })
  }
  const addOrUpdate = () => {
    if(building.value.id){
      buildingApi.update(building.value.id,building.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }else {
      buildingApi.add(building.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }
  }

  // 状态选项
  const statusOptions = [
    {value: 0, label: '停用'},
    {value: 1, label: '启用'},
  ]

  // 编辑弹窗内为当前楼栋添加房间（复用房间管理的添加接口）
  const room = ref({})
  const roomDialogVisible = ref(false)
  const roomDialogBuildingId = ref(null)
  // 下拉框数据：楼栋（可切换，默认当前楼栋）
  const buildingOptions = ref([])
  const loadBuildingOptions = () => {
    buildingApi.list({page: 1, limit: 1000}).then(result => {
      buildingOptions.value = result.data.records
    })
  }
  const showAddRoomDialog = () => {
    room.value = {buildingId: roomDialogBuildingId.value, status: 0}
    loadBuildingOptions()
    roomDialogVisible.value = true
  }
  const addRoom = () => {
    // 房型 → 床位数量约束：单人间固定1、双人间固定2、多人间至少3
    if (!validBedCount()) {
      ElMessage.error('床位数量与房型不符：单人间只能1个、双人间只能2个、多人间不能低于3个')
      return
    }
    roomApi.add(room.value).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        roomDialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(result.msg)
      }
    })
  }

  // 床位数量下限随房型变化：单人间/双人间固定，多人间至少3
  const bedCountMin = computed(() => {
    if (room.value.roomType === 3) return 3
    return 1
  })
  // 切换房型时自动校正床位数量
  const onRoomTypeChange = () => {
    if (room.value.roomType === 1) room.value.bedCount = 1
    else if (room.value.roomType === 2) room.value.bedCount = 2
    else if (room.value.roomType === 3 && (!room.value.bedCount || room.value.bedCount < 3)) room.value.bedCount = 3
  }
  const validBedCount = () => {
    if (room.value.roomType === 1) return room.value.bedCount === 1
    if (room.value.roomType === 2) return room.value.bedCount === 2
    if (room.value.roomType === 3) return room.value.bedCount >= 3
    return false
  }

  // 房间类型选项
  const roomTypeOptions = [
    {value: 1, label: '单人间'},
    {value: 2, label: '双人间'},
    {value: 3, label: '多人间'},
  ]
  const roomTypeName = (roomType) => roomTypeOptions.find(item => item.value === roomType)?.label
</script>

<template>
  <div class="building-container">

    <!--顶部统计卡片-->
    <div class="stats-wrapper">
      <div v-for="card in statsCards" :key="card.key" class="stat-card">
        <div class="stat-value" :style="{ color: card.color }">{{ stats[card.key] ?? 0 }}</div>
        <div class="stat-label">{{ card.label }}</div>
      </div>
    </div>

    <el-card class="">
      <template #header>
      <el-form :inline="true" class="search-form" @submit.prevent>
        <el-form-item label="楼栋名称">
          <el-input
              v-model="buildingQuery.buildingName"
              placeholder="请输入楼栋名称"
              clearable
              style="width: 220px"
              @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="buildingQuery.status" clearable placeholder="请选择状态" style="width: 150px">
            <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
              v-model="createTimeRange"
              type="datetimerange"
              value-format="YYYY-MM-DD HH:mm:ss"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </template>
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="showAddDialog" >添加</el-button>
<!--      <el-button type="danger" :icon="Delete" @click="deleteAll" >批量删除</el-button>-->
    </div>
    <el-table :data="list" border style="width: 100%" ref="multipleTableRef" show-overflow-tooltip @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column fixed prop="id" label="ID"/>
      <el-table-column prop="buildingNo" label="楼栋编号" width="120"/>
      <el-table-column prop="buildingName" label="楼栋名称" min-width="120"/>
      <el-table-column prop="floorCount" label="楼层数量" width="100"/>
      <el-table-column prop="roomCount" label="房间数" width="100"/>
      <el-table-column prop="bedCount" label="床位数" width="100"/>
      <el-table-column prop="residentCount" label="入住人数" width="100"/>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="150"/>
      <el-table-column prop="createTime" label="创建时间" width="200px"/>
      <el-table-column align="center" width="200px" fixed="right" label="操作">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="showUpdateDialog(row.id)" >编辑</el-button>
          <el-button size="small" type="danger" @click="deleteById(row.id)" >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="buildingQuery.page"
          v-model:page-size="buildingQuery.limit"
          :page-sizes="[10, 20, 30, 40]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @change="loadData"
      />
    </div>
    </el-card>
  </div>

  <!--添加、编辑弹出框-->
  <el-dialog v-model="dialogFormVisible" :title="title" width="500" :lock-scroll="false" :close-on-click-modal="false">
    <!--编辑时可快捷为当前楼栋添加房间-->
    <div v-if="building.id" class="dialog-toolbar">
      <el-button type="success" :icon="Plus" @click="showAddRoomDialog">添加房间</el-button>
    </div>
    <el-form :model="building">
      <el-form-item label="楼栋编号" :label-width="80">
        <el-input v-model="building.buildingNo" placeholder="请输入楼栋编号，如：A栋" autocomplete="off" />
      </el-form-item>
      <el-form-item label="楼栋名称" :label-width="80">
        <el-input v-model="building.buildingName" placeholder="请输入楼栋名称" autocomplete="off" />
      </el-form-item>
      <el-form-item label="楼层数量" :label-width="80">
        <el-input-number
            v-model="building.floorCount"
            :min="1"
            controls-position="right"
        />
      </el-form-item>
      <el-form-item label="状态" :label-width="80">
        <el-select v-model="building.status">
          <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="描述" :label-width="80">
        <el-input v-model="building.description" type="textarea" autocomplete="off" />
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

  <!--为当前楼栋添加房间弹出框（嵌套弹窗，楼栋默认选中当前楼栋）-->
  <el-dialog v-model="roomDialogVisible" title="添加房间" width="500" append-to-body :lock-scroll="false" :close-on-click-modal="false">
    <el-form :model="room">
      <el-form-item label="房间号" :label-width="80">
        <el-input v-model="room.roomNo" placeholder="请输入房间号，如：101" autocomplete="off" />
      </el-form-item>
      <el-form-item label="楼栋" :label-width="80">
        <el-select v-model="room.buildingId" placeholder="请选择楼栋">
          <el-option
              v-for="item in buildingOptions"
              :key="item.id"
              :label="item.buildingName"
              :value="item.id"
              :disabled="item.id !== roomDialogBuildingId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="楼层" :label-width="80">
        <el-input-number
            v-model="room.floor"
            :min="1"
            controls-position="right"
        />
      </el-form-item>
      <el-form-item label="房间类型" :label-width="80">
        <el-select v-model="room.roomType" @change="onRoomTypeChange">
          <el-option
              v-for="item in roomTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="床位数量" :label-width="80">
        <el-input-number
            v-model="room.bedCount"
            :min="bedCountMin"
            :disabled="room.roomType === 1 || room.roomType === 2"
            controls-position="right"
        />
        <div v-if="room.roomType === 1 || room.roomType === 2" class="status-tip">
          {{ roomTypeName(room.roomType) }}床位数量固定为 {{ room.bedCount }} 个
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="roomDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addRoom">
          确认
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.building-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 顶部统计卡片：5个等宽卡片一行排列 */
.stats-wrapper {
  display: flex;
  gap: 16px;
}

.stat-card {
  flex: 1;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  padding: 16px 20px;
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  line-height: 1.2;
}

.stat-label {
  margin-top: 6px;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

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

/* 编辑弹窗内的快捷操作区 */
.dialog-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
