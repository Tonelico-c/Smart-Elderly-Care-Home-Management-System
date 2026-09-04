<script setup>
  import {useRouter} from 'vue-router'
  import {showConfirmDialog, showSuccessToast, showFailToast, showToast} from 'vant'
  import elderApi from "@/api/elder.js";
  import {useTokenStore} from '@/store/token.js'
  import {elderElderInfoStore} from '@/store/elderInfo.js'
  import {useAppointmentStore} from '@/store/appointment.js'
  import examPackageApi from "@/api/examPackage.js";
  import {onMounted, ref} from "vue";
  const router = useRouter()
  const tokenStore = useTokenStore();
  const elderInfoStore = elderElderInfoStore();
  const appointmentStore = useAppointmentStore();

  //上架的可用套餐数量
  const packageCount = ref(0)
  onMounted(() => {
    examPackageApi.list().then(result => {
      if (result.code === 1) {
        packageCount.value = result.data.length
      }
    })
  })


  //退出登录
  const logout = () => {
    showConfirmDialog({
      title: '退出登录',
      message: '确定要退出登录吗？'
    }).then(() => {
      tokenStore.removeToken()
      elderInfoStore.removeElderInfo()
      //清掉这个账号的预约数据，避免换账号登录后串数据
      appointmentStore.reset()
      showToast('已退出')
      router.push('/login')
    }).catch(() => {})
  }

  //修改密码弹窗
  const showPasswordPopup = ref(false)
  const passwordFormRef = ref(null)
  const passwordForm = ref({
    oldPassword: '',
    newPassword: '',
    reNewPassword: ''
  })

  //校验两次密码是否一致
  const rePasswordValid = (value) => {
    if (value !== passwordForm.value.newPassword) {
      return '两次输入密码不一致'
    }
    return true
  }

  const passwordRules = {
    oldPassword: [
      {required: true, message: '请输入原密码'},
      {validator: v => v.length >= 3 && v.length <= 16, message: '密码长度必须为3~16位'}
    ],
    newPassword: [
      {required: true, message: '请输入新密码'},
      {validator: v => v.length >= 3 && v.length <= 16, message: '密码长度必须为3~16位'}
    ],
    reNewPassword: [
      {required: true, message: '请再次输入新密码'},
      {validator: rePasswordValid}
    ]
  }

  //打开弹窗时重置表单
  const openPasswordPopup = () => {
    passwordForm.value = {oldPassword: '', newPassword: '', reNewPassword: ''}
    showPasswordPopup.value = true
  }

  //提交修改密码（校验通过后触发 submit）
  const resetPassword = () => {
    elderApi.resetPassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    }).then(result => {
      if (result.code === 1) {
        showSuccessToast(result.msg)
        showPasswordPopup.value = false
        //修改成功后需重新登录，清掉本账号缓存数据
        tokenStore.removeToken()
        elderInfoStore.removeElderInfo()
        appointmentStore.reset()
        router.push('/login')
      } else {
        showFailToast(result.msg)
      }
    })
  }
</script>

<template>
  <div class="profile-page">
    <van-nav-bar title="我的" fixed placeholder/>

    <!--老人信息卡片-->
    <div class="elder-card">
      <div class="avatar">
        <van-icon name="user-circle-o" size="56" color="#fff"/>
      </div>
      <div class="elder-info">
        <div class="name">{{ elderInfoStore.elder.name }}</div>
        <div class="room">{{ elderInfoStore.elder.address }} · {{ elderInfoStore.elder.age }}岁</div>
      </div>
    </div>

    <!--预约统计-->
    <div class="stat-card">
      <div class="stat-item" @click="router.push('/appointment')">
        <div class="stat-value">{{ appointmentStore.appointmentList.length }}</div>
        <div class="stat-label">全部预约</div>
      </div>
      <div class="stat-item" @click="router.push('/appointment')">
        <div class="stat-value" style="color: #1989fa">{{ appointmentStore.pendingList.length }}</div>
        <div class="stat-label">待体检</div>
      </div>
      <div class="stat-item" @click="router.push('/package')">
        <div class="stat-value" style="color: #07c160">{{packageCount}}</div>
        <div class="stat-label">可用套餐</div>
      </div>
    </div>

    <!--菜单-->
    <van-cell-group inset class="menu-card">
      <van-cell title="修改密码" icon="shield-o" is-link @click="openPasswordPopup"/>
      <van-cell title="个人资料" icon="elder-o" is-link/>
      <van-cell title="健康档案" icon="records" is-link/>
      <van-cell title="我的预约" icon="clock-o" is-link @click="router.push('/appointment')"/>
      <van-cell title="我要请假" icon="edit" is-link @click="router.push('/leave-form')"/>
      <van-cell title="我的请假" icon="notes-o" is-link @click="router.push('/leave')"/>
      <van-cell title="体检报告" icon="notes-o" is-link/>
      <van-cell title="联系客服" icon="service-o" is-link/>
      <van-cell title="关于我们" icon="info-o" is-link/>
    </van-cell-group>

    <!--退出登录-->
    <div class="logout-btn">
      <van-button block round type="danger" plain @click="logout">退出登录</van-button>
    </div>

    <!--修改密码弹窗-->
    <van-popup v-model:show="showPasswordPopup" position="bottom" round :style="{paddingBottom: '20px'}">
      <div class="popup-title">修改密码</div>
      <van-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" @submit="resetPassword">
        <van-cell-group inset>
          <van-field v-model="passwordForm.oldPassword" type="password" name="oldPassword" label="原密码"
                     placeholder="请输入原密码" :rules="passwordRules.oldPassword"/>
          <van-field v-model="passwordForm.newPassword" type="password" name="newPassword" label="新密码"
                     placeholder="请输入新密码" :rules="passwordRules.newPassword"/>
          <van-field v-model="passwordForm.reNewPassword" type="password" name="reNewPassword" label="确认新密码"
                     placeholder="请再次输入新密码" :rules="passwordRules.reNewPassword"/>
        </van-cell-group>
        <div class="popup-btn">
          <van-button round block @click="showPasswordPopup = false">取消</van-button>
          <van-button round block type="primary" native-type="submit">确认修改</van-button>
        </div>
      </van-form>
    </van-popup>
  </div>
</template>

<style scoped lang="scss">
  .profile-page {
    padding-bottom: 30px;
  }

  .elder-card {
    display: flex;
    align-items: center;
    margin: 12px 16px 0;
    padding: 20px 16px;
    border-radius: 12px;
    background: linear-gradient(135deg, #1989fa 0%, #5cadff 100%);

    .avatar {
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .elder-info {
      flex: 1;
      margin-left: 14px;

      .name {
        font-size: 20px;
        font-weight: bold;
        color: #fff;
      }

      .room {
        margin-top: 6px;
        font-size: 12px;
        color: rgba(255, 255, 255, 0.9);
      }
    }
  }

  .stat-card {
    display: flex;
    margin: 12px 16px 0;
    padding: 16px 0;
    border-radius: 12px;
    background-color: #fff;

    .stat-item {
      flex: 1;
      text-align: center;

      .stat-value {
        font-size: 20px;
        font-weight: bold;
        color: #323233;
      }

      .stat-label {
        margin-top: 4px;
        font-size: 11px;
        color: #969799;
      }
    }
  }

  .menu-card {
    margin-top: 16px;
  }

  .logout-btn {
    margin: 24px 16px 0;
  }

  .popup-title {
    padding: 20px 16px 12px;
    font-size: 16px;
    font-weight: bold;
    color: #323233;
    text-align: center;
  }

  .popup-btn {
    display: flex;
    gap: 12px;
    margin: 20px 16px 0;
  }
</style>
