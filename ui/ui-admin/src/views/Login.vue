<script setup>
  import {ref} from "vue";
  import {User,Lock} from "@element-plus/icons-vue";

  import {useRouter} from 'vue-router'
  const router = useRouter()

  import userApi from "@/api/user.js";
  import {ElMessage} from "element-plus";
  import {useTokenStore} from '@/store/token.js'
  const tokenStore = useTokenStore();
  const user = ref({
    name: '',
    password: ''
  })

  const login = () =>{
    userApi.login(user.value).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        tokenStore.setToken(result.data)
        router.push({path: '/'})
      } else {
        ElMessage.error(result.msg)
      }
    })
  }

  //表单校验模型
  const rules = ref({
    name: [
      {required: true, message: '请输入用户名', trigger: 'blur'},
      {min: 4, max: 16, message: '用户名的长度必须为4~16位', trigger: 'blur'}
    ],
    password: [
      {required: true, message: '请输入密码', trigger: 'blur'},
      {min: 3, max: 16, message: '密码长度必须为3~16位', trigger: 'blur'}
    ]
  })
</script>

<template>
  <div class="login-bg">
    <!-- 装饰圆 -->
    <div class="deco deco-1"></div>
    <div class="deco deco-2"></div>

    <!-- 登录卡片 -->
    <el-form class="form-login" ref="form" size="large" autocomplete="off" :model="user" :rules="rules">
      <div class="login-title">
        <img class="login-logo" src="@/assets/logo.png" alt="logo"/>
        <h1>智慧养老社区管理系统</h1>
        <p>Smart Elderly Care Community</p>
      </div>
      <el-form-item prop="name">
        <el-input :prefix-icon="User" placeholder="请输入用户名" v-model="user.name"></el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input name="password" :prefix-icon="Lock" type="password" placeholder="请输入密码"
                  v-model="user.password"></el-input>
      </el-form-item>
      <el-form-item class="flex">
        <div class="flex">
          <el-checkbox>记住我</el-checkbox>
          <el-link type="primary" :underline="false">忘记密码？</el-link>
        </div>
      </el-form-item>
      <!-- 登录按钮 -->
      <el-form-item>
        <el-button class="button" type="primary" auto-insert-space @click="login">登录</el-button>
      </el-form-item>
    </el-form>
  </div>

</template>

<style scoped lang="scss">

.login-bg {
  height: 100vh;
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  /* 健康暖青渐变背景 */
  background: linear-gradient(135deg, #0f766e 0%, #14b8a6 55%, #5eead4 100%);
}

/* 背景装饰半透明圆 */
.deco {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.deco-1 {
  width: 420px;
  height: 420px;
  top: -120px;
  left: -100px;
}

.deco-2 {
  width: 520px;
  height: 520px;
  bottom: -180px;
  right: -140px;
}

.form-login {
  width: 400px;
  padding: 40px 44px 32px;
  border-radius: 16px;
  background-color: #fff;
  box-shadow: 0 20px 60px rgba(4, 62, 56, 0.35);
  position: relative;
  z-index: 1;
}

.login-title {
  text-align: center;
  margin-bottom: 28px;

  .login-logo {
    width: 52px;
    height: 52px;
    object-fit: contain;
  }

  h1 {
    margin: 10px 0 6px;
    font-size: 22px;
    font-weight: 700;
    color: #134e4a;
    letter-spacing: 1px;
  }

  p {
    margin: 0;
    font-size: 12px;
    color: #8fb5b0;
    letter-spacing: 3px;
  }
}

.form-login .button {
  width: 100%;
}
</style>