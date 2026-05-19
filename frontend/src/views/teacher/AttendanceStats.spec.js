import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import ElementPlus from 'element-plus'
import AttendanceStats from './AttendanceStats.vue'

vi.mock('../../utils/request', () => ({
  default: {
    get: vi.fn(),
  },
}))

vi.mock('echarts', () => ({
  init: vi.fn(() => ({
    setOption: vi.fn(),
    dispose: vi.fn(),
    resize: vi.fn(),
  })),
}))

vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    default: actual,
  }
})

describe('AttendanceStats.vue', () => {
  let router

  beforeEach(() => {
    router = createRouter({
      history: createWebHistory(),
      routes: [
        {
          path: '/teacher/stats',
          name: 'AttendanceStats',
          component: AttendanceStats,
        },
      ],
    })

    vi.clearAllMocks()
  })

  it('should render page title correctly', () => {
    const wrapper = mount(AttendanceStats, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    expect(wrapper.find('h3').text()).toBe('出勤统计')
  })

  it('should load courses on mount', async () => {
    const mockCourses = [
      { courseId: 1, courseName: '数据库原理' },
      { courseId: 2, courseName: '操作系统' },
    ]

    const request = await import('../../utils/request')
    request.default.get.mockResolvedValueOnce(mockCourses)

    const wrapper = mount(AttendanceStats, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(request.default.get).toHaveBeenCalledWith('/teacher/courses')
    expect(wrapper.vm.courses).toEqual(mockCourses)
  })

  it('should show warning when searching without course selection', async () => {
    const request = await import('../../utils/request')
    request.default.get.mockResolvedValueOnce([])

    const wrapper = mount(AttendanceStats, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    wrapper.vm.query.courseId = null
    const result = await wrapper.vm.loadData()

    expect(result).toBeUndefined()
    expect(wrapper.vm.loading).toBe(false)
  })

  it('should load attendance stats data', async () => {
    const mockCourses = [
      { courseId: 1, courseName: '数据库原理' },
    ]

    const mockStats = [
      { studentNo: '2025001', studentName: '张三', leaveCount: 2 },
      { studentNo: '2025002', studentName: '李四', leaveCount: 1 },
      { studentNo: '2025003', studentName: '王五', leaveCount: 0 },
    ]

    const request = await import('../../utils/request')
    request.default.get
      .mockResolvedValueOnce(mockCourses)
      .mockResolvedValueOnce(mockStats)

    const wrapper = mount(AttendanceStats, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    wrapper.vm.query.courseId = 1
    await wrapper.vm.loadData()

    expect(request.default.get).toHaveBeenLastCalledWith('/teacher/stats/attendance', {
      params: { courseId: 1 },
    })
    expect(wrapper.vm.statsData).toEqual(mockStats)
  })

  it('should handle error when loading courses fails', async () => {
    const request = await import('../../utils/request')
    request.default.get.mockRejectedValueOnce(new Error('Network error'))

    const wrapper = mount(AttendanceStats, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(wrapper.vm.courses).toEqual([])
  })

  it('should handle error when loading stats fails', async () => {
    const mockCourses = [
      { courseId: 1, courseName: '数据库原理' },
    ]

    const request = await import('../../utils/request')
    request.default.get
      .mockResolvedValueOnce(mockCourses)
      .mockRejectedValueOnce(new Error('Network error'))

    const wrapper = mount(AttendanceStats, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    wrapper.vm.query.courseId = 1
    await wrapper.vm.loadData()

    expect(wrapper.vm.loading).toBe(false)
    expect(wrapper.vm.statsData).toEqual([])
  })

  it('should display empty state when no stats data', async () => {
    const mockCourses = [
      { courseId: 1, courseName: '数据库原理' },
    ]

    const request = await import('../../utils/request')
    request.default.get
      .mockResolvedValueOnce(mockCourses)
      .mockResolvedValueOnce([])

    const wrapper = mount(AttendanceStats, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    wrapper.vm.query.courseId = 1
    await wrapper.vm.loadData()

    expect(wrapper.vm.statsData).toEqual([])
  })
})
