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

  //表单引用，用于触发表单校验
  const formRef = ref(null)

  const login = () => {
    //先做表单校验，校验通过才发起登录请求
    formRef.value.validate((valid) => {
      if (!valid) {
        ElMessage.error('请输入用户名或密码')
        return
      }
      userApi.login(user.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          tokenStore.setToken(result.data)
          router.push({path: '/'})
        } else {
          ElMessage.error(result.msg)
        }
      })
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

  //注册弹窗
  const registerDialogVisible = ref(false)
  const registerFormRef = ref(null)
  const registerUser = ref({
    name: '',
    password: '',
    rePassword: '',
    phone: ''
  })

  //校验两次输入的密码是否一致
  const rePasswordValid = (rule, value, callback) => {
    if (value !== registerUser.value.password) {
      return callback(new Error('两次输入密码不一致'))
    }
    callback()
  }

  //注册表单校验规则（手机号选填，填了才校验格式）
  const registerRules = ref({
    name: [
      {required: true, message: '请输入用户名', trigger: 'blur'},
      {min: 4, max: 16, message: '用户名的长度必须为4~16位', trigger: 'blur'}
    ],
    password: [
      {required: true, message: '请输入密码', trigger: 'blur'},
      {min: 3, max: 16, message: '密码长度必须为3~16位', trigger: 'blur'}
    ],
    rePassword: [
      {required: true, message: '请再次输入密码', trigger: 'blur'},
      {validator: rePasswordValid, trigger: 'blur'}
    ],
    phone: [
      {pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur'}
    ]
  })

  //打开注册弹窗时重置表单
  const openRegister = () => {
    registerUser.value = {name: '', password: '', rePassword: '', phone: ''}
    registerDialogVisible.value = true
  }

  //提交注册
  const register = () => {
    registerFormRef.value.validate((valid) => {
      if (!valid) {
        ElMessage.error('表单校验失败')
        return
      }
      userApi.register({
        name: registerUser.value.name,
        password: registerUser.value.password,
        phone: registerUser.value.phone
      }).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          registerDialogVisible.value = false
          //注册成功后回填用户名到登录框，方便直接登录
          user.value.name = registerUser.value.name
          user.value.password = ''
        } else {
          ElMessage.error(result.msg)
        }
      })
    })
  }
</script>

<template>
  <div class="login-bg">
    <!-- 装饰圆 -->
    <div class="deco deco-1"></div>
    <div class="deco deco-2"></div>

    <!-- 登录卡片 -->
    <el-form class="form-login" ref="formRef" size="large" autocomplete="off" :model="user" :rules="rules"
             @keyup.enter="login">
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
<!--      <el-form-item class="flex">
        <div class="flex">
          <el-checkbox>记住我</el-checkbox>
          <el-link type="primary" :underline="false">忘记密码？</el-link>
        </div>
      </el-form-item>-->
      <!-- 登录按钮 -->
      <el-form-item>
        <el-button class="button" type="primary" auto-insert-space @click="login">登录</el-button>
      </el-form-item>
      <!-- 注册入口 -->
      <el-form-item>
        <div class="register-tip">
          还没有账号？<el-link type="primary" :underline="false" @click="openRegister">立即注册</el-link>
        </div>
      </el-form-item>
    </el-form>

    <!-- 注册弹窗 -->
    <el-dialog v-model="registerDialogVisible" title="用户注册" width="420" :lock-scroll="false">
      <el-form ref="registerFormRef" :model="registerUser" :rules="registerRules" label-width="80px">
        <el-form-item prop="name" label="用户名">
          <el-input v-model="registerUser.name" placeholder="请输入用户名（4~16位）" autocomplete="off"/>
        </el-form-item>
        <el-form-item prop="password" label="密码">
          <el-input v-model="registerUser.password" type="password" placeholder="请输入密码（3~16位）"
                    autocomplete="off" show-password/>
        </el-form-item>
        <el-form-item prop="rePassword" label="确认密码">
          <el-input v-model="registerUser.rePassword" type="password" placeholder="请再次输入密码"
                    autocomplete="off" show-password/>
        </el-form-item>
        <el-form-item prop="phone" label="手机号">
          <el-input v-model="registerUser.phone" placeholder="请输入手机号（选填）" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="register">注册</el-button>
      </template>
    </el-dialog>
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

.register-tip {
  width: 100%;
  text-align: center;
  font-size: 13px;
  color: #8fb5b0;

  .el-link {
    font-size: 13px;
    vertical-align: baseline;
  }
}
</style>