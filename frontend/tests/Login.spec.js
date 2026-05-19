import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import Login from '../src/views/Login.vue'

vi.mock('../src/utils/request.js', () => ({
  default: {
    post: vi.fn()
  }
}))

const mockRequest = (await import('../src/utils/request.js')).default

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn()
  })
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn()
  }
}))

const mockElMessage = (await import('element-plus')).ElMessage

describe('Login.vue', () => {
  let wrapper

  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()

    wrapper = mount(Login, {
      global: {
        stubs: {
          'el-form': { template: '<div><slot></slot></div>', emits: ['submit'] },
          'el-form-item': { template: '<div><slot></slot></div>' },
          'el-input': { template: '<input />', props: ['modelValue'], emits: ['update:modelValue'] },
          'el-select': { template: '<select><slot></slot></select>', props: ['modelValue'], emits: ['update:modelValue', 'change'] },
          'el-option': { template: '<option><slot></slot></option>', props: ['label', 'value'] },
          'el-button': { template: '<button @click="$emit(\'click\')"><slot></slot></button>', props: ['loading'], emits: ['click'] },
          'el-icon': { template: '<span><slot></slot></span>' }
        }
      }
    })
  })

  it('should render login form', () => {
    expect(wrapper.exists()).toBe(true)
  })

  it('should have default empty form values', () => {
    expect(wrapper.vm.loginForm.username).toBe('')
    expect(wrapper.vm.loginForm.password).toBe('')
    expect(wrapper.vm.loginForm.roleType).toBe('')
  })

  it('should have test accounts available', () => {
    expect(wrapper.text()).toContain('学生')
    expect(wrapper.text()).toContain('教师')
    expect(wrapper.text()).toContain('辅导员')
  })

  it('should fill student account when clicking student button', async () => {
    wrapper.vm.fillTestAccount('student1', 'STUDENT')

    expect(wrapper.vm.loginForm.username).toBe('student1')
    expect(wrapper.vm.loginForm.password).toBe('123456')
    expect(wrapper.vm.loginForm.roleType).toBe('STUDENT')
  })

  it('should fill teacher account when clicking teacher button', async () => {
    wrapper.vm.fillTestAccount('teacher1', 'TEACHER')

    expect(wrapper.vm.loginForm.username).toBe('teacher1')
    expect(wrapper.vm.loginForm.password).toBe('123456')
    expect(wrapper.vm.loginForm.roleType).toBe('TEACHER')
  })

  it('should fill counselor account when clicking counselor button', async () => {
    wrapper.vm.fillTestAccount('counselor1', 'COUNSELOR')

    expect(wrapper.vm.loginForm.username).toBe('counselor1')
    expect(wrapper.vm.loginForm.password).toBe('123456')
    expect(wrapper.vm.loginForm.roleType).toBe('COUNSELOR')
  })

  it('should have loading state initialized to false', () => {
    expect(wrapper.vm.loading).toBe(false)
  })
})
