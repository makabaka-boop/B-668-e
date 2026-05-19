<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="header-left">
        <h2>学生请假管理系统 - 教师端</h2>
      </div>
      <div class="header-right">
        <span class="user-info">{{ userInfo.name }}</span>
        <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>

    <el-container>
      <el-aside width="200px" class="aside">
        <el-menu
          :default-active="$route.path"
          router
          class="menu"
        >
          <el-menu-item index="/teacher/courses">
            <el-icon><List /></el-icon>
            <span>我的课程</span>
          </el-menu-item>
          <el-menu-item index="/teacher/leave-list">
            <el-icon><Document /></el-icon>
            <span>请假列表</span>
          </el-menu-item>
          <el-menu-item index="/teacher/attendance-stats">
            <el-icon><PieChart /></el-icon>
            <span>出勤统计</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const userInfo = ref({})

onMounted(() => {
  const info = localStorage.getItem('userInfo')
  if (info) {
    userInfo.value = JSON.parse(info)
  }
})

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await request.post('/auth/logout')
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    ElMessage.success('退出成功')
    router.push('/login')
  } catch (error) {
    // 用户取消
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #e6a23c;
  color: white;
  padding: 0 20px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info {
  font-size: 14px;
}

.aside {
  background: #f5f7fa;
  border-right: 1px solid #e4e7ed;
}

.menu {
  border-right: none;
}

.main {
  padding: 20px;
  background: #fff;
}
</style>
