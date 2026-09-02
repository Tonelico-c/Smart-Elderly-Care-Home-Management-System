<script setup>

  import {ref} from "vue";
  import examItemApi from "@/api/examItem.js";
  import {Delete, Plus, Search, Refresh} from "@element-plus/icons-vue";
  import {ElMessage, ElMessageBox} from "element-plus";

  const list = ref([])
  const total = ref(0)

  const examItemQuery=ref({
    name:'',
    status:'',
    page:1,
    limit:10
  })

  const createTimeRange = ref([]);
  const loadData = () =>{
    examItemQuery.value.beginCreateTime = createTimeRange.value?.[0];
    examItemQuery.value.endCreateTime = createTimeRange.value?.[1];
    examItemApi.list(examItemQuery.value).then(result => {
      list.value = result.data.records
      total.value = result.data.total
    })
  }
  loadData()

  const onSearch = () => {
    examItemQuery.value.page = 1
    loadData()
  }
  const resetSearch = () => {
    createTimeRange.value = []
    examItemQuery.value = {
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
      examItemApi.deleteById(id).then(result => {
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
      examItemApi.deleteBatch(ids).then(result => {
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
  const examItem = ref({})
  const dialogFormVisible = ref(false)
  const title = ref('')

  const showAddDialog = () => {
    title.value = '添加'
    dialogFormVisible.value = true
    examItem.value = {}
  }
  const showUpdateDialog = (id) => {
    title.value = '修改'
    dialogFormVisible.value = true
    examItemApi.selectById(id).then(result => {
      examItem.value = result.data || {}
    })
  }
  const addOrUpdate = () => {
    if(examItem.value.id){
      examItemApi.update(examItem.value.id,examItem.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }else {
      examItemApi.add(examItem.value).then(result => {
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

  // 结果类型选项：0文本 1数值
  const resultTypeOptions = [
    {value: 0, label: '文本'},
    {value: 1, label: '数值'},
  ]
</script>

<template>
  <el-card class="">
    <template #header>
      <el-form :inline="true" class="search-form" @submit.prevent>
        <el-form-item label="名称">
          <el-input
              v-model="examItemQuery.name"
              placeholder="请输入名称"
              clearable
              style="width: 220px"
              @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="examItemQuery.status" clearable placeholder="请选择状态" style="width: 150px">
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
      <el-table-column prop="name" label="名称" width="120"/>
      <el-table-column prop="price" label="价格(元)" width="100"/>
      <el-table-column prop="unit" label="单位" width="90"/>
      <el-table-column prop="resultType" label="结果类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.resultType === 1 ? 'primary' : 'info'">
            {{ row.resultType === 1 ? '数值' : '文本' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="参考范围" width="160">
        <template #default="{ row }">
          <span v-if="row.resultType === 1">{{ row.referenceMin }} ~ {{ row.referenceMax }} {{ row.referenceUnit }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="项目说明" min-width="150"/>
      <el-table-column prop="sort" label="排序" width="80"/>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="200px"/>
      <el-table-column align="center" width="200px" fixed="right" label="操作">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="showUpdateDialog(row.id)" >编辑</el-button>
          <el-button size="small" type="danger" @click="deleteById(row.id)"  >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="examItemQuery.page"
          v-model:page-size="examItemQuery.limit"
          :page-sizes="[10, 20, 30, 40]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @change="loadData"
      />
    </div>
  </el-card>

  <!--添加、编辑弹出框-->
  <el-dialog v-model="dialogFormVisible" :title="title" width="500" :lock-scroll="false" :close-on-click-modal="false">
    <el-form :model="examItem">
      <el-form-item label="名称" :label-width="80">
        <el-input v-model="examItem.name" autocomplete="off" />
      </el-form-item>
      <el-form-item label="价格" :label-width="80">
        <el-input-number v-model="examItem.price" :precision="2" :min="0" autocomplete="off" />
      </el-form-item>
      <el-form-item label="单位" :label-width="80">
        <el-input v-model="examItem.unit" autocomplete="off" />
      </el-form-item>
      <el-form-item label="结果类型" :label-width="80">
        <el-select v-model="examItem.resultType">
          <el-option
              v-for="item in resultTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </el-form-item>
      <template v-if="examItem.resultType === 1">
        <el-form-item label="参考下限" :label-width="80">
          <el-input-number v-model="examItem.referenceMin" :precision="2" :min="0" autocomplete="off" />
        </el-form-item>
        <el-form-item label="参考上限" :label-width="80">
          <el-input-number v-model="examItem.referenceMax" :precision="2" :min="0" autocomplete="off" />
        </el-form-item>
        <el-form-item label="参考单位" :label-width="80">
          <el-input v-model="examItem.referenceUnit" autocomplete="off" />
        </el-form-item>
      </template>
      <el-form-item label="项目说明" :label-width="80">
        <el-input v-model="examItem.description" type="textarea" :rows="3" autocomplete="off" />
      </el-form-item>
      <el-form-item label="排序" :label-width="80">
        <el-input-number v-model="examItem.sort" :min="0" autocomplete="off" />
      </el-form-item>
      <el-form-item label="状态" :label-width="80">
        <el-select v-model="examItem.status">
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
