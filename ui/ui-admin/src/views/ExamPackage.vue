<script setup>

  import {ref} from "vue";
  import examPackageApi from "@/api/examPackage.js";
  import examItemApi from "@/api/examItem.js";
  import {Delete, Plus, Search, Refresh} from "@element-plus/icons-vue";
  import {ElMessage, ElMessageBox} from "element-plus";
  import {useTokenStore} from '@/store/token.js'
  const tokenStore = useTokenStore();

  const list = ref([])
  const total = ref(0)

  const examPackageQuery=ref({
    name:'',
    status:'',
    page:1,
    limit:10
  })

  const createTimeRange = ref([]);
  const loadData = () =>{
    examPackageQuery.value.beginCreateTime = createTimeRange.value?.[0];
    examPackageQuery.value.endCreateTime = createTimeRange.value?.[1];
    examPackageApi.list(examPackageQuery.value).then(result => {
      list.value = result.data.records
      total.value = result.data.total
    })
  }
  loadData()

  const onSearch = () => {
    examPackageQuery.value.page = 1
    loadData()
  }
  const resetSearch = () => {
    createTimeRange.value = []
    examPackageQuery.value = {
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
      examPackageApi.deleteById(id).then(result => {
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
      examPackageApi.deleteBatch(ids).then(result => {
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
  const examPackage = ref({})
  const dialogFormVisible = ref(false)
  const title = ref('')

  const showAddDialog = () => {
    title.value = '添加'
    dialogFormVisible.value = true
    examPackage.value = {}
  }
  const showUpdateDialog = (id) => {
    title.value = '修改'
    dialogFormVisible.value = true
    examPackageApi.selectById(id).then(result => {
      examPackage.value = result.data || {}
    })
  }
  const addOrUpdate = () => {
    if(examPackage.value.id){
      examPackageApi.update(examPackage.value.id,examPackage.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }else {
      examPackageApi.add(examPackage.value).then(result => {
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

  // 图片上传成功
  const handleAvatarSuccess = (res) => {
      examPackage.value.image = res.data
  }

  // ===== 给套餐分配体检项目(穿梭框) =====
  const assignDialogVisible = ref(false)
  const currentPackage = ref({})
  // 穿梭框的数据源,每项必须是 {key, label, disabled} 结构
  const examItemData = ref([])
  // 穿梭框右侧已选中的体检项目id数组(v-model双向绑定)
  const assignedItemIds = ref([])

  const showAssignDialog = (row) => {
    currentPackage.value = row
    assignDialogVisible.value = true
    // 加载所有启用的体检项目,作为穿梭框左侧可选数据
    examItemApi.list({page: 1, limit: 9999, status: 1}).then(result => {
      examItemData.value = result.data.records.map(item => ({
        key: item.id,
        label: item.name,
        disabled: false
      }))
    })
    // 加载该套餐已分配的项目id,回显到穿梭框右侧
    examPackageApi.selectAssignedItem(row.id).then(result => {
      assignedItemIds.value = result.data
    })
  }

  const assignItem = () => {
    examPackageApi.assignItem(currentPackage.value.id, assignedItemIds.value).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        assignDialogVisible.value = false
      } else {
        ElMessage.error(result.msg)
      }
    })
  }

  // 状态选项：0下架 1上架
  const statusOptions = [
    {value: 0, label: '下架'},
    {value: 1, label: '上架'},
  ]
</script>

<template>
  <el-card class="">
    <template #header>
      <el-form :inline="true" class="search-form" @submit.prevent>
        <el-form-item label="名称">
          <el-input
              v-model="examPackageQuery.name"
              placeholder="请输入名称"
              clearable
              style="width: 220px"
              @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="examPackageQuery.status" clearable placeholder="请选择状态" style="width: 150px">
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
      <el-table-column prop="image" label="图片">
        <template #default="{row}">
          <img :src="row.image" alt="" class="table-image">
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" width="140"/>
      <el-table-column prop="price" label="价格(元)" width="100"/>
      <el-table-column prop="description" label="套餐说明" min-width="150"/>
      <el-table-column prop="sort" label="排序" width="80"/>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="200px"/>
      <el-table-column align="center" width="280px" fixed="right" label="操作">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="showUpdateDialog(row.id)" >编辑</el-button>
          <el-button size="small" type="warning" @click="showAssignDialog(row)" >分配项目</el-button>
          <el-button size="small" type="danger" @click="deleteById(row.id)"  >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="examPackageQuery.page"
          v-model:page-size="examPackageQuery.limit"
          :page-sizes="[10, 20, 30, 40]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @change="loadData"
      />
    </div>
  </el-card>

  <!--添加、编辑弹出框-->
  <el-dialog v-model="dialogFormVisible" :title="title" width="500" :lock-scroll="false" :close-on-click-modal="false">
    <el-form :model="examPackage">
      <el-form-item label="名称" :label-width="80">
        <el-input v-model="examPackage.name" autocomplete="off" />
      </el-form-item>
      <el-form-item label="价格" :label-width="80">
        <el-input-number v-model="examPackage.price" :precision="2" :min="0" autocomplete="off" />
      </el-form-item>
      <el-form-item label="套餐说明" :label-width="80">
        <el-input v-model="examPackage.description" type="textarea" :rows="3" autocomplete="off" />
      </el-form-item>
      <el-form-item label="排序" :label-width="80">
        <el-input-number v-model="examPackage.sort" :min="0" autocomplete="off" />
      </el-form-item>
      <el-form-item label="状态" :label-width="80">
        <el-select v-model="examPackage.status">
          <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="图片" :label-width="60">
        <el-upload
            class="avatar-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :headers="{Authorization: tokenStore.token}"
        >
          <img v-if="examPackage.image" :src="examPackage.image" class="avatar"/>
          <el-icon v-else class="avatar-uploader-icon">
            <Plus/>
          </el-icon>
        </el-upload>
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

  <!--分配体检项目弹出框(穿梭框)-->
  <el-dialog v-model="assignDialogVisible" :title="'给【' + currentPackage.name + '】分配体检项目'" width="700" :lock-scroll="false" :close-on-click-modal="false">
    <!--
      v-model: 右侧已选中的key数组(即选中的体检项目id)
      :data: 穿梭框数据源,每项必须是 {key, label, disabled} 结构
      filterable: 开启搜索框,按label过滤项目名
    -->
    <el-transfer
        v-model="assignedItemIds"
        :data="examItemData"
        :titles="['可选体检项目', '已选体检项目']"
        filterable
        filter-placeholder="请输入项目名"
    />
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="assignItem">
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

.table-image {
  width: 50px;
  height: 50px;
  display: block;
  object-fit: cover;
  margin: 0 auto;
}

.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}
</style>
