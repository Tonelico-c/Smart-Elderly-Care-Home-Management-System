<script setup>
  import {ref} from 'vue'
  import {ElMessage} from 'element-plus'
  import {Plus} from '@element-plus/icons-vue'
  import avatar from '@/assets/default.png'
  import {useTokenStore} from '@/store/token.js'
  import {UserInfoStore} from '@/store/userInfo.js'
  import userApi from '@/api/user.js'

  const tokenStore = useTokenStore()
  const userInfoStore = UserInfoStore()

  // 临时保存待提交的头像地址
  const avatarUrl = ref(userInfoStore.user.avatar || '')

  // 上传成功，先只更新本地预览，等点"更新头像"再提交
  const handleAvatarSuccess = (result) => {
    avatarUrl.value = result.data
  }

  // 提交修改
  const updateAvatar = () => {
    if (!avatarUrl.value) {
      ElMessage.warning('请先上传头像')
      return
    }
    // 只传 id 和 avatar，后端 updateById 只更新非空字段，不影响密码等其他信息
    userApi.update(userInfoStore.user.id, {id: userInfoStore.user.id, avatar: avatarUrl.value})
      .then(result => {
        if (result.code === 1) {
          ElMessage.success('更新成功')
          // 同步更新 store，右上角头像立即刷新
          userInfoStore.setUserInfo({...userInfoStore.user, avatar: avatarUrl.value})
        } else {
          ElMessage.error('更新失败')
        }
      })
  }
</script>

<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>更换头像</span>
      </div>
    </template>
    <el-row>
      <el-col :span="12">
        <h3 class="tip">上传新头像：</h3>
        <el-upload
            class="avatar-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            :headers="{Authorization: tokenStore.token}"
        >
          <img v-if="avatarUrl" :src="avatarUrl" class="avatar"/>
          <img v-else :src="avatar" class="avatar"/>
          <el-icon class="avatar-uploader-icon">
            <Plus/>
          </el-icon>
        </el-upload>
        <br/>
        <el-button type="primary" @click="updateAvatar">更新头像</el-button>
      </el-col>
    </el-row>
  </el-card>
</template>

<style scoped>
.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
}

.tip {
  color: #666;
  margin-bottom: 10px;
}
</style>

<style>
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
</style>
