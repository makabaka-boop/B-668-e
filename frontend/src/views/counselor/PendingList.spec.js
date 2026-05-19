import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import ElementPlus from 'element-plus'
import PendingList from './PendingList.vue'

vi.mock('../../utils/request', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

vi.mock('../../utils/date', () => ({
  formatDate: vi.fn((date) => `formatted-${date}`),
  formatDateTime: vi.fn((date) => `formatted-${date}`),
}))

vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    default: actual,
  }
})

describe('PendingList.vue', () => {
  let router

  beforeEach(() => {
    router = createRouter({
      history: createWebHistory(),
      routes: [
        {
          path: '/counselor/pending',
          name: 'PendingList',
          component: PendingList,
        },
      ],
    })

    vi.clearAllMocks()
  })

  it('should render page title correctly', () => {
    const wrapper = mount(PendingList, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    expect(wrapper.find('h3').text()).toBe('待审核列表')
  })

  it('should load pending leave requests on mount', async () => {
    const mockResponse = {
      list: [
        {
          id: 1,
          studentId: 1,
          leaveDate: '2026-01-27',
          leaveType: 'SICK',
          reason: '感冒发烧',
          attachmentUrl: '/uploads/test.jpg',
          createdAt: '2026-01-26 10:00:00',
        },
      ],
      total: 1,
    }

    const request = await import('../../utils/request')
    request.default.get.mockResolvedValueOnce(mockResponse)

    const wrapper = mount(PendingList, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(request.default.get).toHaveBeenCalledWith('/counselor/leave-requests/pending', {
      params: { page: 1, size: 10 },
    })
    expect(wrapper.vm.tableData).toEqual(mockResponse.list)
    expect(wrapper.vm.total).toBe(1)
  })

  it('should display loading state while fetching data', async () => {
    const request = await import('../../utils/request')
    request.default.get.mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve({ list: [], total: 0 }), 100))
    )

    const wrapper = mount(PendingList, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    expect(wrapper.vm.loading).toBe(true)
  })

  it('should handle error when loading data fails', async () => {
    const request = await import('../../utils/request')
    request.default.get.mockRejectedValueOnce(new Error('Network error'))

    const wrapper = mount(PendingList, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(wrapper.vm.loading).toBe(false)
    expect(wrapper.vm.tableData).toEqual([])
  })

  it('should open audit dialog when approve button is clicked', async () => {
    const mockResponse = {
      list: [
        {
          id: 1,
          studentId: 1,
          leaveDate: '2026-01-27',
          leaveType: 'SICK',
          reason: '感冒发烧',
        },
      ],
      total: 1,
    }

    const request = await import('../../utils/request')
    request.default.get.mockResolvedValueOnce(mockResponse)

    const wrapper = mount(PendingList, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    wrapper.vm.handleAudit(mockResponse.list[0], 'APPROVED')

    expect(wrapper.vm.dialogVisible).toBe(true)
    expect(wrapper.vm.dialogTitle).toBe('审核通过')
    expect(wrapper.vm.auditForm.result).toBe('APPROVED')
  })

  it('should open audit dialog when reject button is clicked', async () => {
    const mockResponse = {
      list: [
        {
          id: 1,
          studentId: 1,
          leaveDate: '2026-01-27',
          leaveType: 'PERSONAL',
          reason: '家中有事',
        },
      ],
      total: 1,
    }

    const request = await import('../../utils/request')
    request.default.get.mockResolvedValueOnce(mockResponse)

    const wrapper = mount(PendingList, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    wrapper.vm.handleAudit(mockResponse.list[0], 'REJECTED')

    expect(wrapper.vm.dialogVisible).toBe(true)
    expect(wrapper.vm.dialogTitle).toBe('审核不通过')
    expect(wrapper.vm.auditForm.result).toBe('REJECTED')
  })

  it('should submit audit successfully', async () => {
    const mockResponse = {
      list: [
        {
          id: 1,
          studentId: 1,
          leaveDate: '2026-01-27',
          leaveType: 'SICK',
          reason: '感冒发烧',
        },
      ],
      total: 1,
    }

    const request = await import('../../utils/request')
    request.default.get.mockResolvedValueOnce(mockResponse)
    request.default.post.mockResolvedValueOnce({})

    const wrapper = mount(PendingList, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    wrapper.vm.handleAudit(mockResponse.list[0], 'APPROVED')
    wrapper.vm.auditForm.remark = '同意请假'

    await wrapper.vm.handleConfirmAudit()

    expect(request.default.post).toHaveBeenCalledWith('/counselor/leave-requests/1/audit', {
      result: 'APPROVED',
      remark: '同意请假',
    })
    expect(wrapper.vm.dialogVisible).toBe(false)
    expect(request.default.get).toHaveBeenCalledTimes(2)
  })

  it('should handle audit submission error', async () => {
    const mockResponse = {
      list: [
        {
          id: 1,
          studentId: 1,
          leaveDate: '2026-01-27',
          leaveType: 'SICK',
          reason: '感冒发烧',
        },
      ],
      total: 1,
    }

    const request = await import('../../utils/request')
    request.default.get.mockResolvedValueOnce(mockResponse)
    request.default.post.mockRejectedValueOnce(new Error('Audit failed'))

    const wrapper = mount(PendingList, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    wrapper.vm.handleAudit(mockResponse.list[0], 'APPROVED')
    await wrapper.vm.handleConfirmAudit()

    expect(wrapper.vm.submitting).toBe(false)
  })

  it('should reload data when page size changes', async () => {
    const request = await import('../../utils/request')
    request.default.get.mockResolvedValueOnce({ list: [], total: 0 })

    const wrapper = mount(PendingList, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    wrapper.vm.query.size = 20
    await wrapper.vm.loadData()

    expect(request.default.get).toHaveBeenLastCalledWith('/counselor/leave-requests/pending', {
      params: { page: 1, size: 20 },
    })
  })

  it('should reload data when page number changes', async () => {
    const request = await import('../../utils/request')
    request.default.get.mockResolvedValueOnce({ list: [], total: 0 })

    const wrapper = mount(PendingList, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    wrapper.vm.query.page = 2
    await wrapper.vm.loadData()

    expect(request.default.get).toHaveBeenLastCalledWith('/counselor/leave-requests/pending', {
      params: { page: 2, size: 10 },
    })
  })
})
