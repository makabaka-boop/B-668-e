import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createRouter, createMemoryHistory } from 'vue-router'
import routerGuard from './index'

const routes = [
  { path: '/login', component: { template: '<div>login</div>' } },
  { path: '/student/courses', component: { template: '<div>student</div>' }, meta: { requiresAuth: true } },
  { path: '/counselor/pending', component: { template: '<div>counselor</div>' }, meta: { requiresAuth: true } },
  { path: '/teacher/courses', component: { template: '<div>teacher</div>' }, meta: { requiresAuth: true } }
]

describe('路由守卫', () => {
  let router

  beforeEach(() => {
    localStorage.clear()
  })

  const createTestRouter = () => {
    router = createRouter({
      history: createMemoryHistory(),
      routes
    })

    router.beforeEach((to, from, next) => {
      const token = localStorage.getItem('token')
      if (to.path !== '/login' && !token) {
        next('/login')
      } else {
        next()
      }
    })

    return router
  }

  describe('未登录状态', () => {
    it('访问登录页直接放行', async () => {
      const router = createTestRouter()
      await router.push('/login')
      await router.isReady()
      expect(router.currentRoute.value.path).toBe('/login')
    })

    it('访问学生页面重定向到登录页', async () => {
      const router = createTestRouter()
      await router.push('/student/courses')
      await router.isReady()
      expect(router.currentRoute.value.path).toBe('/login')
    })

    it('访问辅导员页面重定向到登录页', async () => {
      const router = createTestRouter()
      await router.push('/counselor/pending')
      await router.isReady()
      expect(router.currentRoute.value.path).toBe('/login')
    })

    it('访问教师页面重定向到登录页', async () => {
      const router = createTestRouter()
      await router.push('/teacher/courses')
      await router.isReady()
      expect(router.currentRoute.value.path).toBe('/login')
    })
  })

  describe('已登录状态', () => {
    beforeEach(() => {
      localStorage.setItem('token', 'test-token')
    })

    it('访问学生页面正常放行', async () => {
      const router = createTestRouter()
      await router.push('/student/courses')
      await router.isReady()
      expect(router.currentRoute.value.path).toBe('/student/courses')
    })

    it('访问辅导员页面正常放行', async () => {
      const router = createTestRouter()
      await router.push('/counselor/pending')
      await router.isReady()
      expect(router.currentRoute.value.path).toBe('/counselor/pending')
    })

    it('访问教师页面正常放行', async () => {
      const router = createTestRouter()
      await router.push('/teacher/courses')
      await router.isReady()
      expect(router.currentRoute.value.path).toBe('/teacher/courses')
    })

    it('已登录时也可以访问登录页', async () => {
      const router = createTestRouter()
      await router.push('/login')
      await router.isReady()
      expect(router.currentRoute.value.path).toBe('/login')
    })
  })
})
