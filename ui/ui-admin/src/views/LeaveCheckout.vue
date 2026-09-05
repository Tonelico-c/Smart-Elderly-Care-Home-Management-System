<script setup>

  import {ref} from "vue";
  import elderLeaveApi from "@/api/elderLeave.js";
  import elderApi from "@/api/elder.js";
  import {Search, Refresh} from "@element-plus/icons-vue";
  import {ElMessage} from "element-plus";

  const list = ref([])
  const total = ref(0)

  //本页只看请假中的记录，状态固定为1
  const elderLeaveQuery = ref({
    elderId: '',
    status: 1,
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
      status: 1,
      page: 1,
      limit: 10
    }
    loadData()
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
      <el-table-column align="center" width="100px" fixed="right" label="操作">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="showCheckoutDialog(row)" >销假</el-button>
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

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
