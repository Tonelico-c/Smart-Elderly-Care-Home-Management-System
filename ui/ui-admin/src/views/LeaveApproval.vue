<script setup>

  import {ref} from "vue";
  import elderLeaveApi from "@/api/elderLeave.js";
  import elderApi from "@/api/elder.js";
  import {Search, Refresh} from "@element-plus/icons-vue";
  import {ElMessage} from "element-plus";

  const list = ref([])
  const total = ref(0)

  //本页只看待审批的记录，状态固定为0
  const elderLeaveQuery = ref({
    elderId: '',
    status: 0,
    page: 1,
    limit: 10
  })

  const loadData = () => {
    elderLeaveApi.list(elderLeaveQuery.value).then(result => {
      list.value = result.data?.records || []
      total.value = result.data?.total || 0
    })
  }
  loadData()

  // 老人选项
  const elderOptions = ref([])
  const loadElderOptions = () => {
    elderApi.list({page: 1, limit: 1000}).then(result => {
      elderOptions.value = result.data.records || []
    })
  }
  loadElderOptions()

  const onSearch = () => {
    elderLeaveQuery.value.page = 1
    loadData()
  }
  const resetSearch = () => {
    elderLeaveQuery.value = {
      elderId: '',
      status: 0,
      page: 1,
      limit: 10
    }
    loadData()
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
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </template>
    <el-table :data="list" border style="width: 100%" show-overflow-tooltip>
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
      <el-table-column prop="createTime" label="申请时间" width="180px"/>
      <el-table-column align="center" width="100px" fixed="right" label="操作">
        <template #default="{ row }">
          <el-button size="small" type="warning" @click="showApproveDialog(row)" >审批</el-button>
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

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
