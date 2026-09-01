<script setup>

  import {ref} from "vue";
  import carePlanApi from "@/api/carePlan.js";
  import careLevelApi from "@/api/careLevel.js";
  import careItemApi from "@/api/careItem.js";
  import elderApi from "@/api/elder.js";
  import userApi from "@/api/user.js";
  import {Delete, Plus, Search, Refresh} from "@element-plus/icons-vue";
  import {ElMessage, ElMessageBox} from "element-plus";
  import hasBtnPermission from "@/utils/btnPermission.js";

  const list = ref([])
  const total = ref(0)

  const carePlanQuery=ref({
    name:'',
    status:'',
    page:1,
    limit:10
  })

  const createTimeRange = ref([]);
  const loadData = () =>{
    carePlanQuery.value.beginCreateTime = createTimeRange.value?.[0];
    carePlanQuery.value.endCreateTime = createTimeRange.value?.[1];
    carePlanApi.list(carePlanQuery.value).then(result => {
      list.value = result.data.records
      total.value = result.data.total
    })
  }
  loadData()

  // 下拉框数据：老人、护理人员、护理等级
  const elderList = ref([])
  const userList = ref([])
  const careLevelList = ref([])
  const careItemList = ref([])
  const loadOptions = () => {
    elderApi.list({page: 1, limit: 1000}).then(result => {
      elderList.value = result.data.records
    })
    userApi.list({page: 1, limit: 1000}).then(result => {
      userList.value = result.data.records
    })
    careLevelApi.list({page: 1, limit: 1000}).then(result => {
      careLevelList.value = result.data.records
    })
    careItemApi.list({page: 1, limit: 1000}).then(result => {
      careItemList.value = result.data.records
    })
  }
  loadOptions()

  const elderName = (elderId) => elderList.value.find(elder => elder.id === elderId)?.name
  const userName = (userId) => userList.value.find(user => user.id === userId)?.name
  const careLevelName = (careLevelId) => careLevelList.value.find(careLevel => careLevel.id === careLevelId)?.name

  const onSearch = () => {
    carePlanQuery.value.page = 1
    loadData()
  }
  const resetSearch = () => {
    createTimeRange.value = []
    carePlanQuery.value = {
      name:'',
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
      carePlanApi.deleteById(id).then(result => {
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
      carePlanApi.deleteBatch(ids).then(result => {
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
  const carePlan = ref({})
  const dialogFormVisible = ref(false)
  const title = ref('')

  const showAddDialog = () => {
    title.value = '添加'
    dialogFormVisible.value = true
    carePlan.value = {items: []}
  }
  const showUpdateDialog = (id) => {
    title.value = '修改'
    dialogFormVisible.value = true
    carePlanApi.selectById(id).then(result => {
      carePlan.value = result.data || {}
      if (!carePlan.value.items) {
        carePlan.value.items = []
      }
      // 数据库 time 类型返回 HH:mm:ss，统一截取为 HH:mm 供时间选择器使用
      carePlan.value.items.forEach(item => {
        item.executeTime = item.executeTime ? item.executeTime.slice(0, 5) : ''
      })
    })
  }

  // 护理计划明细（执行周期：0 天 1 周 2 月）
  const cycleOptions = [
    {value: 0, label: '每天'},
    {value: 1, label: '每周'},
    {value: 2, label: '每月'},
  ]
  const addItem = () => {
    carePlan.value.items.push({careItemId: null, executeTime: '08:00', executeCycle: 0, executeFrequency: 1})
  }
  const removeItem = (index) => {
    carePlan.value.items.splice(index, 1)
  }
  const addOrUpdate = () => {
    if(carePlan.value.id){
      carePlanApi.update(carePlan.value.id,carePlan.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }else {
      carePlanApi.add(carePlan.value).then(result => {
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
    {value: 0, label: '已结束'},
    {value: 1, label: '进行中'},
  ]
</script>

<template>
  <el-card class="">
    <template #header>
      <el-form :inline="true" class="search-form" @submit.prevent>
        <el-form-item label="名称">
          <el-input
              v-model="carePlanQuery.name"
              placeholder="请输入名称"
              clearable
              style="width: 220px"
              @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="carePlanQuery.status" clearable placeholder="请选择状态" style="width: 150px">
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
      <el-button type="danger" :icon="Delete" @click="deleteAll" >批量删除</el-button>
    </div>
    <el-table :data="list" border style="width: 100%" ref="multipleTableRef" show-overflow-tooltip @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column fixed prop="id" label="ID"/>
      <el-table-column prop="name" label="计划名称" min-width="120"/>
      <el-table-column label="老人" width="100">
        <template #default="{ row }">{{ elderName(row.elderId) }}</template>
      </el-table-column>
      <el-table-column label="护理人员" width="100">
        <template #default="{ row }">{{ userName(row.userId) }}</template>
      </el-table-column>
      <el-table-column label="护理等级" width="100">
        <template #default="{ row }">{{ careLevelName(row.careLevelId) }}</template>
      </el-table-column>
      <el-table-column prop="startDate" label="开始日期" width="120"/>
      <el-table-column prop="endDate" label="结束日期" width="120"/>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '进行中' : '已结束' }}
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
          v-model:current-page="carePlanQuery.page"
          v-model:page-size="carePlanQuery.limit"
          :page-sizes="[10, 20, 30, 40]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @change="loadData"
      />
    </div>
  </el-card>

  <!--添加、编辑弹出框-->
  <el-dialog v-model="dialogFormVisible" :title="title" width="800" :lock-scroll="false" :close-on-click-modal="false">
    <el-form :model="carePlan">
      <el-form-item label="计划名称" :label-width="80">
        <el-input v-model="carePlan.name" autocomplete="off" />
      </el-form-item>
      <el-form-item label="老人" :label-width="80">
        <el-select v-model="carePlan.elderId" filterable placeholder="请选择老人">
          <el-option
              v-for="item in elderList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="护理人员" :label-width="80">
        <el-select v-model="carePlan.userId" filterable placeholder="请选择护理人员">
          <el-option
              v-for="item in userList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="护理等级" :label-width="80">
        <el-select v-model="carePlan.careLevelId" placeholder="请选择护理等级">
          <el-option
              v-for="item in careLevelList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="开始日期" :label-width="80">
        <el-date-picker
            v-model="carePlan.startDate"
            type="date"
            placeholder="请选择开始日期"
            value-format="YYYY-MM-DD"
        />
      </el-form-item>
      <el-form-item label="结束日期" :label-width="80">
        <el-date-picker
            v-model="carePlan.endDate"
            type="date"
            placeholder="请选择结束日期"
            value-format="YYYY-MM-DD"
        />
      </el-form-item>
      <el-form-item label="状态" :label-width="80">
        <el-select v-model="carePlan.status">
          <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </el-form-item>

      <!--护理计划明细-->
      <el-form-item label="护理明细" :label-width="80">
        <div class="care-items-wrapper">
          <div class="care-items-header">
            <span>护理项目</span>
            <el-button type="primary" size="small" :icon="Plus" @click="addItem">添加项目</el-button>
          </div>
          <el-table :data="carePlan.items" border size="small">
            <el-table-column label="护理项目" min-width="140">
              <template #default="{ row }">
                <el-select v-model="row.careItemId" filterable placeholder="请选择">
                  <el-option
                      v-for="item in careItemList"
                      :key="item.id"
                      :label="item.name"
                      :value="item.id"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="护理服务时间" width="130">
              <template #default="{ row }">
                <el-time-picker
                    v-model="row.executeTime"
                    format="HH:mm"
                    value-format="HH:mm"
                    placeholder="选择时间"
                    style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column label="执行周期" width="110">
              <template #default="{ row }">
                <el-select v-model="row.executeCycle" placeholder="请选择">
                  <el-option
                      v-for="cycle in cycleOptions"
                      :key="cycle.value"
                      :label="cycle.label"
                      :value="cycle.value"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="执行频次" width="120">
              <template #default="{ row }">
                <el-input-number
                    v-model="row.executeFrequency"
                    :min="1"
                    controls-position="right"
                    style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ $index }">
                <el-button type="danger" size="small" @click="removeItem($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
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

.care-items-wrapper {
  width: 100%;
}

.care-items-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

</style>
