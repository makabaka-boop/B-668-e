import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import PendingList from './PendingList.vue'

const mockGet = vi.fn()
const mockPost = vi.fn()
vi.mock('../../utils/request', () => ({
  default: {
    get: (...args) => mockGet(...args),
    post: (...args) => mockPost(...args)
  }
}))

vi.mock('../../utils/date', () => ({
  formatDate: (d) => d || '',
  formatDateTime: (d) => d || ''
}))

vi.mock('../../components/FilePreview.vue', () => ({
  default: { template: '<div class="file-preview">preview</div>' }
}))

describe('PendingList.vue 辅导员待审核列表', () => {
  beforeEach(() => {
    vi.clearAllMocks()

    mockGet.mockResolvedValue({
      list: [
        {
          id: 1,
          studentId: 1,
          leaveDate: '2026-01-27',
          leaveType: 'SICK',
          reason: '感冒发烧',
          attachmentUrl: '/uploads/cert.jpg',
          createdAt: '2026-01-26 10:00:00',
          status: 'PENDING'
        },
        {
          id: 2,
          studentId: 2,
          leaveDate: '2026-01-29',
          leaveType: 'PERSONAL',
          reason: '家中有事',
          attachmentUrl: null,
          createdAt: '2026-01-28 14:00:00',
          status: 'PENDING'
        }
      ],
      total: 2,
      page: 1,
      size: 10
    })
  })

  const mountComponent = async () => {
    const wrapper = mount(PendingList, {
      global: {
        plugins: [ElementPlus]
      }
    })
    await new Promise(resolve => setTimeout(resolve, 0))
    await wrapper.vm.$nextTick()
    return wrapper
  }

  describe('页面渲染', () => {
    it('包含待审核列表标题', async () => {
      const wrapper = await mountComponent()
      expect(wrapper.find('h3').text()).toBe('待审核列表')
    })

    it('加载待审核数据', async () => {
      await mountComponent()
      expect(mockGet).toHaveBeenCalledWith('/counselor/leave-requests/pending', expect.objectContaining({
        params: expect.objectContaining({ page: 1, size: 10 })
      }))
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

  describe('审核操作', () => {
    it('点击通过按钮打开审核对话框', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm

      const mockRow = { id: 1, studentId: 1, leaveType: 'SICK', reason: '感冒' }
      vm.handleAudit(mockRow, 'APPROVED')

      expect(vm.dialogVisible).toBe(true)
      expect(vm.auditForm.result).toBe('APPROVED')
      expect(vm.currentRow).toEqual(mockRow)
    })

    it('点击拒绝按钮打开审核对话框', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm

      const mockRow = { id: 1, studentId: 1, leaveType: 'PERSONAL', reason: '有事' }
      vm.handleAudit(mockRow, 'REJECTED')

      expect(vm.dialogVisible).toBe(true)
      expect(vm.auditForm.result).toBe('REJECTED')
    })

    it('确认审核提交请求', async () => {
      mockPost.mockResolvedValue({})

      const wrapper = await mountComponent()
      const vm = wrapper.vm

      vm.currentRow = { id: 1 }
      vm.auditForm.result = 'APPROVED'
      vm.auditForm.remark = '同意请假'

      await vm.handleConfirmAudit()
      await wrapper.vm.$nextTick()

      expect(mockPost).toHaveBeenCalledWith('/counselor/leave-requests/1/audit', {
        result: 'APPROVED',
        remark: '同意请假'
      })
    })

    it('审核成功后关闭对话框并刷新数据', async () => {
      mockPost.mockResolvedValue({})

      const wrapper = await mountComponent()
      const vm = wrapper.vm

      vm.currentRow = { id: 1 }
      vm.auditForm.result = 'APPROVED'
      vm.auditForm.remark = ''

      await vm.handleConfirmAudit()
      await wrapper.vm.$nextTick()

      expect(vm.dialogVisible).toBe(false)
      expect(mockGet).toHaveBeenCalledTimes(2)
    })

    it('审核失败不关闭对话框', async () => {
      mockPost.mockRejectedValue(new Error('审核失败'))

      const wrapper = await mountComponent()
      const vm = wrapper.vm

      vm.currentRow = { id: 1 }
      vm.auditForm.result = 'APPROVED'
      vm.dialogVisible = true

      await vm.handleConfirmAudit()
      await wrapper.vm.$nextTick()

      expect(vm.dialogVisible).toBe(true)
    })
  })
})
