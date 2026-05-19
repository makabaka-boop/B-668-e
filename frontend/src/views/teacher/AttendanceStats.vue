<template>
  <div>
    <h3>出勤统计</h3>
    <p v-if="courseName" style="color: #666; margin-top: 10px">
      当前课程：{{ courseName }}
    </p>

    <!-- 筛选条件 -->
    <el-form :inline="true" style="margin-top: 20px">
      <el-form-item label="课程">
        <el-select v-model="query.courseId" placeholder="请选择课程" style="width: 200px">
          <el-option
            v-for="course in courses"
            :key="course.courseId"
            :label="course.courseName"
            :value="course.courseId"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <!-- 统计表格 -->
    <el-table :data="statsData" border style="margin-top: 20px" v-loading="loading">
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="studentName" label="姓名" width="120" />
      <el-table-column prop="leaveCount" label="请假次数（已通过）" width="150" />
    </el-table>

    <!-- 图表 -->
    <div v-if="statsData.length > 0" style="margin-top: 30px">
      <h4>请假次数统计图</h4>
      <div ref="chartRef" style="width: 100%; height: 400px; margin-top: 20px"></div>
    </div>

    <el-empty v-if="!loading && statsData.length === 0" description="暂无数据" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import request from '../../utils/request'

const route = useRoute()
const loading = ref(false)
const statsData = ref([])
const courses = ref([])
const courseName = ref('')
const chartRef = ref(null)
let chartInstance = null

const query = reactive({
  courseId: null
})

onMounted(async () => {
  await loadCourses()

  // 如果从课程列表跳转过来，自动选中课程
  if (route.query.courseId) {
    query.courseId = Number(route.query.courseId)
    courseName.value = route.query.courseName
  }

  if (query.courseId) {
    loadData()
  }
})

const loadCourses = async () => {
  try {
    courses.value = await request.get('/teacher/courses')
  } catch (error) {
    ElMessage.error('加载课程列表失败')
  }
}

const loadData = async () => {
  if (!query.courseId) {
    ElMessage.warning('请先选择课程')
    return
  }

  loading.value = true
  try {
    const params = {
      courseId: query.courseId
    }
    statsData.value = await request.get('/teacher/stats/attendance', { params })

    // 渲染图表
    await nextTick()
    renderChart()
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  loadData()
}

const renderChart = () => {
  if (!chartRef.value || statsData.value.length === 0) return

  // 销毁旧实例
  if (chartInstance) {
    chartInstance.dispose()
  }

  // 创建新实例
  chartInstance = echarts.init(chartRef.value)

  const option = {
    title: {
      text: '学生请假次数统计',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: statsData.value.map(item => item.studentName),
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '请假次数',
      minInterval: 1
    },
    series: [
      {
        name: '请假次数',
        type: 'bar',
        data: statsData.value.map(item => item.leaveCount),
        itemStyle: {
          color: '#409eff'
        },
        label: {
          show: true,
          position: 'top'
        }
      }
    ]
  }

  chartInstance.setOption(option)

  // 响应式
  window.addEventListener('resize', () => {
    chartInstance?.resize()
  })
}
</script>
