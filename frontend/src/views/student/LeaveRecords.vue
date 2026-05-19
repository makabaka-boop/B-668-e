<template>
	<div>
		<h3>请假记录</h3>

		<!-- 筛选条件 -->
		<el-form :inline="true" style="margin-top: 20px">
			<el-form-item label="状态">
				<el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
					<el-option label="待审核" value="PENDING" />
					<el-option label="已通过" value="APPROVED" />
					<el-option label="未通过" value="REJECTED" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" @click="handleSearch">查询</el-button>
				<el-button @click="handleReset">重置</el-button>
			</el-form-item>
		</el-form>

		<!-- 数据表格 -->
		<el-table :data="tableData" border style="margin-top: 20px" v-loading="loading">
			<el-table-column prop="id" label="申请ID" width="80" />
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
			<el-table-column label="操作" width="100">
				<template #default="{ row }">
					<el-button type="primary" size="small" link @click="handleView(row)"> 查看详情 </el-button>
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

		<!-- 详情对话框 -->
		<el-dialog v-model="dialogVisible" title="请假详情" width="600px">
			<el-descriptions :column="1" border v-if="currentRow">
				<el-descriptions-item label="申请ID">{{ currentRow.id }}</el-descriptions-item>
				<el-descriptions-item label="请假日期">{{ formatDate(currentRow.leaveDate) }}</el-descriptions-item>
				<el-descriptions-item label="请假类型">
					{{ currentRow.leaveType === 'SICK' ? '病假' : '事假' }}
				</el-descriptions-item>
				<el-descriptions-item label="请假原因">{{ currentRow.reason }}</el-descriptions-item>
				<el-descriptions-item label="附件" v-if="currentRow.attachmentUrl">
					<FilePreview :url="currentRow.attachmentUrl" />
				</el-descriptions-item>
				<el-descriptions-item label="状态">
					<el-tag
						:type="currentRow.status === 'APPROVED' ? 'success' : currentRow.status === 'REJECTED' ? 'danger' : 'info'"
					>
						{{ getStatusText(currentRow.status) }}
					</el-tag>
				</el-descriptions-item>
				<el-descriptions-item label="审核意见" v-if="currentRow.auditRemark">
					{{ currentRow.auditRemark }}
				</el-descriptions-item>
				<el-descriptions-item label="申请时间">{{ formatDateTime(currentRow.createdAt) }}</el-descriptions-item>
				<el-descriptions-item label="审核时间" v-if="currentRow.auditedAt">
					{{ formatDateTime(currentRow.auditedAt) }}
				</el-descriptions-item>
			</el-descriptions>
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
const dialogVisible = ref(false)
const currentRow = ref(null)

const query = reactive({
	status: '',
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
			status: query.status || undefined,
			page: query.page,
			size: query.size,
		}
		const data = await request.get('/student/leave-requests', { params })
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
	query.status = ''
	query.page = 1
	loadData()
}

const handleView = (row) => {
	currentRow.value = row
	dialogVisible.value = true
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
