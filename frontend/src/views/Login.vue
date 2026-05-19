<template>
	<div class="login-container">
		<div class="login-box">
			<h1 class="title">无纸化学生请假管理系统</h1>
			<el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
				<el-form-item prop="username">
					<el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" size="large" />
				</el-form-item>

				<el-form-item prop="password">
					<el-input
						v-model="loginForm.password"
						type="password"
						placeholder="请输入密码"
						prefix-icon="Lock"
						size="large"
						@keyup.enter="handleLogin"
					/>
				</el-form-item>

				<el-form-item prop="roleType">
					<el-select v-model="loginForm.roleType" placeholder="请选择角色" size="large" style="width: 100%">
						<el-option label="学生" value="STUDENT" />
						<el-option label="教师" value="TEACHER" />
						<el-option label="辅导员" value="COUNSELOR" />
					</el-select>
				</el-form-item>

				<el-form-item>
					<el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleLogin">
						登录
					</el-button>
				</el-form-item>
			</el-form>

			<div class="tips">
				<p class="tips-title">快速登录测试账号</p>
				<div class="test-accounts">
					<el-button size="small" @click="fillTestAccount('student1', 'STUDENT')">
						<el-icon><User /></el-icon>
						学生
					</el-button>
					<el-button size="small" @click="fillTestAccount('teacher1', 'TEACHER')">
						<el-icon><Reading /></el-icon>
						教师
					</el-button>
					<el-button size="small" @click="fillTestAccount('counselor1', 'COUNSELOR')">
						<el-icon><UserFilled /></el-icon>
						辅导员
					</el-button>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Reading, UserFilled } from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
	username: '',
	password: '',
	roleType: '',
})

const rules = {
	username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
	password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
	roleType: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

const fillTestAccount = (username, roleType) => {
	loginForm.username = username
	loginForm.password = '123456'
	loginForm.roleType = roleType
}

const handleLogin = async () => {
	if (!loginFormRef.value || loading.value) return

	try {
		// 先检查 validation 状态，如果是异步校验也不怕并发
		await loginFormRef.value.validate()

		// 再次判定 loading 状态，双重防护
		if (loading.value) return
		loading.value = true

		const data = await request.post('/auth/login', {
			username: loginForm.username,
			password: loginForm.password,
			roleType: loginForm.roleType,
		})

		// 保存token和用户信息
		localStorage.setItem('token', data.token)
		localStorage.setItem('userInfo', JSON.stringify(data))

		ElMessage.success('登录成功')

		// 根据角色跳转到对应页面
		if (data.roleType === 'STUDENT') {
			router.push('/student/courses')
		} else if (data.roleType === 'TEACHER') {
			router.push('/teacher/courses')
		} else if (data.roleType === 'COUNSELOR') {
			router.push('/counselor/pending')
		}
	} catch (error) {
		// 仅记录日志，错误提示由 request.js 拦截器统一处理
		console.error('Login entry failed:', error)
	} finally {
		loading.value = false
	}
}
</script>

<style scoped>
.login-container {
	display: flex;
	justify-content: center;
	align-items: center;
	min-height: 100vh;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
	width: 400px;
	padding: 40px;
	background: white;
	border-radius: 10px;
	box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.title {
	text-align: center;
	font-size: 24px;
	font-weight: bold;
	color: #333;
	margin-bottom: 30px;
}

.login-form {
	margin-top: 20px;
}

.tips {
	margin-top: 20px;
	padding: 15px;
	background: #f5f7fa;
	border-radius: 8px;
}

.tips-title {
	margin: 0 0 12px 0;
	font-size: 13px;
	color: #909399;
	text-align: center;
}

.test-accounts {
	display: flex;
	justify-content: center;
	gap: 10px;
}

.test-accounts .el-button {
	flex: 1;
}
</style>
