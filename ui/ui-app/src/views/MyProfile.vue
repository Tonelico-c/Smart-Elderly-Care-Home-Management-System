<script setup>
  import {onMounted, ref} from 'vue'
  import {showToast} from 'vant'
  import {useRouter} from 'vue-router'
  import elderApi from '@/api/elder.js'
  import {elderElderInfoStore} from '@/store/elderInfo.js'

  const router = useRouter()
  const elderInfoStore = elderElderInfoStore()

  //进入页面时刷新老人信息，保证资料与后端一致
  const loading = ref(true)
  onMounted(() => {
    elderApi.elderInfo().then(result => {
      if (result.code === 1) {
        elderInfoStore.setElderInfo(result.data)
      } else {
        showToast(result.msg)
      }
    }).finally(() => {
      loading.value = false
    })
  })

  //资料字段为空时显示'-'
  const profileText = (value) => {
    return value === null || value === undefined || value === '' ? '-' : value
  }

  //修改基本资料弹窗（与ui-admin的Index.vue修改信息逻辑一致）
  const dialogFormVisible = ref(false)
  const elder = ref({})
  const showUpdateDialog = () => {
    //打开弹窗时把当前老人信息拷贝到表单
    Object.assign(elder.value, elderInfoStore.elder)
    dialogFormVisible.value = true
  }

  //头像上传成功后更新表单里的头像URL
  const afterRead = (file) => {
    elderApi.uploadAvatar(file.file).then(result => {
      if (result.code === 1) {
        elder.value.avatar = result.data
      } else {
        showToast(result.msg)
      }
    })
  }

  //确认修改后重新拉取老人信息，同步到缓存
  const updateElderInfo = () => {
    elderApi.updateInfo(elder.value).then(result => {
      if (result.code === 1) {
        dialogFormVisible.value = false
        showToast('更新成功')
        elderApi.elderInfo().then(r => {
          if (r.code === 1) {
            elderInfoStore.setElderInfo(r.data)
          }
        })
      } else {
        showToast(result.msg)
      }
    })
  }
</script>

<template>
  <div class="profile-detail-page">
    <van-nav-bar title="个人资料" left-arrow fixed placeholder right-text="修改" @click-left="router.back()"
                 @click-right="showUpdateDialog"/>

    <van-loading v-if="loading" class="page-loading" size="24" vertical>加载中...</van-loading>

    <template v-else>
      <!--头像-->
      <div class="avatar-wrap">
        <van-image
            v-if="elderInfoStore.elder.avatar"
            round
            width="72"
            height="72"
            fit="cover"
            :src="elderInfoStore.elder.avatar"
        />
        <!--无头像时显示默认图标-->
        <div v-else class="avatar-default">
          <van-icon name="user-circle-o" size="48" color="#c8c9cc"/>
        </div>
      </div>

      <!--基本信息-->
      <div class="section-title">基本信息</div>
      <van-cell-group inset>
        <van-cell title="姓名" :value="profileText(elderInfoStore.elder.name)"/>
        <van-cell title="年龄" :value="profileText(elderInfoStore.elder.age)"/>
        <van-cell title="出生日期" :value="profileText(elderInfoStore.elder.birthday)"/>
        <van-cell title="身份证号" :value="profileText(elderInfoStore.elder.idCardNo)"/>
        <van-cell title="联系电话" :value="profileText(elderInfoStore.elder.phone)"/>
        <van-cell title="家庭住址" :value="profileText(elderInfoStore.elder.address)"/>
      </van-cell-group>

      <!--入住信息-->
      <div class="section-title">入住信息</div>
      <van-cell-group inset>
        <van-cell title="入住楼栋" :value="profileText(elderInfoStore.elder.buildingName)"/>
        <van-cell title="房间号" :value="profileText(elderInfoStore.elder.roomNo)"/>
        <van-cell title="床位号" :value="profileText(elderInfoStore.elder.bedNo)"/>
      </van-cell-group>
    </template>

    <!--修改基本资料弹窗（与ui-admin的Index.vue修改信息逻辑一致）-->
    <van-popup v-model:show="dialogFormVisible" position="bottom" round :style="{paddingBottom: '20px'}">
      <div class="popup-title">修改信息</div>
      <van-cell-group inset>
        <van-field v-model="elder.name" label="姓名" placeholder="请输入姓名"/>
        <van-field v-model="elder.phone" type="tel" label="联系电话" placeholder="请输入联系电话"/>
        <van-field v-model="elder.address" type="textarea" rows="1" autosize label="家庭住址"
                   placeholder="请输入家庭住址"/>
        <!--头像：点击上传，成功后直接显示新头像-->
        <van-field label="头像">
          <template #input>
            <van-uploader :after-read="afterRead">
              <van-image v-if="elder.avatar" width="88" height="88" fit="cover" round :src="elder.avatar"/>
              <div v-else class="avatar-uploader">
                <van-icon name="plus" size="24" color="#969799"/>
              </div>
            </van-uploader>
          </template>
        </van-field>
      </van-cell-group>
      <div class="popup-btn">
        <van-button round block @click="dialogFormVisible = false">取消</van-button>
        <van-button round block type="primary" @click="updateElderInfo">确认</van-button>
      </div>
    </van-popup>
  </div>
</template>

<style scoped lang="scss">
  .profile-detail-page {
    min-height: 100vh;
    padding-bottom: 20px;
    background-color: #f5f6f8;
  }

  .page-loading {
    padding: 60px 0;
  }

  /*头像展示区*/
  .avatar-wrap {
    display: flex;
    justify-content: center;
    padding: 24px 0 4px;
  }

  .avatar-default {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 72px;
    height: 72px;
    border-radius: 50%;
    background-color: #ebedf0;
  }

  .section-title {
    margin: 16px 16px 8px;
    font-size: 13px;
    color: #969799;
  }

  .popup-title {
    padding: 20px 16px 12px;
    font-size: 16px;
    font-weight: bold;
    color: #323233;
    text-align: center;
  }

  /*无头像时的上传占位（与el-upload的Plus图标占位一致）*/
  .avatar-uploader {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 88px;
    height: 88px;
    border: 1px dashed #dcdee0;
    border-radius: 50%;
  }

  .popup-btn {
    display: flex;
    gap: 12px;
    margin: 20px 16px 0;
  }
</style>
