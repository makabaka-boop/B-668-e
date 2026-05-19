import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import ElementPlus from 'element-plus'
import Login from './Login.vue'

vi.mock('../utils/request', () => ({
  default: {
    post: vi.fn(),
  },
}))

vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    default: actual,
  }
})

vi.mock('@element-plus/icons-vue', () => ({
  User: { template: '<div>User</div>' },
  Reading: { template: '<div>Reading</div>' },
  UserFilled: { template: '<div>UserFilled</div>' },
}))

describe('Login.vue', () => {
  let router

  beforeEach(() => {
    router = createRouter({
      history: createWebHistory(),
      routes: [
        {
          path: '/login',
          name: 'Login',
          component: Login,
        },
        {
          path: '/student/courses',
          name: 'StudentCourses',
          component: { template: '<div></div>' },
        },
      ],
    })

    vi.clearAllMocks()
    localStorage.clear()
  })

  it('should render login form correctly', () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    expect(wrapper.find('.title').text()).toBe('无纸化学生请假管理系统')
    expect(wrapper.find('.login-form').exists()).toBe(true)
  })

  it('should have test account buttons', () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    const testAccountsContainer = wrapper.find('.test-accounts')
    expect(testAccountsContainer.exists()).toBe(true)

    const testButtons = testAccountsContainer.findAll('.el-button')
    expect(testButtons.length).toBe(3)
  })

  it('should fill student test account when clicked', async () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    const testButtons = wrapper.find('.test-accounts').findAll('.el-button')
    await testButtons[0].trigger('click')

    expect(wrapper.vm.loginForm.username).toBe('student1')
    expect(wrapper.vm.loginForm.password).toBe('123456')
    expect(wrapper.vm.loginForm.roleType).toBe('STUDENT')
  })

  it('should fill teacher test account when clicked', async () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    const testButtons = wrapper.find('.test-accounts').findAll('.el-button')
    await testButtons[1].trigger('click')

    expect(wrapper.vm.loginForm.username).toBe('teacher1')
    expect(wrapper.vm.loginForm.password).toBe('123456')
    expect(wrapper.vm.loginForm.roleType).toBe('TEACHER')
  })

  it('should fill counselor test account when clicked', async () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    const testButtons = wrapper.find('.test-accounts').findAll('.el-button')
    await testButtons[2].trigger('click')

    expect(wrapper.vm.loginForm.username).toBe('counselor1')
    expect(wrapper.vm.loginForm.password).toBe('123456')
    expect(wrapper.vm.loginForm.roleType).toBe('COUNSELOR')
  })

  it('should have validation rules for username', () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    expect(wrapper.vm.rules.username).toBeDefined()
    expect(wrapper.vm.rules.username[0].required).toBe(true)
  })

  it('should have validation rules for password', () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    expect(wrapper.vm.rules.password).toBeDefined()
    expect(wrapper.vm.rules.password[0].required).toBe(true)
  })

  it('should have validation rules for roleType', () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    expect(wrapper.vm.rules.roleType).toBeDefined()
    expect(wrapper.vm.rules.roleType[0].required).toBe(true)
  })

  it('should handle login', async () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    wrapper.vm.loginForm.username = 'student1'
    wrapper.vm.loginForm.password = '123456'
    wrapper.vm.loginForm.roleType = 'STUDENT'

    const request = await import('../utils/request')
    request.default.post.mockResolvedValueOnce({
      token: 'mock-token',
      roleType: 'STUDENT',
      username: 'student1',
    })

    const pushSpy = vi.spyOn(router, 'push')

    await wrapper.vm.handleLogin()

    expect(request.default.post).toHaveBeenCalledWith('/auth/login', {
      username: 'student1',
      password: '123456',
      roleType: 'STUDENT',
    })

    expect(localStorage.getItem('token')).toBe('mock-token')
    expect(pushSpy).toHaveBeenCalledWith('/student/courses')
  })

  it('should handle login failure', async () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [ElementPlus, router],
      },
    })

    wrapper.vm.loginForm.username = 'student1'
    wrapper.vm.loginForm.password = '123456'
    wrapper.vm.loginForm.roleType = 'STUDENT'

    const request = await import('../utils/request')
    request.default.post.mockRejectedValueOnce(new Error('登录失败'))

    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

    await wrapper.vm.handleLogin()

    expect(request.default.post).toHaveBeenCalled()
    expect(wrapper.vm.loading).toBe(false)
    expect(localStorage.getItem('token')).toBeNull()

    consoleErrorSpy.mockRestore()
  })
})
