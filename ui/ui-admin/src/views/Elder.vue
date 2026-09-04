<script setup>

  import {ref} from "vue";
  import elderApi from "@/api/elder.js";
  import {Delete, Plus, Search, Refresh, Download} from "@element-plus/icons-vue";
  import {ElMessage, ElMessageBox} from "element-plus";
  import {useTokenStore} from '@/store/token.js'
  import defaultAvatar from '@/assets/default.png'
  import tagApi from "@/api/tag.js";
  import hasBtnPermission from "@/utils/btnPermission.js";
  const tokenStore = useTokenStore();

  // 存储所有标签的Id
  const allTags = ref([])
  tagApi.list({page: 1, limit: 1000}).then(result => {
    allTags.value = result.data.records
  })
  const selectedTagIds = ref([])

  const list = ref([])
  const total = ref(0)

  const elderQuery=ref({
    name:'',
    phone:'',
    page:1,
    limit:10
  })

  const createTimeRange = ref([]);
  const loadData = () =>{
    elderQuery.value.beginCreateTime = createTimeRange.value?.[0];
    elderQuery.value.endCreateTime = createTimeRange.value?.[1];
    elderQuery.value.tagIds = selectedTagIds.value.join(',')
    elderApi.list(elderQuery.value).then(result => {
      list.value = result.data.records
      total.value = result.data.total
    })
  }
  loadData()

  const onSearch = () => {
    elderQuery.value.page = 1
    loadData()
  }
  const resetSearch = () => {
    createTimeRange.value = []
    selectedTagIds.value = []
    elderQuery.value = {
      name:'',
      phone:'',
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
      elderApi.deleteById(id).then(result => {
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
      elderApi.deleteBatch(ids).then(result => {
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
  const elder = ref({})
  const dialogFormVisible = ref(false)
  const title = ref('')

  const showAddDialog = () => {
    title.value = '添加'
    dialogFormVisible.value = true
    elder.value = {}
  }
  const showUpdateDialog = (id) => {
    title.value = '修改'
    dialogFormVisible.value = true
    elderApi.selectById(id).then(result => {
      elder.value = result.data
    })
  }
  const addOrUpdate = () => {
    if(elder.value.id){
      elderApi.update(elder.value.id,elder.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }else {
      elderApi.add(elder.value).then(result => {
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

  // 头像上传成功
  const handleAvatarSuccess = (res) => {
      elder.value.avatar = res.data
  }

  // 状态选项
  const statusOptions = [
    {value: 0, label: '禁用'},
    {value: 1, label: '启用'},
    {value: 2, label: '请假'},
    {value: 3, label: '退住中'},
    {value: 4, label: '入住中'},
    {value: 5, label: '已退住'},
  ]

  // 表格中修改状态
  const handleStatusChange = (row) => {
    elderApi.update(row.id, row).then(result => {
      if (result.code === 1) {
        ElMessage.success('修改状态成功')
      } else {
        ElMessage.error(result.msg)
        loadData()
      }
    })
  }

  const dialogTagVisible = ref(false)
  const tagList = ref([])
  const assignedTagIdList = ref([])
  // 显示已分配标签对话框
  const showAssignedTagDialog = (row) => {
    elder.value = row
    elderApi.selectAssignedTag(row.id).then(result => {
      tagList.value = result.data.tagList
      assignedTagIdList.value = result.data.assignedTagIdList
      dialogTagVisible.value = true
    })
  }
  // 分配标签
  const assignTag = () => {
    // /elders/assignTag?elderId=1&tagIds=1,2,3
    const tagIds = assignedTagIdList.value.join(',');
    elderApi.assignTag(elder.value.id, tagIds).then((result) => {
      if (result.code === 1) {
        ElMessage.success(result.msg);
        dialogTagVisible.value = false;
        loadData();
      } else {
        ElMessage.error(result.msg);
      }
    });
  }

  // 导出Excel
  const exportExcel = () => {
    elderApi.exportExcel().then((result) => {
      //从响应头 Content-Disposition 解析后端返回的文件名,后端做过 URLEncoder.encode,需要解码
      const disposition = result.headers['content-disposition'];
      let fileName = '导出数据.xlsx'; //兜底名
      if (disposition) {
        fileName = decodeURIComponent(disposition.split('filename=')[1]);
      }
      //responseType 为 blob 时 result.data 本身就是 Blob,直接用即可
      let url = window.URL.createObjectURL(result.data);
      const link = document.createElement("a"); // 创建a标签
      link.href = url;
      link.download = fileName; // 使用后端返回的文件名
      link.click();
      URL.revokeObjectURL(url);
    });
  }
</script>

<template>
  <el-card class="">
    <template #header>
      <el-form :inline="true" class="search-form" @submit.prevent>
        <el-form-item label="名字">
          <el-input
              v-model="elderQuery.name"
              placeholder="请输入名字"
              clearable
              style="width: 220px"
              @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="电话">
          <el-input
              v-model="elderQuery.phone"
              placeholder="请输入电话"
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
        <el-form-item label="标签">
          <el-select v-model="selectedTagIds" class="tag-select" multiple clearable collapse-tags
                     :max-collapse-tags="3" collapse-tags-tooltip
                     placeholder="请选择标签" style="width: 320px">
            <el-option v-for="tag in allTags" :key="tag.id"
                       :label="tag.name" :value="tag.id"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </template>
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="showAddDialog" :disabled="!hasBtnPermission('elder:add')">添加</el-button>
      <el-button type="danger" :icon="Delete" @click="deleteAll" :disabled="!hasBtnPermission('elder:delete')">批量删除</el-button>
      <el-button type="success" :icon="Download" @click="exportExcel">导出为Excel</el-button>
    </div>
    <el-table :data="list" border style="width: 100%" ref="multipleTableRef" show-overflow-tooltip @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column fixed prop="id" label="ID"/>
      <el-table-column prop="avatar" label="头像">
        <template #default="{row}">
          <img :src="row.avatar || defaultAvatar" alt="" class="table-avatar">
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名字" width="100"/>
      <el-table-column prop="idCardNo" label="身份证号" width="150"/>
      <el-table-column prop="birthday" label="出生日期" width="120"/>
      <el-table-column prop="phone" label="电话" width="120"/>
      <el-table-column prop="address" label="家庭地址" width="150"/>
      <el-table-column prop="status" label="状态" width="150">
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
      <el-table-column prop="tagNames" label="标签" width="200">
        <template #default="{ row }">
          <el-tag v-for="name in (row.tagNames || [])" :key="name"
                  type="primary" style="margin: 2px">{{ name }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="创建时间" width="200px"/>
      <el-table-column align="center" width="200px" fixed="right" label="操作">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="showUpdateDialog(row.id)" :disabled="!hasBtnPermission('elder:update')">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteById(row.id)"  :disabled="!hasBtnPermission('elder:delete')">删除</el-button>
          <el-button size="small" type="success" @click="showAssignedTagDialog(row)" :disabled="!hasBtnPermission('elder:setTag')">标签</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="elderQuery.page"
          v-model:page-size="elderQuery.limit"
          :page-sizes="[10, 20, 30, 40]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @change="loadData"
      />
    </div>
  </el-card>

  <!--添加、编辑弹出框-->
  <el-dialog v-model="dialogFormVisible" :title="title" width="500" :lock-scroll="false" :close-on-click-modal="false">
    <el-form :model="elder">
      <el-form-item label="名字" :label-width="80">
        <el-input v-model="elder.name" autocomplete="off" />
      </el-form-item>
      <el-form-item label="密码" :label-width="80">
        <el-input v-model="elder.password" autocomplete="off" />
      </el-form-item>
      <el-form-item label="身份证号" :label-width="80">
        <el-input v-model="elder.idCardNo" autocomplete="off" />
      </el-form-item>
      <el-form-item label="手机号" :label-width="80">
        <el-input v-model="elder.phone" autocomplete="off" />
      </el-form-item>
      <el-form-item label="地址" :label-width="80">
        <el-input v-model="elder.address" autocomplete="off" />
      </el-form-item>
      <el-form-item label="出生日期" :label-width="80">
        <el-date-picker
            v-model="elder.birthday"
            type="date"
            placeholder="Pick a date"
            value-format="YYYY-MM-DD"
            clearable
        />
      </el-form-item>
      <el-form-item label="头像" :label-width="60">
        <el-upload
            class="avatar-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :headers="{Authorization: tokenStore.token}"
        >
          <img v-if="elder.avatar" :src="elder.avatar" class="avatar"/>
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
  <!-- 标签分配dialog-->
  <el-dialog title="分配标签" v-model="dialogTagVisible" width="40%">
    <el-form ref="form" :model="elder" label-width="80px">
      <el-form-item label="老人姓名">
        <el-input v-model="elder.name" disabled></el-input>
      </el-form-item>
      <el-form-item label="标签列表">
        <el-checkbox-group v-model="assignedTagIdList">
          <el-checkbox v-for="tag in tagList" :key="tag.id" :label="tag.id">{{tag.name}}</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="assignTag">保存</el-button>
        <el-button  @click="dialogTagVisible = false">取消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<style scoped>
/* 标签下拉框：禁止标签换行，避免选多了把搜索表单整行撑高 */
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

.table-avatar {
  width: 50px;
  height: 50px;
  display: block;
  border-radius: 50%;
  object-fit: cover;
  margin: 0 auto;
}

.tag-select :deep(.el-select__selection) {
  flex-wrap: nowrap;
  overflow: hidden;
  /* 未选标签时占位符是绝对定位，容器高度会塌成 0 被 overflow 裁掉，给它一个最小高度 */
  min-height: 24px;
}
/* 放不下的标签内容用省略号截断，保证 +N 折叠标签不被挤掉 */
.tag-select :deep(.el-tag__content) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 50%;
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
  border-radius: 50%;
}
</style>