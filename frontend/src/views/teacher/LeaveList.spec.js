import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import ElementPlus from 'element-plus'
import LeaveList from './LeaveList.vue'

const mockGet = vi.fn()
vi.mock('../../utils/request', () => ({
  default: {
    get: (...args) => mockGet(...args)
  }
}))

vi.mock('../../utils/date', () => ({
  formatDate: (d) => d || '',
  formatDateTime: (d) => d || ''
}))

vi.mock('../../components/FilePreview.vue', () => ({
  default: { template: '<div class="file-preview">preview</div>' }
}))

const createTestRouter = (query = {}) => createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/teacher/leave-list', component: LeaveList, props: true },
    { path: '/teacher/courses', component: { template: '<div>courses</div>' } }
  ]
})

describe('LeaveList.vue 教师请假列表', () => {
  let router

  beforeEach(() => {
    vi.clearAllMocks()

    mockGet.mockImplementation((url, config) => {
      if (url === '/teacher/courses') {
        return Promise.resolve([
          { courseId: 1, courseName: '数据库原理' },
          { courseId: 2, courseName: '操作系统' }
        ])
      }
      if (url === '/teacher/leave-requests') {
        return Promise.resolve({
          list: [
            {
              id: 1,
              studentId: 1,
              leaveDate: '2026-01-20',
              leaveType: 'SICK',
              reason: '感冒发烧',
              attachmentUrl: '/uploads/cert.jpg',
              status: 'APPROVED',
              auditRemark: '同意',
              createdAt: '2026-01-19 10:00:00'
            },
            {
              id: 2,
              studentId: 2,
              leaveDate: '2026-01-21',
              leaveType: 'PERSONAL',
              reason: '家中有事',
              attachmentUrl: null,
              status: 'PENDING',
              auditRemark: null,
              createdAt: '2026-01-20 14:00:00'
            }
          ],
          total: 2,
          page: 1,
          size: 10
        })
      }
      return Promise.resolve([])
    })
  })

  const mountComponent = async (query = {}) => {
    router = createTestRouter(query)
    router.push({ path: '/teacher/leave-list', query })
    await router.isReady()

    const wrapper = mount(LeaveList, {
      global: {
        plugins: [router, ElementPlus]
      }
    })
    await new Promise(resolve => setTimeout(resolve, 0))
    await wrapper.vm.$nextTick()
    return wrapper
  }

  describe('页面渲染', () => {
    it('包含课程请假列表标题', async () => {
      const wrapper = await mountComponent()
      expect(wrapper.find('h3').text()).toBe('课程请假列表')
    })

    it('加载课程列表和请假数据', async () => {
      await mountComponent()
      expect(mockGet).toHaveBeenCalledWith('/teacher/courses')
      expect(mockGet).toHaveBeenCalled()
      const leaveRequestCalls = mockGet.mock.calls.filter(call => call[0] === '/teacher/leave-requests')
      expect(leaveRequestCalls.length).toBeGreaterThanOrEqual(1)
    })

    it('渲染数据表格', async () => {
      const wrapper = await mountComponent()
      const table = wrapper.find('.el-table')
      expect(table.exists()).toBe(true)
    })

    it('显示分页组件', async () => {
      const wrapper = await mountComponent()
      const pagination = wrapper.find('.el-pagination')
      expect(pagination.exists()).toBe(true)
    })
  })

  describe('筛选功能', () => {
    it('查询时重置页码为1', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm

      vm.query.page = 3
      vm.query.courseId = 1
      vm.query.status = 'PENDING'

      vm.handleSearch()

      expect(vm.query.page).toBe(1)
    })

    it('重置筛选条件', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm

      vm.query.courseId = 1
      vm.query.status = 'PENDING'
      vm.query.page = 3

      vm.handleReset()

      expect(vm.query.courseId).toBeNull()
      expect(vm.query.status).toBe('')
      expect(vm.query.page).toBe(1)
    })
  })

  describe('状态显示', () => {
    it('getStatusText正确映射状态', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm

      expect(vm.getStatusText('PENDING')).toBe('待审核')
      expect(vm.getStatusText('APPROVED')).toBe('已通过')
      expect(vm.getStatusText('REJECTED')).toBe('未通过')
      expect(vm.getStatusText('UNKNOWN')).toBe('UNKNOWN')
    })
  })

  describe('从课程列表跳转', () => {
    it('带有courseId参数时自动选中课程', async () => {
      const wrapper = await mountComponent({ courseId: '1', courseName: '数据库原理' })
      const vm = wrapper.vm

      expect(vm.query.courseId).toBe(1)
      expect(vm.courseName).toBe('数据库原理')
    })
  })
})
