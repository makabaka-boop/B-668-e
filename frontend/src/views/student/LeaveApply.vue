<template>
	<div>
		<h3>请假申请</h3>

		<el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 600px; margin-top: 20px">
			<el-form-item label="课程" prop="courseId">
				<el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%">
					<el-option
						v-for="course in courses"
						:key="course.courseId"
						:label="`${course.courseName} - ${course.teacherName}`"
						:value="course.courseId"
					/>
				</el-select>
			</el-form-item>

			<el-form-item label="请假日期" prop="leaveDate">
				<el-date-picker
					v-model="form.leaveDate"
					type="date"
					placeholder="选择日期"
					style="width: 100%"
					format="YYYY-MM-DD"
					value-format="YYYY-MM-DD"
				/>
			</el-form-item>

			<el-form-item label="请假类型" prop="leaveType">
				<el-radio-group v-model="form.leaveType">
					<el-radio label="SICK">病假</el-radio>
					<el-radio label="PERSONAL">事假</el-radio>
				</el-radio-group>
			</el-form-item>

			<el-form-item label="请假原因" prop="reason">
				<el-input
					v-model="form.reason"
					type="textarea"
					:rows="4"
					placeholder="请输入请假原因"
					maxlength="500"
					show-word-limit
				/>
			</el-form-item>

			<el-form-item label="附件">
				<el-upload
					:action="uploadUrl"
					:headers="uploadHeaders"
					:on-success="handleUploadSuccess"
					:before-upload="beforeUpload"
					:limit="1"
				>
					<el-button size="small" type="primary">点击上传</el-button>
					<template #tip>
						<div class="el-upload__tip">支持jpg/png/pdf文件，且不超过5MB</div>
					</template>
				</el-upload>
				<div v-if="form.attachmentUrl" style="margin-top: 10px">
					<div style="display: flex; align-items: center; gap: 10px">
						<el-tag closable @close="form.attachmentUrl = ''"> 已上传附件 </el-tag>
						<FilePreview :url="form.attachmentUrl" />
					</div>
				</div>
			</el-form-item>

			<el-form-item>
				<el-button type="primary" @click="handleSubmit" :loading="submitting"> 提交申请 </el-button>
				<el-button @click="handleReset">重置</el-button>
			</el-form-item>
		</el-form>
	</div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import FilePreview from '../../components/FilePreview.vue'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const submitting = ref(false)
const courses = ref([])

const uploadUrl = '/api/upload'
const uploadHeaders = {
	Authorization: `Bearer ${localStorage.getItem('token')}`,
}

const form = reactive({
	courseId: null,
	leaveDate: '',
	leaveType: 'SICK',
	reason: '',
	attachmentUrl: '',
})

const rules = {
	courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
	leaveDate: [{ required: true, message: '请选择请假日期', trigger: 'change' }],
	leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
}

onMounted(async () => {
	await loadCourses()

	// 如果从课程列表跳转过来，自动选中课程
	if (route.query.courseId) {
		form.courseId = Number(route.query.courseId)
	}
})

const loadCourses = async () => {
	try {
		courses.value = await request.get('/student/courses')
	} catch (error) {
		ElMessage.error('加载课程列表失败')
	}
}

const beforeUpload = (file) => {
	const isValidType = ['image/jpeg', 'image/png', 'application/pdf'].includes(file.type)
	const isLt5M = file.size / 1024 / 1024 < 5

	if (!isValidType) {
		ElMessage.error('只能上传 JPG/PNG/PDF 格式的文件!')
		return false
	}
	if (!isLt5M) {
		ElMessage.error('文件大小不能超过 5MB!')
		return false
	}
	return true
}

const handleUploadSuccess = (response) => {
	if (response.code === 200) {
		form.attachmentUrl = response.data
		ElMessage.success('上传成功')
	} else {
		ElMessage.error(response.message || '上传失败')
	}
}

const handleSubmit = async () => {
	if (!formRef.value) return

	await formRef.value.validate(async (valid) => {
		if (!valid) return

		submitting.value = true
		try {
			await request.post('/student/leave-requests', form)
			ElMessage.success('提交成功')
			router.push('/student/leave-records')
		} catch (error) {
			console.error('提交失败:', error)
		} finally {
			submitting.value = false
		}
	})
}

const handleReset = () => {
	formRef.value?.resetFields()
	form.attachmentUrl = ''
}
</script>
