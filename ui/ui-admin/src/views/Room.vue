<script setup>

  import {ref} from "vue";
  import roomApi from "@/api/room.js";
  import buildingApi from "@/api/building.js";
  import {Delete, Plus, Search, Refresh} from "@element-plus/icons-vue";
  import {ElMessage, ElMessageBox} from "element-plus";

  const list = ref([])
  const total = ref(0)

  const roomQuery=ref({
    roomNo:'',
    buildingId:'',
    roomType:'',
    status:'',
    page:1,
    limit:10
  })

  const loadData = () =>{
    roomApi.list(roomQuery.value).then(result => {
      list.value = result.data.records
      total.value = result.data.total
    })
  }
  loadData()

  // 下拉框数据：楼栋
  const buildingList = ref([])
  const loadOptions = () => {
    buildingApi.list({page: 1, limit: 1000}).then(result => {
      buildingList.value = result.data.records
    })
  }
  loadOptions()

  const onSearch = () => {
    roomQuery.value.page = 1
    loadData()
  }
  const resetSearch = () => {
    roomQuery.value = {
      roomNo:'',
      buildingId:'',
      roomType:'',
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
      roomApi.deleteById(id).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    })
  }
  let ids = []
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
      roomApi.deleteBatch(ids).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    })
  }

  // 添加、修改
  const room = ref({})
  const dialogFormVisible = ref(false)
  const title = ref('')

  const showAddDialog = () => {
    title.value = '添加'
    dialogFormVisible.value = true
    room.value = {status: 0}
  }
  const showUpdateDialog = (id) => {
    title.value = '修改'
    dialogFormVisible.value = true
    roomApi.selectById(id).then(result => {
      room.value = result.data || {}
    })
  }
  const addOrUpdate = () => {
    if(room.value.id){
      roomApi.update(room.value.id,room.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }else {
      roomApi.add(room.value).then(result => {
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

  // 房间类型选项
  const roomTypeOptions = [
    {value: 1, label: '单人间'},
    {value: 2, label: '双人间'},
    {value: 3, label: '多人间'},
  ]
  // 房间状态选项
  const statusOptions = [
    {value: 0, label: '空闲'},
    {value: 1, label: '部分入住'},
    {value: 2, label: '已满'},
    {value: 3, label: '维修'},
  ]
  const roomTypeName = (roomType) => roomTypeOptions.find(item => item.value === roomType)?.label
  const statusName = (status) => statusOptions.find(item => item.value === status)?.label
  const statusTagType = (status) => {
    if (status === 0) return 'success'
    if (status === 1) return 'warning'
    if (status === 2) return 'danger'
    return 'info'
  }
</script>

<template>
  <el-card class="">
    <template #header>
      <el-form :inline="true" class="search-form" @submit.prevent>
        <el-form-item label="房间号">
          <el-input
              v-model="roomQuery.roomNo"
              placeholder="请输入房间号"
              clearable
              style="width: 180px"
              @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="楼栋">
          <el-select v-model="roomQuery.buildingId" clearable filterable placeholder="请选择楼栋" style="width: 150px">
            <el-option
                v-for="item in buildingList"
                :key="item.id"
                :label="item.buildingName"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="房间类型">
          <el-select v-model="roomQuery.roomType" clearable placeholder="请选择房间类型" style="width: 150px">
            <el-option
                v-for="item in roomTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="房间状态">
          <el-select v-model="roomQuery.status" clearable placeholder="请选择房间状态" style="width: 150px">
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
      <el-button type="primary" :icon="Plus" @click="showAddDialog" >添加</el-button>
      <el-button type="danger" :icon="Delete" @click="deleteAll" >批量删除</el-button>
    </div>
    <el-table :data="list" border style="width: 100%" ref="multipleTableRef" show-overflow-tooltip @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column fixed prop="id" label="ID"/>
      <el-table-column prop="roomNo" label="房间号" width="120"/>
      <el-table-column prop="buildingName" label="楼栋" min-width="120"/>
      <el-table-column prop="floor" label="楼层" width="100"/>
      <el-table-column label="房间类型" width="100">
        <template #default="{ row }">{{ roomTypeName(row.roomType) }}</template>
      </el-table-column>
      <el-table-column prop="bedCount" label="床位数量" width="100"/>
      <el-table-column prop="residentCount" label="已入住人数" width="100"/>
      <el-table-column label="房间状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">
            {{ statusName(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
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
          v-model:current-page="roomQuery.page"
          v-model:page-size="roomQuery.limit"
          :page-sizes="[10, 20, 30, 40]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @change="loadData"
      />
    </div>
  </el-card>

  <!--添加、编辑弹出框-->
  <el-dialog v-model="dialogFormVisible" :title="title" width="500" :lock-scroll="false" :close-on-click-modal="false">
    <el-form :model="room">
      <el-form-item label="房间号" :label-width="80">
        <el-input v-model="room.roomNo" placeholder="请输入房间号，如：101" autocomplete="off" />
      </el-form-item>
      <el-form-item label="楼栋" :label-width="80">
        <el-select v-model="room.buildingId" filterable placeholder="请选择楼栋">
          <el-option
              v-for="item in buildingList"
              :key="item.id"
              :label="item.buildingName"
              :value="item.id"
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
        <el-select v-model="room.roomType">
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
            :min="1"
            controls-position="right"
        />
      </el-form-item>
      <el-form-item label="房间状态" :label-width="80">
        <el-select v-model="room.status">
          <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
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
