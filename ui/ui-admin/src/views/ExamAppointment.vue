<script setup>

  import {ref} from "vue";
  import examAppointmentApi from "@/api/examAppointment.js";
  import elderApi from "@/api/elder.js";
  import examPackageApi from "@/api/examPackage.js";
  import {Delete, Plus, Search, Refresh} from "@element-plus/icons-vue";
  import {ElMessage, ElMessageBox} from "element-plus";

  const list = ref([])
  const total = ref(0)

  const examAppointmentQuery=ref({
    elderId:'',
    packageId:'',
    status:'',
    page:1,
    limit:10
  })

  const createTimeRange = ref([]);
  const loadData = () =>{
    examAppointmentQuery.value.beginCreateTime = createTimeRange.value?.[0];
    examAppointmentQuery.value.endCreateTime = createTimeRange.value?.[1];
    examAppointmentApi.list(examAppointmentQuery.value).then(result => {
      list.value = result.data.records
      total.value = result.data.total
    })
  }
  loadData()

  // 老人选项
  const elderOptions = ref([])
  const loadElderOptions = () => {
    elderApi.list({page:1, limit:1000}).then(result => {
      elderOptions.value = result.data.records || []
    })
  }
  loadElderOptions()

  // 套餐选项
  const packageOptions = ref([])
  const loadPackageOptions = () => {
    examPackageApi.list({page:1, limit:1000, status:1}).then(result => {
      packageOptions.value = result.data.records || []
    })
  }
  loadPackageOptions()

  const onSearch = () => {
    examAppointmentQuery.value.page = 1
    loadData()
  }
  const resetSearch = () => {
    createTimeRange.value = []
    examAppointmentQuery.value = {
      elderId:'',
      packageId:'',
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
      examAppointmentApi.deleteById(id).then(result => {
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
      examAppointmentApi.deleteBatch(ids).then(result => {
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
  const examAppointment = ref({})
  const dialogFormVisible = ref(false)
  const title = ref('')

  const showAddDialog = () => {
    title.value = '添加'
    dialogFormVisible.value = true
    examAppointment.value = {}
  }
  const showUpdateDialog = (id) => {
    title.value = '修改'
    dialogFormVisible.value = true
    examAppointmentApi.selectById(id).then(result => {
      examAppointment.value = result.data || {}
    })
  }
  const addOrUpdate = () => {
    if(examAppointment.value.id){
      examAppointmentApi.update(examAppointment.value.id,examAppointment.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }else {
      examAppointmentApi.add(examAppointment.value).then(result => {
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

  // 选择套餐时自动填充价格
  const onPackageChange = (packageId) => {
    const pkg = packageOptions.value.find(item => item.id === packageId)
    if (pkg) {
      examAppointment.value.price = pkg.price
    }
  }

  // 状态选项：0待体检 1体检中 2已完成 3已取消 4已过期
  const statusOptions = [
    {value: 0, label: '待体检'},
    {value: 1, label: '体检中'},
    {value: 2, label: '已完成'},
    {value: 3, label: '已取消'},
    {value: 4, label: '已过期'},
  ]
  const statusTagType = (status) => {
    const map = {0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'info'}
    return map[status] || 'info'
  }

  // 体检结果
  const resultDialogVisible = ref(false)
  const resultItems = ref([])
  const resultAppointment = ref({})
  // 待体检/体检中可编辑结果，已完成只读展示
  const resultEditable = ref(false)

  const showResultDialog = (row) => {
    resultAppointment.value = row
    resultEditable.value = row.status === 0 || row.status === 1
    resultDialogVisible.value = true
    examAppointmentApi.listItems(row.id).then(result => {
      resultItems.value = (result.data || []).map(item => ({...item, abnormal: item.abnormal ?? 0}))
    })
  }

  // 数值型结果变化时，若有参考范围则自动判断是否异常
  const onResultValueChange = (row) => {
    if (row.resultType === 1 && row.resultValue != null
        && (row.referenceMin != null || row.referenceMax != null)) {
      const outOfRange = (row.referenceMin != null && row.resultValue < row.referenceMin)
          || (row.referenceMax != null && row.resultValue > row.referenceMax)
      row.abnormal = outOfRange ? 1 : 0
    }
  }

  const saveResults = () => {
    examAppointmentApi.saveResults(resultAppointment.value.id, resultItems.value).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        resultDialogVisible.value = false
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
          <el-select v-model="examAppointmentQuery.elderId" clearable filterable placeholder="请选择老人" style="width: 180px">
            <el-option
                v-for="item in elderOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="套餐">
          <el-select v-model="examAppointmentQuery.packageId" clearable filterable placeholder="请选择套餐" style="width: 180px">
            <el-option
                v-for="item in packageOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="examAppointmentQuery.status" clearable placeholder="请选择状态" style="width: 150px">
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
      <el-table-column label="老人" width="120">
        <template #default="{ row }">
          {{ elderOptions.find(item => item.id === row.elderId)?.name || row.elderId }}
        </template>
      </el-table-column>
      <el-table-column label="体检套餐" width="160">
        <template #default="{ row }">
          {{ packageOptions.find(item => item.id === row.packageId)?.name || row.packageId }}
        </template>
      </el-table-column>
      <el-table-column prop="appointmentDate" label="预约/体检日期" width="180px"/>
      <el-table-column prop="price" label="价格(元)" width="100"/>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">
            {{ statusOptions.find(item => item.value === row.status)?.label || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="150"/>
      <el-table-column prop="createTime" label="创建时间" width="200px"/>
      <el-table-column align="center" width="280px" fixed="right" label="操作">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="showUpdateDialog(row.id)" >编辑</el-button>
          <el-button v-if="row.status === 0 || row.status === 1 || row.status === 2" size="small" type="warning" @click="showResultDialog(row)" >体检结果</el-button>
          <el-button size="small" type="danger" @click="deleteById(row.id)"  >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="examAppointmentQuery.page"
          v-model:page-size="examAppointmentQuery.limit"
          :page-sizes="[10, 20, 30, 40]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @change="loadData"
      />
    </div>
  </el-card>

  <!--添加、编辑弹出框-->
  <el-dialog v-model="dialogFormVisible" :title="title" width="500" :lock-scroll="false" :close-on-click-modal="false">
    <el-form :model="examAppointment">
      <el-form-item label="老人" :label-width="100">
        <el-select v-model="examAppointment.elderId" filterable placeholder="请选择老人">
          <el-option
              v-for="item in elderOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="体检套餐" :label-width="100">
        <el-select v-model="examAppointment.packageId" filterable placeholder="请选择套餐" @change="onPackageChange">
          <el-option
              v-for="item in packageOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="预约日期" :label-width="100">
        <el-date-picker
            v-model="examAppointment.appointmentDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择预约日期"
        />
      </el-form-item>
      <el-form-item label="预约时间" :label-width="100">
        <el-time-picker
            v-model="examAppointment.appointmentTime"
            value-format="HH:mm:ss"
            placeholder="请选择预约时间"
        />
      </el-form-item>
      <el-form-item label="价格(元)" :label-width="100">
        <el-input-number v-model="examAppointment.price" :precision="2" :min="0" autocomplete="off" />
      </el-form-item>
      <el-form-item label="状态" :label-width="100">
        <el-select v-model="examAppointment.status">
          <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="备注" :label-width="100">
        <el-input v-model="examAppointment.remark" type="textarea" :rows="3" autocomplete="off" />
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

  <!--体检结果弹出框-->
  <el-dialog v-model="resultDialogVisible" title="体检结果" width="850" :lock-scroll="false" :close-on-click-modal="false">
    <!--预约信息-->
    <el-descriptions :column="3" border style="margin-bottom: 16px">
      <el-descriptions-item label="老人">
        {{ elderOptions.find(item => item.id === resultAppointment.elderId)?.name || resultAppointment.elderId }}
      </el-descriptions-item>
      <el-descriptions-item label="体检套餐">
        {{ packageOptions.find(item => item.id === resultAppointment.packageId)?.name || resultAppointment.packageId }}
      </el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="statusTagType(resultAppointment.status)">
          {{ statusOptions.find(item => item.value === resultAppointment.status)?.label || resultAppointment.status }}
        </el-tag>
      </el-descriptions-item>
    </el-descriptions>

    <!--体检项目列表：待体检/体检中可编辑，已完成只读展示-->
    <el-table :data="resultItems" border>
      <el-table-column prop="itemName" label="体检项目" width="140"/>
      <el-table-column label="参考范围" width="170">
        <template #default="{ row }">
          <span v-if="row.referenceMin != null || row.referenceMax != null">
            {{ row.referenceMin ?? '-' }} ~ {{ row.referenceMax ?? '-' }} {{ row.referenceUnit || '' }}
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="体检结果" min-width="180">
        <template #default="{ row }">
          <template v-if="resultEditable">
            <div v-if="row.resultType === 1" style="display: flex; align-items: center; gap: 6px">
              <el-input-number
                  v-model="row.resultValue"
                  :precision="2"
                  :controls="false"
                  style="width: 140px"
                  @change="onResultValueChange(row)"
              />
            </div>
            <el-input v-else v-model="row.resultText" placeholder="请输入体检结果"/>
          </template>
          <span v-else>
            {{ row.resultType === 1 ? (row.resultValue != null ? row.resultValue : '-') : (row.resultText || '-') }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="是否异常" width="110">
        <template #default="{ row }">
          <el-select v-if="resultEditable" v-model="row.abnormal">
            <el-option :value="0" label="正常"/>
            <el-option :value="1" label="异常"/>
          </el-select>
          <el-tag v-else :type="row.abnormal === 1 ? 'danger' : 'success'">
            {{ row.abnormal === 1 ? '异常' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" min-width="140">
        <template #default="{ row }">
          <el-input v-if="resultEditable" v-model="row.remark" placeholder="请输入备注"/>
          <span v-else>{{ row.remark || '-' }}</span>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="resultItems.length === 0" description="该套餐暂无体检项目" :image-size="80"/>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="resultDialogVisible = false">关闭</el-button>
        <el-button v-if="resultEditable" type="primary" @click="saveResults">
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
</style>
