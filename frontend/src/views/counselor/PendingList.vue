<template>
	<div>
		<h3>待审核列表</h3>

		<!-- 数据表格 -->
		<el-table :data="tableData" border style="margin-top: 20px" v-loading="loading">
			<el-table-column prop="id" label="申请ID" width="80" />
			<el-table-column prop="studentId" label="学生ID" width="100" />
			<el-table-column label="请假日期" width="120">
				<template #default="{ row }">
					{{ formatDate(row.leaveDate) }}
				</template>
			</el-table-column>
			<el-table-column label="请假类型" width="100">
				<template #default="{ row }">
					<el-tag :type="row.leaveType === 'SICK' ? 'danger' : 'warning'">
						{{ row.leaveType === 'SICK' ? '病假' : '事假' }}
					</el-tag>
				</template>
			</el-table-column>
			<el-table-column prop="reason" label="请假原因" show-overflow-tooltip />
			<el-table-column label="附件" width="100">
				<template #default="{ row }">
					<FilePreview v-if="row.attachmentUrl" :url="row.attachmentUrl" />
					<span v-else>无</span>
				</template>
			</el-table-column>
			<el-table-column label="申请时间" width="160">
				<template #default="{ row }">
					{{ formatDateTime(row.createdAt) }}
				</template>
			</el-table-column>
			<el-table-column label="操作" width="150" fixed="right">
				<template #default="{ row }">
					<el-button type="success" size="small" @click="handleAudit(row, 'APPROVED')"> 通过 </el-button>
					<el-button type="danger" size="small" @click="handleAudit(row, 'REJECTED')"> 拒绝 </el-button>
				</template>
			</el-table-column>
		</el-table>

		<!-- 分页 -->
		<el-pagination
			v-model:current-page="query.page"
			v-model:page-size="query.size"
			:total="total"
			:page-sizes="[10, 20, 50]"
			layout="total, sizes, prev, pager, next, jumper"
			style="margin-top: 20px; justify-content: flex-end"
			@size-change="loadData"
			@current-change="loadData"
		/>

		<!-- 审核对话框 -->
		<el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
			<el-form :model="auditForm" label-width="100px">
				<el-form-item label="审核结果">
					<el-tag :type="auditForm.result === 'APPROVED' ? 'success' : 'danger'">
						{{ auditForm.result === 'APPROVED' ? '通过' : '不通过' }}
					</el-tag>
				</el-form-item>
				<el-form-item label="审核意见">
					<el-input
						v-model="auditForm.remark"
						type="textarea"
						:rows="4"
						placeholder="请输入审核意见（可选）"
						maxlength="255"
						show-word-limit
					/>
				</el-form-item>
			</el-form>
			<template #footer>
				<el-button @click="dialogVisible = false">取消</el-button>
				<el-button type="primary" @click="handleConfirmAudit" :loading="submitting"> 确定 </el-button>
			</template>
		</el-dialog>
	</div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import { formatDate, formatDateTime } from '../../utils/date'
import FilePreview from '../../components/FilePreview.vue'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const currentRow = ref(null)

const query = reactive({
	page: 1,
	size: 10,
})

const auditForm = reactive({
	result: '',
	remark: '',
})

onMounted(() => {
	loadData()
})

const loadData = async () => {
	loading.value = true
	try {
		const params = {
			page: query.page,
			size: query.size,
		}
		const data = await request.get('/counselor/leave-requests/pending', { params })
		tableData.value = data.list
		total.value = data.total
	} catch (error) {
		ElMessage.error('加载数据失败')
	} finally {
		loading.value = false
	}
}

const handleAudit = (row, result) => {
	currentRow.value = row
	auditForm.result = result
	auditForm.remark = ''
	dialogTitle.value = result === 'APPROVED' ? '审核通过' : '审核不通过'
	dialogVisible.value = true
}

const handleConfirmAudit = async () => {
	submitting.value = true
	try {
		await request.post(`/counselor/leave-requests/${currentRow.value.id}/audit`, auditForm)
		ElMessage.success('审核成功')
		dialogVisible.value = false
		loadData()
	} catch (error) {
		console.error('审核失败:', error)
	} finally {
		submitting.value = false
	}
}
</script>
