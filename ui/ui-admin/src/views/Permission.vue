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

  //保存返回树形结构数据，List<PermissionVO>
  const list = ref([])
  const loadData = () => {
    permissionApi.selectPermissionTree().then(result => {
      list.value = result.data
    })
  }

  loadData()

  /*let ids = []
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
  }*/

  /*const permission = ref({})
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
  }*/

  // 状态切换
  /*const handleSwitchChange = (row) => {
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
  }*/
</script>

<template>
  <el-button type="success" size="mini" @click="showAddDialog()">添加顶级菜单</el-button>
  <el-table :data="list" style="width: 100%; margin-bottom: 20px" row-key="id" border>
    <el-table-column prop="name" label="名称"/>
    <el-table-column prop="icon" width="80px" label="图标" #default="{row}">
      <el-icon><component :is="row.icon" /></el-icon>
    </el-table-column>
    <el-table-column prop="type" label="权限类型" #default="{row}">
      <el-tag v-if="row.type == 0">目录权限</el-tag>
      <el-tag v-if="row.type == 1" type="success">菜单权限</el-tag>
      <el-tag v-if="row.type == 2" type="warning">按钮权限</el-tag>
    </el-table-column>
    <el-table-column prop="path" label="路由地址"></el-table-column>
    <el-table-column prop="permissionValue" label="按钮权限"></el-table-column>
    <el-table-column prop="sort" label="排序"></el-table-column>
    <el-table-column label="操作" align="center" width="200px" fixed="right" #default="{row}">
      <el-button size="small" type="success" @click="showAddDialog(row)">添加</el-button>
      <el-button size="small" type="primary" @click="showUpdateDialog(row)">修改</el-button>
      <el-button size="small" type="danger" @click="deleteById(row.id)" :disabled="row.children?.length > 0">删除</el-button>
    </el-table-column>
  </el-table>
</template>

<style scoped>

</style>