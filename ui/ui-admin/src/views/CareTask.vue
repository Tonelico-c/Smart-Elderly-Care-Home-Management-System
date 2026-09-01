<script setup>

  import {ref} from "vue";
  import careTaskApi from "@/api/careTask.js";
  import carePlanApi from "@/api/carePlan.js";
  import careItemApi from "@/api/careItem.js";
  import elderApi from "@/api/elder.js";
  import userApi from "@/api/user.js";
  import {Delete, Search, Refresh, Plus} from "@element-plus/icons-vue";
  import {ElMessage, ElMessageBox} from "element-plus";
  import hasBtnPermission from "@/utils/btnPermission.js";
  import {useTokenStore} from '@/store/token.js'
  const tokenStore = useTokenStore();

  const list = ref([])
  const total = ref(0)

  const careTaskQuery=ref({
    elderId:'',
    status:'',
    page:1,
    limit:10
  })

  const planExecuteDateRange = ref([]);
  const loadData = () =>{
    careTaskQuery.value.beginPlanExecuteDate = planExecuteDateRange.value?.[0];
    careTaskQuery.value.endPlanExecuteDate = planExecuteDateRange.value?.[1];
    careTaskApi.list(careTaskQuery.value).then(result => {
      list.value = result.data.records
      total.value = result.data.total
    })
  }
  loadData()

  // 下拉框数据：老人、护理人员、护理计划、护理项目
  const elderList = ref([])
  const userList = ref([])
  const carePlanList = ref([])
  const careItemList = ref([])
  const loadOptions = () => {
    elderApi.list({page: 1, limit: 1000}).then(result => {
      elderList.value = result.data.records
    })
    userApi.list({page: 1, limit: 1000}).then(result => {
      userList.value = result.data.records
    })
    carePlanApi.list({page: 1, limit: 1000}).then(result => {
      carePlanList.value = result.data.records
    })
    careItemApi.list({page: 1, limit: 1000}).then(result => {
      careItemList.value = result.data.records
    })
  }
  loadOptions()

  const elderName = (elderId) => elderList.value.find(elder => elder.id === elderId)?.name
  const userName = (userId) => userList.value.find(user => user.id === userId)?.name
  const carePlanName = (carePlanId) => carePlanList.value.find(carePlan => carePlan.id === carePlanId)?.name

  const onSearch = () => {
    careTaskQuery.value.page = 1
    loadData()
  }
  const resetSearch = () => {
    planExecuteDateRange.value = []
    careTaskQuery.value = {
      elderId:'',
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
      careTaskApi.deleteById(id).then(result => {
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
      careTaskApi.deleteBatch(ids).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    })
  }

  // 执行（本质是修改）
  const careTask = ref({})
  const dialogFormVisible = ref(false)
  const title = ref('')

  const showExecuteDialog = (id) => {
    title.value = '执行任务'
    dialogFormVisible.value = true
    careTaskApi.selectById(id).then(result => {
      careTask.value = result.data || {}
      // 数据库 time 类型返回 HH:mm:ss，统一截取为 HH:mm 供时间选择器使用
      if (careTask.value.planExecuteTime) {
        careTask.value.planExecuteTime = careTask.value.planExecuteTime.slice(0, 5)
      }
      // 已有现场照片时，回填上传列表（预览第一张）
      imgList.value = careTask.value.executeImg ? careTask.value.executeImg.split(',') : []
    })
  }
  const update = () => {
    careTaskApi.update(careTask.value.id,careTask.value).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        dialogFormVisible.value = false
        loadData()
      } else {
        ElMessage.error(result.msg)
      }
    })
  }

  // 现场照片上传成功，多个URL以逗号拼接
  const imgList = ref([])
  const handleImgSuccess = (res) => {
    imgList.value.push(res.data)
    careTask.value.executeImg = imgList.value.join(',')
  }

  // 状态选项
  const statusOptions = [
    {value: 0, label: '待执行'},
    {value: 1, label: '已完成'},
    {value: 2, label: '已跳过'},
  ]
</script>

<template>
  <el-card class="">
    <template #header>
      <el-form :inline="true" class="search-form" @submit.prevent>
        <el-form-item label="老人">
          <el-select v-model="careTaskQuery.elderId" clearable filterable placeholder="请选择老人" style="width: 150px">
            <el-option
                v-for="item in elderList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="careTaskQuery.status" clearable placeholder="请选择状态" style="width: 150px">
            <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="计划执行日期">
          <el-date-picker
              v-model="planExecuteDateRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </template>
    <div class="toolbar">
      <el-button type="danger" :icon="Delete" @click="deleteAll" >批量删除</el-button>
    </div>
    <el-table :data="list" border style="width: 100%" ref="multipleTableRef" show-overflow-tooltip @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column fixed prop="id" label="ID"/>
      <el-table-column label="老人" width="100">
        <template #default="{ row }">{{ elderName(row.elderId) }}</template>
      </el-table-column>
      <el-table-column label="来源计划" min-width="120">
        <template #default="{ row }">{{ carePlanName(row.carePlanId) }}</template>
      </el-table-column>
      <el-table-column prop="careItemName" label="护理项目" min-width="120"/>
      <el-table-column label="护理人员" width="100">
        <template #default="{ row }">{{ userName(row.userId) }}</template>
      </el-table-column>
      <el-table-column prop="planExecuteDate" label="计划执行日期" width="120"/>
      <el-table-column label="计划执行时间" width="120">
        <template #default="{ row }">{{ row.planExecuteTime ? row.planExecuteTime.slice(0, 5) : '' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : (row.status === 2 ? 'info' : 'warning')">
            {{ statusOptions.find(item => item.value === row.status)?.label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="actualExecuteTime" label="实际完成时间" width="200px"/>
      <el-table-column prop="executeResult" label="执行结果" min-width="120"/>
      <el-table-column prop="remark" label="备注" min-width="120"/>
      <el-table-column prop="createTime" label="创建时间" width="200px"/>
      <el-table-column align="center" width="200px" fixed="right" label="操作">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="showExecuteDialog(row.id)" >执行</el-button>
          <el-button size="small" type="danger" @click="deleteById(row.id)" >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="careTaskQuery.page"
          v-model:page-size="careTaskQuery.limit"
          :page-sizes="[10, 20, 30, 40]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @change="loadData"
      />
    </div>
  </el-card>

  <!--执行任务弹出框-->
  <el-dialog v-model="dialogFormVisible" :title="title" width="500" :lock-scroll="false" :close-on-click-modal="false">
    <el-form :model="careTask">
      <el-form-item label="老人" :label-width="90">
        <el-select v-model="careTask.elderId" filterable placeholder="请选择老人" disabled>
          <el-option
              v-for="item in elderList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="来源计划" :label-width="90">
        <el-select v-model="careTask.carePlanId" filterable placeholder="请选择护理计划" disabled>
          <el-option
              v-for="item in carePlanList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="护理项目" :label-width="90">
        <el-select v-model="careTask.careItemId" filterable placeholder="请选择护理项目" disabled>
          <el-option
              v-for="item in careItemList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="计划执行日期" :label-width="90">
        <el-date-picker
            v-model="careTask.planExecuteDate"
            type="date"
            placeholder="请选择计划执行日期"
            value-format="YYYY-MM-DD"
            disabled
        />
      </el-form-item>
      <el-form-item label="计划执行时间" :label-width="90">
        <el-time-picker
            v-model="careTask.planExecuteTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="选择时间"
            disabled
        />
      </el-form-item>
      <el-form-item label="护理人员" :label-width="90">
        <el-select v-model="careTask.userId" filterable placeholder="请选择护理人员" clearable>
          <el-option
              v-for="item in userList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" :label-width="90">
        <el-select v-model="careTask.status">
          <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="实际完成时间" :label-width="90">
        <el-date-picker
            v-model="careTask.actualExecuteTime"
            type="datetime"
            placeholder="请选择实际完成时间"
            value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item label="执行结果" :label-width="90">
        <el-input v-model="careTask.executeResult" type="textarea" autocomplete="off" />
      </el-form-item>
      <el-form-item label="现场照片" :label-width="90">
        <el-upload
            class="img-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleImgSuccess"
            :headers="{Authorization: tokenStore.token}"
        >
          <img v-if="careTask.executeImg" :src="careTask.executeImg.split(',')[0]" class="task-img"/>
          <el-icon v-else class="img-uploader-icon">
            <Plus/>
          </el-icon>
        </el-upload>
      </el-form-item>
      <el-form-item label="备注" :label-width="90">
        <el-input v-model="careTask.remark" type="textarea" autocomplete="off" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="update">
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

/* 现场照片上传：样式与老人表头像上传类似 */
.img-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.img-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.img-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
}

.task-img {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
  border-radius: 6px;
}
</style>
