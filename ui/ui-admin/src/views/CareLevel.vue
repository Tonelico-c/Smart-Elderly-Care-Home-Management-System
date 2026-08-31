<script setup>

  import {ref} from "vue";
  import careLevelApi from "@/api/careLevel.js";
  import {Delete, Plus, Search, Refresh} from "@element-plus/icons-vue";
  import {ElMessage, ElMessageBox} from "element-plus";
  import hasBtnPermission from "@/utils/btnPermission.js";

  const list = ref([])
  const total = ref(0)

  const careLevelQuery=ref({
    name:'',
    status:'',
    page:1,
    limit:10
  })

  const createTimeRange = ref([]);
  const loadData = () =>{
    careLevelQuery.value.beginCreateTime = createTimeRange.value?.[0];
    careLevelQuery.value.endCreateTime = createTimeRange.value?.[1];
    careLevelApi.list(careLevelQuery.value).then(result => {
      list.value = result.data.records
      total.value = result.data.total
    })
  }
  loadData()

  const onSearch = () => {
    careLevelQuery.value.page = 1
    loadData()
  }
  const resetSearch = () => {
    createTimeRange.value = []
    careLevelQuery.value = {
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
      careLevelApi.deleteById(id).then(result => {
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
      careLevelApi.deleteBatch(ids).then(result => {
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
  const careLevel = ref({})
  const dialogFormVisible = ref(false)
  const title = ref('')

  const showAddDialog = () => {
    title.value = '添加'
    dialogFormVisible.value = true
    careLevel.value = {}
  }
  const showUpdateDialog = (id) => {
    title.value = '修改'
    dialogFormVisible.value = true
    careLevelApi.selectById(id).then(result => {
      careLevel.value = result.data
    })
  }
  const addOrUpdate = () => {
    if(careLevel.value.id){
      careLevelApi.update(careLevel.value.id,careLevel.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }else {
      careLevelApi.add(careLevel.value).then(result => {
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
    {value: 0, label: '禁用'},
    {value: 1, label: '启用'},
  ]

  // 表格中修改状态
  const handleStatusChange = (row) => {
    careLevelApi.update(row.id, row).then(result => {
      if (result.code === 1) {
        ElMessage.success('修改状态成功')
      } else {
        ElMessage.error(result.msg)
        loadData()
      }
    })
  }
</script>

<template>
  <el-card class="">
    <template #header>
      <el-form :inline="true" class="search-form" @submit.prevent>
        <el-form-item label="名称">
          <el-input
              v-model="careLevelQuery.name"
              placeholder="请输入名称"
              clearable
              style="width: 220px"
              @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="careLevelQuery.status" clearable placeholder="请选择状态" style="width: 150px">
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
      <el-table-column prop="name" label="等级名称" width="120"/>
      <el-table-column prop="price" label="护理费用(元)" width="120"/>
      <el-table-column prop="description" label="等级说明" min-width="150"/>
      <el-table-column prop="sort" label="排序" width="80"/>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-select v-model="row.status" @change="handleStatusChange(row)">
            <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
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
          v-model:current-page="careLevelQuery.page"
          v-model:page-size="careLevelQuery.limit"
          :page-sizes="[10, 20, 30, 40]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @change="loadData"
      />
    </div>
  </el-card>

  <!--添加、编辑弹出框-->
  <el-dialog v-model="dialogFormVisible" :title="title" width="500" :lock-scroll="false" :close-on-click-modal="false">
    <el-form :model="careLevel">
      <el-form-item label="等级名称" :label-width="80">
        <el-input v-model="careLevel.name" autocomplete="off" />
      </el-form-item>
      <el-form-item label="护理费用" :label-width="80">
        <el-input-number v-model="careLevel.price" :precision="2" :min="0" autocomplete="off" />
      </el-form-item>
      <el-form-item label="等级说明" :label-width="80">
        <el-input v-model="careLevel.description" type="textarea" :rows="3" autocomplete="off" />
      </el-form-item>
      <el-form-item label="排序" :label-width="80">
        <el-input-number v-model="careLevel.sort" :min="0" autocomplete="off" />
      </el-form-item>
      <el-form-item label="状态" :label-width="80">
        <el-select v-model="careLevel.status">
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
