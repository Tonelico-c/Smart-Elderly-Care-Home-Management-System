<script setup>

  import {computed, ref} from "vue";
  import elderLeaveApi from "@/api/elderLeave.js";
  import elderApi from "@/api/elder.js";
  import {Delete, Plus, Search, Refresh} from "@element-plus/icons-vue";
  import {ElMessage, ElMessageBox} from "element-plus";

  const list = ref([])
  const total = ref(0)

  const elderLeaveQuery=ref({
    elderId:'',
    status:'',
    page:1,
    limit:10
  })

  const loadData = () =>{
    elderLeaveApi.list(elderLeaveQuery.value).then(result => {
      list.value = result.data?.records || []
      total.value = result.data?.total || 0
    })
  }
  loadData()

  // 老人选项（搜索栏用全部老人）
  const elderOptions = ref([])
  const loadElderOptions = () => {
    elderApi.list({page:1, limit:1000}).then(result => {
      elderOptions.value = result.data.records || []
    })
  }
  loadElderOptions()

  // 可请假老人选项（添加弹窗用）：只有入住中（状态4）的老人可以请假
  const checkedInElderOptions = computed(() => elderOptions.value.filter(item => item.status === 4))

  const onSearch = () => {
    elderLeaveQuery.value.page = 1
    loadData()
  }
  const resetSearch = () => {
    elderLeaveQuery.value = {
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
      elderLeaveApi.deleteById(id).then(result => {
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
      elderLeaveApi.deleteBatch(ids).then(result => {
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
  const elderLeave = ref({})
  const dialogFormVisible = ref(false)
  const title = ref('')

  const showAddDialog = () => {
    title.value = '添加'
    dialogFormVisible.value = true
    elderLeave.value = {}
  }
  const showUpdateDialog = (id) => {
    title.value = '修改'
    dialogFormVisible.value = true
    elderLeaveApi.selectById(id).then(result => {
      elderLeave.value = result.data || {}
    })
  }
  const addOrUpdate = () => {
    if(elderLeave.value.id){
      elderLeaveApi.update(elderLeave.value.id,elderLeave.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }else {
      elderLeaveApi.add(elderLeave.value).then(result => {
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

  // 状态选项：0待审批 1请假中 2已销假 3已驳回
  const statusOptions = [
    {value: 0, label: '待审批'},
    {value: 1, label: '请假中'},
    {value: 2, label: '已销假'},
    {value: 3, label: '已驳回'},
  ]
  const statusTagType = (status) => {
    const map = {0: 'info', 1: 'warning', 2: 'success', 3: 'danger'}
    return map[status] || 'info'
  }

  // ===== 审批 =====
  const approveDialogVisible = ref(false)
  const currentLeave = ref({})
  const rejectReason = ref('')

  const showApproveDialog = (row) => {
    currentLeave.value = row
    rejectReason.value = ''
    approveDialogVisible.value = true
  }

  //审批人id不用前端传,后端会从token里解析当前登录用户
  const approve = () => {
    elderLeaveApi.approve(currentLeave.value.id).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        approveDialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(result.msg)
      }
    })
  }

  const reject = () => {
    if (!rejectReason.value) {
      ElMessage.warning('请填写驳回理由')
      return
    }
    elderLeaveApi.reject(currentLeave.value.id, rejectReason.value).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        approveDialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(result.msg)
      }
    })
  }

  // ===== 销假 =====
  const checkoutDialogVisible = ref(false)
  const checkoutForm = ref({})

  const showCheckoutDialog = (row) => {
    checkoutForm.value = {
      id: row.id,
      //默认当前时间,可修改
      actualReturnTime: formatNow()
    }
    checkoutDialogVisible.value = true
  }

  const submitCheckout = () => {
    elderLeaveApi.checkout(checkoutForm.value.id, checkoutForm.value.actualReturnTime).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        checkoutDialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(result.msg)
      }
    })
  }

  // 时间格式化
  const formatTime = (time) => {
    return time || '-'
  }

  // 当前时间格式化为 yyyy-MM-dd HH:mm:ss
  const formatNow = () => {
    const pad = (n) => String(n).padStart(2, '0')
    const d = new Date()
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  }
</script>

<template>
  <el-card class="">
    <template #header>
      <el-form :inline="true" class="search-form" @submit.prevent>
        <el-form-item label="老人">
          <el-select v-model="elderLeaveQuery.elderId" clearable filterable placeholder="请选择老人" style="width: 180px">
            <el-option
                v-for="item in elderOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="elderLeaveQuery.status" clearable placeholder="请选择状态" style="width: 150px">
            <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
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
      <el-table-column label="老人" width="120">
        <template #default="{ row }">
          {{ elderOptions.find(item => item.id === row.elderId)?.name || row.elderId }}
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="请假事由" min-width="120"/>
      <el-table-column prop="destination" label="外出去向" min-width="120"/>
      <el-table-column prop="contactPhone" label="联系电话" width="130"/>
      <el-table-column prop="beginTime" label="预计外出时间" width="180px"/>
      <el-table-column prop="endTime" label="预计返回时间" width="180px"/>
      <el-table-column prop="actualReturnTime" label="实际返回时间" width="180px">
        <template #default="{ row }">
          {{ formatTime(row.actualReturnTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">
            {{ statusOptions.find(item => item.value === row.status)?.label || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="rejectReason" label="驳回理由" min-width="120"/>
      <el-table-column label="审批人" width="100">
        <template #default="{ row }">
          {{ row.approverName || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180px"/>
      <el-table-column align="center" width="260px" fixed="right" label="操作">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" size="small" type="warning" @click="showApproveDialog(row)" >审批</el-button>
          <el-button v-if="row.status === 1" size="small" type="success" @click="showCheckoutDialog(row)" >销假</el-button>
          <el-button :disabled="row.status !== 0" size="small" type="primary" @click="showUpdateDialog(row.id)" >编辑</el-button>
          <el-button size="small" type="danger" @click="deleteById(row.id)"  >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="elderLeaveQuery.page"
          v-model:page-size="elderLeaveQuery.limit"
          :page-sizes="[10, 20, 30, 40]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @change="loadData"
      />
    </div>
  </el-card>

  <!--添加、编辑弹出框-->
  <el-dialog v-model="dialogFormVisible" :title="title" width="560" :lock-scroll="false" :close-on-click-modal="false">
    <el-form :model="elderLeave">
      <el-form-item label="老人" :label-width="100">
        <el-select v-model="elderLeave.elderId" filterable placeholder="请选择老人（仅入住中）">
          <el-option
              v-for="item in checkedInElderOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="请假事由" :label-width="100">
        <el-input v-model="elderLeave.reason" placeholder="请输入请假事由" autocomplete="off" />
      </el-form-item>
      <el-form-item label="外出去向" :label-width="100">
        <el-input v-model="elderLeave.destination" placeholder="请输入外出去向" autocomplete="off" />
      </el-form-item>
      <el-form-item label="联系电话" :label-width="100">
        <el-input v-model="elderLeave.contactPhone" placeholder="请输入外出期间联系电话" autocomplete="off" />
      </el-form-item>
      <el-form-item label="预计外出时间" :label-width="100">
        <el-date-picker
            v-model="elderLeave.beginTime"
            type="datetime"
            placeholder="请选择预计外出时间"
            value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item label="预计返回时间" :label-width="100">
        <el-date-picker
            v-model="elderLeave.endTime"
            type="datetime"
            placeholder="请选择预计返回时间"
            value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>

      <el-form-item label="状态" :label-width="100">
        <el-tag type="info">待审批</el-tag>
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

  <!--审批弹出框-->
  <el-dialog v-model="approveDialogVisible" title="审批请假申请" width="560" :lock-scroll="false" :close-on-click-modal="false">
    <!--该请假申请的重要信息-->
    <el-descriptions :column="1" border>
      <el-descriptions-item label="老人">
        {{ currentLeave.elderName || elderOptions.find(item => item.id === currentLeave.elderId)?.name || currentLeave.elderId }}
      </el-descriptions-item>
      <el-descriptions-item label="请假事由">{{ currentLeave.reason }}</el-descriptions-item>
      <el-descriptions-item label="外出去向">{{ currentLeave.destination || '-' }}</el-descriptions-item>
      <el-descriptions-item label="联系电话">{{ currentLeave.contactPhone || '-' }}</el-descriptions-item>
      <el-descriptions-item label="预计外出时间">{{ currentLeave.beginTime }}</el-descriptions-item>
      <el-descriptions-item label="预计返回时间">{{ currentLeave.endTime }}</el-descriptions-item>
    </el-descriptions>
    <!--驳回时必填-->
    <el-form style="margin-top: 16px">
      <el-form-item label="驳回理由" :label-width="100">
        <el-input
            v-model="rejectReason"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="驳回时必填,通过时可留空"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="reject">驳回</el-button>
        <el-button type="primary" @click="approve">通过</el-button>
      </div>
    </template>
  </el-dialog>

  <!--销假弹出框-->
  <el-dialog v-model="checkoutDialogVisible" title="销假" width="500" :lock-scroll="false" :close-on-click-modal="false">
    <el-form :model="checkoutForm">
      <el-form-item label="实际返回时间" :label-width="100">
        <el-date-picker
            v-model="checkoutForm.actualReturnTime"
            type="datetime"
            placeholder="不填默认当前时间"
            value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="checkoutDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCheckout">确认销假</el-button>
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
