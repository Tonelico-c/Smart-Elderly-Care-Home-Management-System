<script setup>

  import {
    Delete,
    Plus,
    Refresh,
    Search,
  } from "@element-plus/icons-vue";
  import {ref} from "vue";
  import permissionApi from "@/api/permission.js"
  import {ElMessage, ElMessageBox} from "element-plus"

  const permissionQuery = ref({
    parentId:'',
    name:'',
    permissionValue:'',
    page:1,
    limit:10
  });
  const list = ref([])
  const total = ref(0)
  const createTimeRange = ref([])
  // 接口返回的是 PermissionVO 对象,真正的id列表在 parentIds 字段里
  const permissionList = ref([])
  permissionApi.selectParentId().then(result => {
    permissionList.value = result.data.parentIds
  })

  const loadData = () => {
    permissionQuery.value.beginCreateTime = createTimeRange.value?.[0]
    permissionQuery.value.endCreateTime = createTimeRange.value?.[1]
    permissionApi.list(permissionQuery.value).then(result => {
      list.value = result.data.records
      total.value = result.data.total
    })
  }
  loadData()
  const onSearch = () => {
    permissionQuery.value.page = 1
    loadData()
  }
  const resetSearch = () => {
    permissionQuery.value = {
      parentId:'',
      name:'',
      permissionValue:'',
      page:1,
      limit:10
    }
    createTimeRange.value = []
    loadData()
  }

  let ids = []
  const handleSelectionChange = (rows) => {
    ids = rows.map(row => row.id)
  }
  const deleteById = id => {
    ElMessageBox.confirm('确定删除该权限吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      permissionApi.deleteById(id).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    })
  }
  const deleteAll = () => {
    ElMessageBox.confirm('确定删除所选权限吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      permissionApi.deleteBatch(ids).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    })
  }

  const permission = ref({})
  const dialogFormVisible = ref(false)
  const title = ref()
  const showAddDialog = () => {
    title.value = '添加权限'
    permission.value = {}
    dialogFormVisible.value = true
  }
  const showUpdateDialog = id => {
    title.value = '编辑权限'
    permissionApi.selectById(id).then(result => {
      permission.value = result.data
      dialogFormVisible.value = true
    })
  }
  const addOrUpdate = () => {
    if(permission.value.id){
      permissionApi.update(permission.value.id,permission.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }else{
      permissionApi.add(permission.value).then(result => {
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

  // 状态切换
  const handleSwitchChange = (row) => {
    const permission = {}
    permission.id = row.id
    permission.status = row.status
    permissionApi.update(permission.id,permission).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
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
        <el-form-item label="父级权限">
          <el-select v-model="permissionQuery.parentId" placeholder="请选择父级权限" clearable style="width: 220px">
            <el-option label="无" value=""></el-option>
            <el-option v-for="item in permissionList" :key="item" :label="item" :value="item"/>
          </el-select>
        </el-form-item>
        <el-form-item label="权限名">
          <el-input
              v-model="permissionQuery.name"
              placeholder="请输入权限名"
              clearable
              style="width: 220px"
              @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="权限值">
          <el-input
              v-model="permissionQuery.permissionValue"
              placeholder="请输入权限值"
              clearable
              style="width: 220px"
              @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
              v-model="permissionQuery.createTimeRange"
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
      <el-button type="primary" :icon="Plus" @click="showAddDialog">添加</el-button>
      <el-button type="danger" :icon="Delete" @click="deleteAll">批量删除</el-button>
    </div>
    <el-table :data="list" border style="width: 100%" ref="multipleTableRef" show-overflow-tooltip @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column fixed prop="id" label="ID" width="50"/>
      <el-table-column prop="parentId" label="父级权限" width="100"/>
      <el-table-column prop="name" label="权限名" width="120"/>
      <el-table-column prop="type" label="类型" width="100"/>
      <el-table-column prop="path" label="路径" width="120"/>
      <el-table-column prop="permissionValue" label="权限值" width="120"/>
      <el-table-column prop="icon" label="图标" width="100">
        <template #default="{row}">
          <!-- 数据库存的是图标名(如 "User"),全局注册后 <component :is> 按名字解析 -->
          <el-icon v-if="row.icon">
            <component :is="row.icon"/>
          </el-icon>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="50"/>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-switch
              v-model="row.status"
              style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
              :active-value="1"
              :inactive-value="0"
              inline-prompt
              active-text="正常"
              inactive-text="禁用"
              @change="handleSwitchChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="200"/>
      <el-table-column align="center" width="200px" fixed="right" label="操作">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="showUpdateDialog(row.id)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteById(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="permissionQuery.page"
          v-model:page-size="permissionQuery.limit"
          :page-sizes="[10, 20, 30, 40]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @change="loadData"
      />
    </div>
  </el-card>
  <!--添加、编辑弹出框-->
  <el-dialog v-model="dialogFormVisible" :title="title" width="500" :lock-scroll="false" :close-on-click-modal="false">
    <el-form :model="permission">
      <el-form-item label="名字" :label-width="80">
        <el-input v-model="permission.name" autocomplete="off" />
      </el-form-item>
      <el-form-item label="父级权限" :label-width="80">
        <el-select v-model="permission.parentId" placeholder="请选择父级权限" clearable style="width: 100%">
          <el-option label="无" value=""></el-option>
          <el-option v-for="item in permissionList" :key="item" :label="item.name" :value="item"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="类型" :label-width="80">
        <el-select v-model="permission.type" placeholder="请选择类型" clearable style="width: 100%">
          <el-option label="目录" value="0"></el-option>
          <el-option label="菜单" value="1"></el-option>
          <el-option label="按钮" value="2"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="路径" :label-width="80">
        <el-input v-model="permission.path" autocomplete="off" />
      </el-form-item>
      <el-form-item label="图标" :label-width="80">
        <el-input v-model="permission.icon" autocomplete="off" />
      </el-form-item>
      <el-form-item label="权限值" :label-width="80">
        <el-input v-model="permission.permissionValue" autocomplete="off" />
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

</style>