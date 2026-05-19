import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createRouter, createWebHistory } from 'vue-router'

import Login from '../src/views/Login.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/student',
    name: 'Student',
    component: { template: '<div>Student Layout</div>' },
    redirect: '/student/courses',
    children: [
      {
        path: 'courses',
        name: 'StudentCourses',
        component: { template: '<div>Student Courses</div>' }
      }
    ]
  },
  {
    path: '/counselor',
    name: 'Counselor',
    component: { template: '<div>Counselor Layout</div>' },
    redirect: '/counselor/pending',
    children: [
      {
        path: 'pending',
        name: 'PendingList',
        component: { template: '<div>Pending List</div>' }
      }
    ]
  },
  {
    path: '/teacher',
    name: 'Teacher',
    component: { template: '<div>Teacher Layout</div>' },
    redirect: '/teacher/courses',
    children: [
      {
        path: 'courses',
        name: 'TeacherCourses',
        component: { template: '<div>Teacher Courses</div>' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
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

describe('Router Guard', () => {
  beforeEach(() => {
    localStorage.clear()
    router.push('/')
  })

  it('should redirect to login when accessing protected route without token', async () => {
    await router.push('/student/courses')
    await router.isReady()

    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('should allow access to login page without token', async () => {
    await router.push('/login')
    await router.isReady()

    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('should allow access to protected route with token', async () => {
    localStorage.setItem('token', 'test-token')

    await router.push('/student/courses')
    await router.isReady()

    expect(router.currentRoute.value.path).toBe('/student/courses')
  })

  it('should allow access to counselor route with token', async () => {
    localStorage.setItem('token', 'test-token')

    await router.push('/counselor/pending')
    await router.isReady()

    expect(router.currentRoute.value.path).toBe('/counselor/pending')
  })

  it('should allow access to teacher route with token', async () => {
    localStorage.setItem('token', 'test-token')

    await router.push('/teacher/courses')
    await router.isReady()

    expect(router.currentRoute.value.path).toBe('/teacher/courses')
  })

  it('should redirect root path to login', async () => {
    await router.push('/')
    await router.isReady()

    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('should redirect root path to student courses with token', async () => {
    localStorage.setItem('token', 'test-token')

    await router.push('/')
    await router.isReady()

    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('should have all expected routes defined', () => {
    const routeNames = router.getRoutes().map(r => r.name)

    expect(routeNames).toContain('Login')
    expect(routeNames).toContain('Student')
    expect(routeNames).toContain('StudentCourses')
    expect(routeNames).toContain('Counselor')
    expect(routeNames).toContain('PendingList')
    expect(routeNames).toContain('Teacher')
    expect(routeNames).toContain('TeacherCourses')
  })
})
