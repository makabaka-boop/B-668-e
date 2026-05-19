<template>
	<div>
		<h3>统计查询</h3>

		<!-- 筛选条件 -->
		<el-form :inline="true" style="margin-top: 20px">
			<el-form-item label="学号">
				<el-input v-model="query.studentNo" placeholder="请输入学号" clearable style="width: 150px" />
			</el-form-item>
			<el-form-item label="状态">
				<el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
					<el-option label="待审核" value="PENDING" />
					<el-option label="已通过" value="APPROVED" />
					<el-option label="未通过" value="REJECTED" />
				</el-select>
			</el-form-item>
			<el-form-item label="类型">
				<el-select v-model="query.leaveType" placeholder="全部" clearable style="width: 120px">
					<el-option label="病假" value="SICK" />
					<el-option label="事假" value="PERSONAL" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" @click="handleSearch">查询</el-button>
				<el-button @click="handleReset">重置</el-button>
				<el-button type="success" @click="handleLoadStatistics">查看统计</el-button>
			</el-form-item>
		</el-form>

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
			<el-table-column label="状态" width="100">
				<template #default="{ row }">
					<el-tag :type="row.status === 'APPROVED' ? 'success' : row.status === 'REJECTED' ? 'danger' : 'info'">
						{{ getStatusText(row.status) }}
					</el-tag>
				</template>
			</el-table-column>
			<el-table-column prop="auditRemark" label="审核意见" show-overflow-tooltip />
			<el-table-column label="申请时间" width="160">
				<template #default="{ row }">
					{{ formatDateTime(row.createdAt) }}
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

		<!-- 统计对话框 -->
		<el-dialog v-model="statsDialogVisible" title="学生请假统计" width="600px">
			<el-table :data="statsData" border>
				<el-table-column prop="studentNo" label="学号" width="120" />
				<el-table-column prop="studentName" label="姓名" width="120" />
				<el-table-column prop="leaveCount" label="请假次数" width="100" />
			</el-table>
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
const tableData = ref([])
const total = ref(0)
const statsDialogVisible = ref(false)
const statsData = ref([])

const query = reactive({
	studentNo: '',
	status: '',
	leaveType: '',
	page: 1,
	size: 10,
})

onMounted(() => {
	loadData()
})

const loadData = async () => {
	loading.value = true
	try {
		const params = {
			studentNo: query.studentNo || undefined,
			status: query.status || undefined,
			leaveType: query.leaveType || undefined,
			page: query.page,
			size: query.size,
		}
		const data = await request.get('/counselor/leave-requests', { params })
		tableData.value = data.list
		total.value = data.total
	} catch (error) {
		ElMessage.error('加载数据失败')
	} finally {
		loading.value = false
	}
}

const handleSearch = () => {
	query.page = 1
	loadData()
}

const handleReset = () => {
	query.studentNo = ''
	query.status = ''
	query.leaveType = ''
	query.page = 1
	loadData()
}

const handleLoadStatistics = async () => {
	try {
		statsData.value = await request.get('/counselor/stats/by-student')
		statsDialogVisible.value = true
	} catch (error) {
		ElMessage.error('加载统计数据失败')
	}
}

const getStatusText = (status) => {
	const map = {
		PENDING: '待审核',
		APPROVED: '已通过',
		REJECTED: '未通过',
	}
	return map[status] || status
}
</script>
