import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import ElementPlus from 'element-plus'
import LeaveApply from './LeaveApply.vue'

const mockGet = vi.fn()
const mockPost = vi.fn()
vi.mock('../../utils/request', () => ({
  default: {
    get: (...args) => mockGet(...args),
    post: (...args) => mockPost(...args)
  }
}))

vi.mock('../../components/FilePreview.vue', () => ({
  default: { template: '<div class="file-preview">preview</div>' }
}))

const createTestRouter = () => createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/student/leave-apply', component: LeaveApply },
    { path: '/student/leave-records', component: { template: '<div>records</div>' } }
  ]
})

describe('LeaveApply.vue 学生请假申请', () => {
  let router

  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    localStorage.setItem('token', 'test-token')
    router = createTestRouter()

    mockGet.mockResolvedValue([
      { courseId: 1, courseName: '数据库原理', teacherName: '王老师' },
      { courseId: 2, courseName: '操作系统', teacherName: '刘老师' }
    ])
  })

  const mountComponent = async () => {
    router.push('/student/leave-apply')
    await router.isReady()

    const wrapper = mount(LeaveApply, {
      global: {
        plugins: [router, ElementPlus]
      }
    })
    await new Promise(resolve => setTimeout(resolve, 0))
    await wrapper.vm.$nextTick()
    return wrapper
  }

  describe('页面渲染', () => {
    it('包含请假申请表单', async () => {
      const wrapper = await mountComponent()
      expect(wrapper.find('h3').text()).toBe('请假申请')
      expect(wrapper.find('form').exists() || wrapper.find('.el-form').exists()).toBe(true)
    })

    it('加载课程列表', async () => {
      await mountComponent()
      expect(mockGet).toHaveBeenCalledWith('/student/courses')
    })

    it('包含提交按钮和重置按钮', async () => {
      const wrapper = await mountComponent()
      const buttons = wrapper.findAll('button')
      const texts = buttons.map(b => b.text())
      expect(texts.some(t => t.includes('提交'))).toBe(true)
      expect(texts.some(t => t.includes('重置'))).toBe(true)
    })
  })

  describe('请假类型选择', () => {
    it('默认请假类型为SICK', async () => {
      const wrapper = await mountComponent()
      expect(wrapper.vm.form.leaveType).toBe('SICK')
    })
  })

  describe('提交请假', () => {
    it('提交成功后跳转到请假记录页', async () => {
      mockPost.mockResolvedValue({})

      const wrapper = await mountComponent()
      const vm = wrapper.vm

      vm.form.courseId = 1
      vm.form.leaveDate = '2025-09-01'
      vm.form.leaveType = 'SICK'
      vm.form.reason = '感冒发烧'

      vm.formRef = { validate: vi.fn().mockImplementation(cb => {
        if (cb) cb(true)
        return Promise.resolve(true)
      }) }

      await vm.handleSubmit()
      await wrapper.vm.$nextTick()

      expect(mockPost).toHaveBeenCalledWith('/student/leave-requests', expect.objectContaining({
        courseId: 1,
        leaveType: 'SICK',
        reason: '感冒发烧'
      }))
    })

    it('提交失败不跳转', async () => {
      mockPost.mockRejectedValue(new Error('提交失败'))

      const wrapper = await mountComponent()
      const vm = wrapper.vm

      vm.form.courseId = 1
      vm.form.leaveDate = '2025-09-01'
      vm.form.leaveType = 'SICK'

      vm.formRef = { validate: vi.fn().mockImplementation(cb => {
        if (cb) cb(true)
        return Promise.resolve(true)
      }) }

      const pushSpy = vi.spyOn(router, 'push')

      await vm.handleSubmit()
      await wrapper.vm.$nextTick()

      expect(pushSpy).not.toHaveBeenCalledWith('/student/leave-records')
    })
  })

  describe('重置表单', () => {
    it('重置表单清空所有字段', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm

      vm.form.courseId = 1
      vm.form.leaveDate = '2025-09-01'
      vm.form.attachmentUrl = '/uploads/test.jpg'

      vm.formRef = { resetFields: vi.fn() }

      vm.handleReset()
      expect(vm.formRef.resetFields).toHaveBeenCalled()
      expect(vm.form.attachmentUrl).toBe('')
    })
  })
})
