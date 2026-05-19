import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import ElementPlus from 'element-plus'
import Login from './Login.vue'

const mockPost = vi.fn()
vi.mock('../utils/request', () => ({
  default: {
    post: (...args) => mockPost(...args)
  }
}))

const createTestRouter = () => createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/login', component: Login },
    { path: '/student/courses', component: { template: '<div>student</div>' } },
    { path: '/teacher/courses', component: { template: '<div>teacher</div>' } },
    { path: '/counselor/pending', component: { template: '<div>counselor</div>' } }
  ]
})

describe('Login.vue', () => {
  let router

  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    router = createTestRouter()
  })

  const mountLogin = async () => {
    router.push('/login')
    await router.isReady()

    const wrapper = mount(Login, {
      global: {
        plugins: [router, ElementPlus]
      }
    })
    await wrapper.vm.$nextTick()
    return wrapper
  }

  describe('页面渲染', () => {
    it('包含登录表单元素', async () => {
      const wrapper = await mountLogin()
      expect(wrapper.find('h1.title').text()).toBe('无纸化学生请假管理系统')
      expect(wrapper.find('input[placeholder="请输入用户名"]').exists()).toBe(true)
      expect(wrapper.find('input[placeholder="请输入密码"]').exists()).toBe(true)
    })

    it('包含角色选择下拉框', async () => {
      const wrapper = await mountLogin()
      const select = wrapper.find('.el-select')
      expect(select.exists()).toBe(true)
    })

    it('包含登录按钮', async () => {
      const wrapper = await mountLogin()
      const button = wrapper.find('.el-button--primary')
      expect(button.exists()).toBe(true)
      expect(button.text()).toContain('登录')
    })

    it('包含快速登录测试账号按钮', async () => {
      const wrapper = await mountLogin()
      const tips = wrapper.find('.tips')
      expect(tips.exists()).toBe(true)
    })
  })

  describe('快速填充测试账号', () => {
    it('点击学生按钮填充student1', async () => {
      const wrapper = await mountLogin()
      const buttons = wrapper.findAll('.test-accounts .el-button')
      expect(buttons.length).toBe(3)

      await buttons[0].trigger('click')
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.loginForm.username).toBe('student1')
      expect(wrapper.vm.loginForm.password).toBe('123456')
      expect(wrapper.vm.loginForm.roleType).toBe('STUDENT')
    })

    it('点击教师按钮填充teacher1', async () => {
      const wrapper = await mountLogin()
      const buttons = wrapper.findAll('.test-accounts .el-button')

      await buttons[1].trigger('click')
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.loginForm.username).toBe('teacher1')
      expect(wrapper.vm.loginForm.roleType).toBe('TEACHER')
    })

    it('点击辅导员按钮填充counselor1', async () => {
      const wrapper = await mountLogin()
      const buttons = wrapper.findAll('.test-accounts .el-button')

      await buttons[2].trigger('click')
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.loginForm.username).toBe('counselor1')
      expect(wrapper.vm.loginForm.roleType).toBe('COUNSELOR')
    })
  })

  describe('登录流程', () => {
    it('学生登录成功后跳转到/student/courses', async () => {
      mockPost.mockResolvedValue({
        token: 'test-token',
        userId: 1,
        username: 'student1',
        name: '张三',
        roleType: 'STUDENT'
      })

      const wrapper = await mountLogin()
      const vm = wrapper.vm

      vm.loginForm.username = 'student1'
      vm.loginForm.password = '123456'
      vm.loginForm.roleType = 'STUDENT'

      await vm.handleLogin()
      await wrapper.vm.$nextTick()

      expect(mockPost).toHaveBeenCalledWith('/auth/login', {
        username: 'student1',
        password: '123456',
        roleType: 'STUDENT'
      })
      expect(localStorage.getItem('token')).toBe('test-token')
    })

    it('教师登录成功后数据存储正确', async () => {
      mockPost.mockResolvedValue({
        token: 'teacher-token',
        userId: 1,
        username: 'teacher1',
        name: '王老师',
        roleType: 'TEACHER'
      })

      const wrapper = await mountLogin()
      const vm = wrapper.vm

      vm.loginForm.username = 'teacher1'
      vm.loginForm.password = '123456'
      vm.loginForm.roleType = 'TEACHER'

      await vm.handleLogin()
      await wrapper.vm.$nextTick()

      expect(localStorage.getItem('token')).toBe('teacher-token')
      const userInfo = JSON.parse(localStorage.getItem('userInfo'))
      expect(userInfo.roleType).toBe('TEACHER')
    })

    it('辅导员登录成功后数据存储正确', async () => {
      mockPost.mockResolvedValue({
        token: 'counselor-token',
        userId: 1,
        username: 'counselor1',
        name: '张辅导员',
        roleType: 'COUNSELOR'
      })

      const wrapper = await mountLogin()
      const vm = wrapper.vm

      vm.loginForm.username = 'counselor1'
      vm.loginForm.password = '123456'
      vm.loginForm.roleType = 'COUNSELOR'

      await vm.handleLogin()
      await wrapper.vm.$nextTick()

      expect(localStorage.getItem('token')).toBe('counselor-token')
    })

    it('登录失败时不存储token', async () => {
      mockPost.mockRejectedValue(new Error('用户名或密码错误'))

      const wrapper = await mountLogin()
      const vm = wrapper.vm

      vm.loginForm.username = 'student1'
      vm.loginForm.password = 'wrong'
      vm.loginForm.roleType = 'STUDENT'

      await vm.handleLogin()
      await wrapper.vm.$nextTick()

      expect(localStorage.getItem('token')).toBeNull()
    })

    it('loading状态下不重复提交', async () => {
      let resolvePost
      mockPost.mockImplementation(() => new Promise(resolve => { resolvePost = resolve }))

      const wrapper = await mountLogin()
      const vm = wrapper.vm

      vm.loginForm.username = 'student1'
      vm.loginForm.password = '123456'
      vm.loginForm.roleType = 'STUDENT'

      vm.loading = true

      await vm.handleLogin()

      expect(mockPost).not.toHaveBeenCalled()

      vm.loading = false
      resolvePost && resolvePost({})
    })
  })
})
