<template>
  <div class="course-list">
    <div class="page-header">
      <h3>我的课程</h3>
      <span class="course-count">共 {{ courses.length }} 门课程</span>
    </div>

    <el-table
      :data="courses"
      v-loading="loading"
      class="course-table"
      :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: '600' }"
      :row-class-name="tableRowClassName"
      stripe
    >
      <el-table-column prop="courseName" label="课程名称" min-width="180">
        <template #default="{ row }">
          <div class="course-name">
            <el-icon class="course-icon"><Reading /></el-icon>
            <span>{{ row.courseName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="teacherName" label="任课教师" width="120">
        <template #default="{ row }">
          <el-tag type="info" effect="plain" size="small">{{ row.teacherName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="上课时间" width="160">
        <template #default="{ row }">
          <div class="time-cell">
            <el-icon><Clock /></el-icon>
            <span>周{{ ['一', '二', '三', '四', '五'][row.weekday - 1] }} 第{{ row.period }}节</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="term" label="学期" width="130">
        <template #default="{ row }">
          <el-tag effect="plain" size="small">{{ row.term }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" round @click="goToLeaveApply(row)">
            <el-icon><EditPen /></el-icon>
            请假
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && courses.length === 0" description="暂无课程" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Reading, Clock, EditPen } from '@element-plus/icons-vue'
import request from '../../utils/request'

const router = useRouter()
const loading = ref(false)
const courses = ref([])

onMounted(async () => {
  await loadCourses()
})

const loadCourses = async () => {
  loading.value = true
  try {
    courses.value = await request.get('/student/courses')
  } catch (error) {
    ElMessage.error('加载课程列表失败')
  } finally {
    loading.value = false
  }
}

const goToLeaveApply = (course) => {
  router.push({
    path: '/student/leave-apply',
    query: {
      courseId: course.courseId,
      courseName: course.courseName
    }
  })
}

const tableRowClassName = ({ rowIndex }) => {
  return rowIndex % 2 === 0 ? 'even-row' : 'odd-row'
}
</script>

<style scoped>
.course-list {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header h3 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.course-count {
  font-size: 14px;
  color: #909399;
}

.course-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.course-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #303133;
}

.course-icon {
  color: #409eff;
  font-size: 18px;
}

.time-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
}

.time-cell .el-icon {
  color: #67c23a;
}

:deep(.el-table__row) {
  transition: background-color 0.2s;
}

:deep(.el-table__row:hover) {
  background-color: #ecf5ff !important;
}

:deep(.el-table__header th) {
  padding: 14px 0;
}

:deep(.el-table__body td) {
  padding: 16px 0;
}
</style>
