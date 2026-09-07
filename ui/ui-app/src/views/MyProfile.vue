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
</script>

<template>
  <div class="profile-detail-page">
    <van-nav-bar title="个人资料" left-arrow fixed placeholder @click-left="router.back()"/>

    <van-loading v-if="loading" class="page-loading" size="24" vertical>加载中...</van-loading>

    <template v-else>
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

  .section-title {
    margin: 16px 16px 8px;
    font-size: 13px;
    color: #969799;
  }
</style>
