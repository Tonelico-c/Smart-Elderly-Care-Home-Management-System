<script setup>

  import {UserInfoStore} from "@/store/userInfo.js";
  const userInfoStore = UserInfoStore();
  const user = userInfoStore.user;
</script>

<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>基本资料</span>
      </div>
    </template>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="用户名">{{ user.name }}</el-descriptions-item>
      <el-descriptions-item label="手机号">{{ user.phone }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ user.email }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="user.status === 1 ? 'success' : 'danger'">
          {{ user.status === 1 ? '正常' : '禁用' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ user.createTime }}</el-descriptions-item>
      <el-descriptions-item label="修改时间">{{ user.updateTime }}</el-descriptions-item>
      <el-descriptions-item label="头像">
        <img :src="user.avatar" alt="头像" style="width: 120px; height: 150px; border-radius: 50%">
      </el-descriptions-item>
    </el-descriptions>
  </el-card>
</template>

<style lang="scss" scoped>
.page-container {
  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}
</style>
