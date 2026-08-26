<script setup>
  import {ref} from "vue";
  import tagApi from "@/api/tag.js";
  import {ElMessage, ElMessageBox} from "element-plus";

  const list = ref([])
  const total = ref(0)
  const tagQuery = ref({
    code: '',
    page: 1,
    limit: 10
  })

  // 搜索
  const createTimeRange = ref([])
  const localData = () => {
    tagQuery.value.beginCreateTime = createTimeRange.value?.[0];
    tagQuery.value.endCreateTime = createTimeRange.value?.[1];
    tagApi.list(tagQuery.value).then(result => {
      list.value = result.data.records
      total.value = result.data.total
    })
  }
  localData()
  const onSearch = () => {
    tagQuery.value.page = 1
    localData()
  }

  // 删除
  let ids = []
  const handleSelectionChange = (rows) => {
    console.log(rows)
    ids = rows.map(row => row.id)
  }
  const deleteById = id => {
    ElMessageBox.confirm('确定删除该标签吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      tagApi.deleteById(id).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          localData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    })
  }
  const deleteAll = () => {
    ElMessageBox.confirm('确定删除所选标签吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      tagApi.deleteBatch(ids).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          localData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }).catch(()=>{})
  }

  // 添加、编辑弹出框
  const tag = ref({})
  const dialogFormVisible = ref(false)
  const title = ref()
  const showAddDialog = () => {
    tag.value = {}
    dialogFormVisible.value = true
  }
  const showUpdateDialog = id => {
    tagApi.selectById(id).then(result => {
      tag.value = result.data
      dialogFormVisible.value = true
    })
  }
  const addOrUpdate = () => {
    if(tag.value.id){
      tagApi.update(tag.value.id, tag.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          localData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    } else {
      tagApi.add(tag.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          localData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }
  }
</script>

<template>
  <el-card class="">
    <template #header>
      <div class="header">
        <el-button type="primary" @click="showAddDialog">添加</el-button>
        <el-button type="danger" @click="deleteAll">批量删除</el-button>
      </div>
    </template>
    <el-form :inline="true">
      <el-form-item label="标签码">
        <el-input v-model="tagQuery.code" placeholder="请输入标签码" clearable style="width: 150px"/>
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
        <el-button type="primary" @click="onSearch">搜索</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="list" border style="width: 100%" ref="multipleTableRef" show-overflow-tooltip @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column fixed prop="id" label="ID"/>
      <el-table-column prop="code" label="标签编码"/>
      <el-table-column prop="name" label="标签名称"/>
      <el-table-column prop="createTime" label="创建时间" width="200px"/>
      <el-table-column align="center" width="200px" fixed="right" label="操作">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="showUpdateDialog(row.id)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteById(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
        v-model:current-page="tagQuery.page"
        v-model:page-size="tagQuery.limit"
        :page-sizes="[10, 20, 30, 40]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @change="localData"
        style="margin-top: 20px; justify-content: flex-end"
    />
  </el-card>
  <!--添加、编辑弹出框-->
  <el-dialog v-model="dialogFormVisible" :title="title" width="500" :lock-scroll="false" :close-on-click-modal="false">
    <el-form :model="tag">
      <el-form-item label="名字" :label-width="60">
        <el-input v-model="tag.name" autocomplete="off" />
      </el-form-item>
      <el-form-item label="编码" :label-width="60">
        <el-input v-model="tag.code" autocomplete="off" />
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