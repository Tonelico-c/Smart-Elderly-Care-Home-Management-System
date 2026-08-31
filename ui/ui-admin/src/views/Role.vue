<script setup>

  import {Delete, Download, Plus, Refresh, Search, Upload} from "@element-plus/icons-vue";
  import {ref} from "vue";
  import roleApi from "@/api/role.js";
  import {ElMessage, ElMessageBox} from "element-plus";

  const roleQuery = ref({
    name:'',
    code:'',
    page:1,
    limit:10
  })
  const list = ref([])
  const total = ref(0)

  const createTimeRange = ref([])
  const loadData = () => {
    roleQuery.value.beginCreateTime = createTimeRange.value?.[0]
    roleQuery.value.endCreateTime = createTimeRange.value?.[1]
    roleApi.list(roleQuery.value).then(result => {
      list.value = result.data.records
      total.value = result.data.total
    })
  }
  loadData()
  const onSearch = () => {
    roleQuery.value.page = 1
    loadData()
  }
  const resetSearch = () => {
    roleQuery.value = {
      name:'',
      code:'',
      page:1,
      limit:10
    }
    createTimeRange.value = []
    loadData()
  }

  let ids = []
  const handleSelectionChange = (rows) => {
    ids = rows.map(row => row.id)
    console.log(ids)
  }
  const deleteById = id => {
    ElMessageBox.confirm(
        '您确认要删除么?',
        '警告',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
        }
    ).then(() => {
      roleApi.deleteById(id).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      }).catch(() => {})
    })
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
      roleApi.deleteBatch(ids).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      }).catch(() => {})
    })
  }

  const role = ref({})
  const title = ref()
  const dialogFormVisible = ref(false)
  const showAddDialog = () => {
    title.value = '添加'
    dialogFormVisible.value = true
    role.value = {}
  }
  const showUpdateDialog = id => {
    title.value = '编辑'
    dialogFormVisible.value = true
    roleApi.selectById(id).then(result => {
      role.value = result.data
    })
  }
  // 添加或更新
  const addOrUpdate = () => {
    if(role.value.id){
      roleApi.update(role.value.id,role.value).then(result => {
        if(result.code === 1){
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadData()
        }else{
          ElMessage.error(result.msg)
        }
      })
    }else{
      roleApi.add(role.value).then(result => {
        if(result.code === 1){
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadData()
        }else{
          ElMessage.error(result.msg)
        }
      })
    }
  }

  //权限分配的树形结构
  const dialogPermissionVisible = ref(false)
  const treeData = ref([])
  const treeRef = ref()
  const defaultProps = ref({
    children: 'children',
    label: 'name'
  })
  const showAssignedPermissionDialog = (row) => {
    role.value = row;
    dialogPermissionVisible.value = true;
    treeData.value = [];
    roleApi.selectAssignedPermission(row.id).then((result) => {
      if (result.code === 1) {
        treeData.value = result.data.permissionVOList;
        let checkedLeafIdList = [];
        //找到所有这个角色已经分配的权限里面的叶子权限
        getCheckedLeafIdList(result.data.permissionVOList, result.data.assignedPermissionIds, checkedLeafIdList);
        treeRef.value.setCheckedKeys(checkedLeafIdList);
      }
    });
  }

  const getCheckedLeafIdList = (permissionVOList, assignedPermissionIds, checkedLeafIdList) => {
    permissionVOList.forEach(permissionVO => {
      assignedPermissionIds.forEach(id => {
        //这个角色下面的权限，而且是没有孩子的叶子节点
        if (permissionVO.id===id && permissionVO.children.length === 0) {
          checkedLeafIdList.push(id);
        } else if(permissionVO.id===id && permissionVO.children.length !== 0) {
          getCheckedLeafIdList(permissionVO.children, assignedPermissionIds, checkedLeafIdList);
        }
      })
    });
  }

  const assignPermission = () => {
    let checkedNodes = treeRef.value.getCheckedNodes(false, true);
    console.log(checkedNodes);
    let permissionIds = checkedNodes.map((node) => node.id);
    permissionIds = permissionIds.join(',');
    roleApi.assignPermission(role.value.id, permissionIds).then((result) => {
      if (result.code === 1) {
        ElMessage({message: result.msg, type: 'success',})
        dialogPermissionVisible.value = false;
      }
    });
  }
</script>

<template>
  <el-card class="">
    <template #header>
      <el-form :inline="true" class="search-form" @submit.prevent>
        <el-form-item label="名字">
          <el-input
              v-model="roleQuery.name"
              placeholder="请输入名字"
              clearable
              style="width: 220px"
              @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="编码">
          <el-input
              v-model="roleQuery.code"
              placeholder="请输入编码"
              clearable
              style="width: 220px"
              @keyup.enter="onSearch"
          />
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
      <el-button type="primary" :icon="Plus" @click="showAddDialog">添加</el-button>
      <el-button type="danger" :icon="Delete" @click="deleteAll">批量删除</el-button>
    </div>
    <el-table :data="list" border style="width: 100%" ref="multipleTableRef" show-overflow-tooltip @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column fixed prop="id" label="ID" width="50"/>
      <el-table-column prop="name" label="名字" width="150"/>
      <el-table-column prop="code" label="编码" width="150"/>
      <el-table-column prop="description" label="权限描述" width="400"/>
      <el-table-column prop="createTime" label="创建时间" width="200"/>
      <el-table-column align="center" width="250" fixed="right" label="操作">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="showUpdateDialog(row.id)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteById(row.id)">删除</el-button>
          <el-button size="small" type="success" @click="showAssignedPermissionDialog(row)">权限</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="roleQuery.page"
          v-model:page-size="roleQuery.limit"
          :page-sizes="[10, 20, 30, 40]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @change="loadData"
      />
    </div>
  </el-card>
  <!--添加、编辑弹出框-->
  <el-dialog v-model="dialogFormVisible" :title="title" width="500" :lock-scroll="false" :close-on-click-modal="false">
    <el-form :model="role">
      <el-form-item label="名字" :label-width="60">
        <el-input v-model="role.name" autocomplete="off" />
      </el-form-item>
      <el-form-item label="编码" :label-width="60">
        <el-input v-model="role.code" autocomplete="off" />
      </el-form-item>
      <el-form-item label="权限描述" :label-width="60">
        <el-input v-model="role.description" autocomplete="off" />
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
  <!--    权限分配的dialog-->
  <el-dialog
      title="分配权限"
      v-model="dialogPermissionVisible"
      width="40%" :lock-scroll="false">
    <el-tree
        :data="treeData"
        ref="treeRef"
        show-checkbox
        node-key="id"
        default-expand-all
        :props="defaultProps">
    </el-tree>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="assignPermission()">保存</el-button>
        <el-button  @click="dialogPermissionVisible = false">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
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
</style>