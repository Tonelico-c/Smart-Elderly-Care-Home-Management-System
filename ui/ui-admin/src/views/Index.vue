<script setup>
  import {
    Avatar,
    CaretBottom,
    CollectionTag,
    Crop,
    Document,
    EditPen,
    Key,
    Lock,
    Postcard,
    Setting,
    SwitchButton,
    User,
    UserFilled
  } from '@element-plus/icons-vue'
  import avatar from '@/assets/default.png'

  import {useTokenStore} from '@/store/token.js'
  const tokenStore = useTokenStore();
  //条目被点击后,调用的函数
  import {useRouter, useRoute} from 'vue-router'
  import {ElMessage, ElMessageBox} from "element-plus";
  const router = useRouter()
  const route = useRoute()

  // 头部显示当前日期
  const today = new Date().toLocaleDateString('zh-CN', {
    year: 'numeric', month: 'long', day: 'numeric', weekday: 'long'
  })
  import userApi from "@/api/user.js";
  import {UserInfoStore} from '@/store/userInfo.js'
  import {ref} from "vue";
  const userInfoStore = UserInfoStore();

  const dialogFormVisible = ref(false)
  const user = ref({})
  const handleCommand = (command) => {
    //判断指令
    if (command === 'logout') {
      //退出登录
      ElMessageBox.confirm('确定要退出登录吗？'
        , {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        tokenStore.removeToken()
        userInfoStore.removeUserInfo()
        router.push('/login')
      }).catch(()=>{

      })
    } else if(command === 'updateUserinfo'){
      dialogFormVisible.value = true
      Object.assign(user.value, userInfoStore.user)
    } else if (command === 'resetPassword'){
      dialogResetPasswordDialog.value = true
      userPasswordDTO.value = {}
      // resetForm.value.resetFields()
    } else {
      router.push('/user/' + command)
    }
  }

  // 获取用户信息
  const getUserInfo = () => {
    userApi.userInfo().then(result => {
      userInfoStore.setUserInfo(result.data.user)
      menuData.value = result.data.routerList
      userInfoStore.setBtn(result.data.btnList)
    })
  }
  getUserInfo()

  // 重置密码的表单数据
  const userPasswordDTO = ref({
    oldPassword: '',
    newPassword: '',
    reNewPassword: ''
  })
  const dialogResetPasswordDialog = ref(false)
  const resetForm = ref()
  //自定义确认密码的校验函数
  const rePasswordValid = (rule, value, callback) => {
    if (value == null || value === '') {
      return callback(new Error('请再次确认密码'))
    }
    //响应式对象要：registerData.value才能拿到值
    if (userPasswordDTO.value.newPassword !== value) {
      return callback(new Error('两次输入密码不一致'))
    }
    callback()
  }

  const rules = ref({
    oldPassword: [
      {required: true, message: '请输入密码', trigger: 'blur'},
      {min: 3, max: 16, message: '密码长度必须为3~16位', trigger: 'blur'}
    ],
    newPassword: [
      {required: true, message: '请输入密码', trigger: 'blur'},
      {min: 3, max: 16, message: '密码长度必须为3~16位', trigger: 'blur'}
    ],
    reNewPassword: [
      {required: true, message: '请输入密码', trigger: 'blur'},
      {validator: rePasswordValid, trigger: 'blur' }
    ]
  })

  const resetPassword = async (formEl) => {
    if (!formEl) return
    await formEl.validate((valid, fields) => {
      if (valid) {
        userApi.resetPassword(userPasswordDTO.value).then(result => {
          if (result.code === 1) {
            ElMessage.success(result.msg)
            dialogResetPasswordDialog.value = false
            tokenStore.removeToken();
            userInfoStore.removeUserInfo();
            // 跳转到登录
            router.push('/login')
          } else {
            ElMessage.error(result.msg)
          }
        })
      } else {
        ElMessage.error('表单验证失败');
      }
    })
  }

  // 头像上传成功
  const handleAvatarSuccess = (res) => {
    user.value.avatar = res.data
  }

  const updateUserInfo = () => {
    userApi.update(user.value.id, user.value).then(result => {
      if(result.code === 1){
        dialogFormVisible.value = false
        ElMessage.success('更新成功')
        getUserInfo()
      }else{
        ElMessage.error('更新失败')
      }
    })
  }

  // 菜单  用户管理， 分类管理， 商品管理
  const menuData = ref([
    {
      name: '业务管理', icon: 'Notebook', children: [
      {name: '老人管理', icon: 'UserFilled', path: "/elder"},
      {name: '入住分配', icon: 'House', path: "/checkin"},
      {name: '标签管理', icon: 'CollectionTag', path: "/tag"},
    ]
    },
    {
      name: '系统管理', icon: 'Setting', children: [
        {name: '用户管理', icon: 'User', path: "/user"},
        {name: '角色管理', icon: 'Key', path: "/role"},
        {name: '权限管理', icon: 'Lock', path: "/permission"},
      ]
    },
      {
        name: '个人中心', icon: 'Postcard', children: [
          {name: '个人资料', icon: 'Document', path: "/user/info"},
        ]
      },
  ]);
</script>

<template>
  <!-- element-plus中的容器 -->
  <el-container class="layout-container">
    <!-- 左侧菜单 -->
    <el-aside width="220px">
      <div class="el-aside__logo">
        <img class="logo-img" src="@/assets/logo.png" alt="logo"/>
        <span class="logo-title">智慧养老</span>
      </div>
      <!-- element-plus的菜单标签 -->
      <!-- default-active 绑定当前路由,让菜单自动高亮当前页面 -->
      <el-menu :default-active="route.path" router>
        <!-- 动态生成菜单 -->
        <template v-for="(menu, index) in menuData" :index="index.toString()">
          <el-sub-menu v-if="menu.children?.length>0" :index="menu.name">
            <template #title>
              <component
                  class="icons"
                  :is="menu.icon"
                  style="width: 1em; height: 1em; margin-right: 8px" >
              </component>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item v-for="(child, ind) in menu.children" :index="child.path">
              <el-icon><component :is="child.icon"></component></el-icon>
              <span>{{ child.name }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="menu.path">
            <el-icon><component :is="menu.icon"></component></el-icon>
            <span>{{ menu.name }}</span>
          </el-menu-item>
        </template>
      </el-menu>
<!--      <el-menu active-text-color="#ffd04b" background-color="#232323" text-color="#fff"
               :default-active="route.path" router>
        &lt;!&ndash; 业务管理 &ndash;&gt;
        <el-sub-menu index="business">
          <template #title>
            <el-icon>
              <Avatar/>
            </el-icon>
            <span>业务管理</span>
          </template>
          <el-menu-item index="/elder">
            <el-icon>
              <UserFilled/>
            </el-icon>
            <span>老人管理</span>
          </el-menu-item>
          <el-menu-item index="/tag">
            <el-icon>
              <CollectionTag/>
            </el-icon>
            <span>标签管理</span>
          </el-menu-item>
        </el-sub-menu>
        &lt;!&ndash; 系统管理 &ndash;&gt;
        <el-sub-menu index="system">
          <template #title>
            <el-icon>
              <Setting/>
            </el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/user">
            <el-icon>
              <User/>
            </el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/role">
            <el-icon>
              <Key/>
            </el-icon>
            <span>角色管理</span>
          </el-menu-item>
          <el-menu-item index="/permission">
            <el-icon>
              <Lock/>
            </el-icon>
            <span>权限管理</span>
          </el-menu-item>
        </el-sub-menu>
        &lt;!&ndash; 个人中心 &ndash;&gt;
        <el-sub-menu index="personal">
          <template #title>
            <el-icon>
              <Postcard/>
            </el-icon>
            <span>个人中心</span>
          </template>
          <el-menu-item index="/user/info">
            <el-icon>
              <Document/>
            </el-icon>
            <span>个人资料</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>-->
    </el-aside>
    <!-- 右侧主区域 -->
    <el-container>
      <!-- 头部区域 -->
      <el-header>
        <div class="header-title">
          <span class="title-dot"></span>
          <span>智慧养老社区管理系统</span>
          <span class="header-date">{{ today }}</span>
        </div>
        <!-- 下拉菜单 -->
        <!-- command: 条目被点击后会触发,在事件函数上可以声明一个参数,接收条目对应的指令 -->
        <el-dropdown placement="bottom-end" @command="handleCommand">
                    <span class="el-dropdown__box">
                        <el-avatar :src="userInfoStore.user.avatar?userInfoStore.user.avatar:avatar"/>
                        <span class="dropdown-name">{{ userInfoStore.user.name }}</span>
                        <el-icon>
                            <CaretBottom/>
                        </el-icon>
                    </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="info" :icon="User">基本资料</el-dropdown-item>
              <el-dropdown-item command="updateUserinfo" :icon="Crop">修改信息</el-dropdown-item>
              <el-dropdown-item command="resetPassword" :icon="EditPen">重置密码</el-dropdown-item>
              <el-dropdown-item command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <!-- 中间区域 -->
      <el-main>
        <!-- <div style="width: 1290px; height: 570px;border: 1px solid red;">
                    内容展示区
                </div> -->
        <router-view></router-view>
      </el-main>
      <!-- 底部区域 -->
      <el-footer>后台管理 ©2024 Created by Gao</el-footer>
    </el-container>
  </el-container>

  <el-dialog v-model="dialogFormVisible" :title="修改信息" width="500" :lock-scroll="false">
    <el-form :model="user">
      <el-form-item label="名字" :label-width="60">
        <el-input v-model="user.name" autocomplete="off" />
      </el-form-item>
      <el-form-item label="邮箱" :label-width="60">
        <el-input v-model="user.email" autocomplete="off" />
      </el-form-item>
      <el-form-item label="手机号" :label-width="60">
        <el-input v-model="user.phone" autocomplete="off" />
      </el-form-item>
      <el-form-item label="头像" :label-width="60">
        <el-upload
            class="avatar-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :headers="{Authorization: tokenStore.token}">
          <img v-if="user.avatar" :src="user.avatar" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="updateUserInfo">
          确认
        </el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog  v-model="dialogResetPasswordDialog" title="重置密码" width="500" :lock-scroll="false">
    <el-form ref="resetForm" :rules="rules" :model="userPasswordDTO">
      <el-form-item prop="oldPassword" label="原密码" :label-width="100">
        <el-input v-model="userPasswordDTO.oldPassword" autocomplete="off"/>
      </el-form-item>
      <el-form-item prop="newPassword" label="新密码" :label-width="100">
        <el-input v-model="userPasswordDTO.newPassword" autocomplete="off"/>
      </el-form-item>
      <el-form-item prop="reNewPassword" label="重复新密码" :label-width="100">
        <el-input v-model="userPasswordDTO.reNewPassword" autocomplete="off"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogResetPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="resetPassword(resetForm)">
          确认
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;

  .el-aside {
    /* 深青渐变侧边栏，呼应"智慧健康"主题 */
    background: linear-gradient(180deg, #103733 0%, #0a2622 100%);
    box-shadow: 2px 0 8px rgba(10, 38, 34, 0.15);
    display: flex;
    flex-direction: column;

    &__logo {
      height: 64px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      border-bottom: 1px solid rgba(255, 255, 255, 0.08);
      flex-shrink: 0;

      .logo-img {
        width: 34px;
        height: 34px;
        object-fit: contain;
      }

      .logo-title {
        font-size: 20px;
        font-weight: 700;
        letter-spacing: 2px;
        color: #fff;
      }
    }

    /* 菜单：透明背景由侧边栏渐变透出，选中项用主题色圆角高亮 */
    :deep(.el-menu) {
      --el-menu-bg-color: transparent;
      --el-menu-text-color: rgba(255, 255, 255, 0.68);
      --el-menu-hover-bg-color: rgba(255, 255, 255, 0.08);
      --el-menu-active-color: #5eead4;
      --el-menu-sub-item-hover-bg-color: rgba(255, 255, 255, 0.08);
      border-right: none;
      padding: 10px 12px;

      .el-menu-item,
      .el-sub-menu__title {
        border-radius: 8px;
        margin-bottom: 2px;
        height: 46px;
        line-height: 46px;
      }

      .el-menu-item.is-active {
        background: linear-gradient(90deg, #0d9488 0%, #14b8a6 100%);
        color: #fff !important;
        box-shadow: 0 4px 10px rgba(13, 148, 136, 0.35);
      }

      .el-sub-menu .el-menu {
        padding: 0 0 0 8px;
      }
    }
  }

  .el-header {
    height: 64px;
    background-color: #fff;
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-shadow: 0 1px 4px rgba(20, 60, 55, 0.08);
    position: relative;
    z-index: 1;

    .header-title {
      display: flex;
      align-items: center;
      font-size: 17px;
      font-weight: 600;
      color: #1f3835;

      .title-dot {
        width: 10px;
        height: 10px;
        border-radius: 50%;
        background: linear-gradient(135deg, #0d9488, #2dd4bf);
        margin-right: 10px;
      }

      .header-date {
        margin-left: 16px;
        font-size: 13px;
        font-weight: 400;
        color: #8aa5a1;
      }
    }

    .el-dropdown__box {
      display: flex;
      align-items: center;
      cursor: pointer;

      .dropdown-name {
        margin-left: 10px;
        font-size: 14px;
        color: #4b625e;
      }

      .el-icon {
        color: #8aa5a1;
        margin-left: 6px;
      }

      &:active,
      &:focus {
        outline: none;
      }
    }
  }

  .el-main {
    background-color: #f0f4f3;
    padding: 16px;
  }

  .el-footer {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 44px;
    font-size: 13px;
    color: #93a8a5;
    background-color: #f0f4f3;
  }
}
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}
</style>