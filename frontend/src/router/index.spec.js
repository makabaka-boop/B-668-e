import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login',
  },
  {
    path: '/login',
    name: 'Login',
    component: { template: '<div>Login</div>' },
  },
  {
    path: '/student',
    name: 'Student',
    component: { template: '<div>Student</div>' },
    redirect: '/student/courses',
    children: [
      {
        path: 'courses',
        name: 'StudentCourses',
        component: { template: '<div>Courses</div>' },
      },
      {
        path: 'leave-apply',
        name: 'LeaveApply',
        component: { template: '<div>LeaveApply</div>' },
      },
    ],
  },
  {
    path: '/teacher',
    name: 'Teacher',
    component: { template: '<div>Teacher</div>' },
    redirect: '/teacher/courses',
    children: [
      {
        path: 'courses',
        name: 'TeacherCourses',
        component: { template: '<div>Courses</div>' },
      },
    ],
  },
  {
    path: '/counselor',
    name: 'Counselor',
    component: { template: '<div>Counselor</div>' },
    redirect: '/counselor/pending',
    children: [
      {
        path: 'pending',
        name: 'PendingList',
        component: { template: '<div>Pending</div>' },
      },
    ],
  },
]

function createTestRouter() {
  const router = createRouter({
    history: createWebHistory(),
    routes,
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

describe('Router Guard', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('should redirect to login when accessing protected route without token', async () => {
    const router = createTestRouter()

    await router.push('/student/courses')

    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('should allow access to login page without token', async () => {
    const router = createTestRouter()

    await router.push('/login')

    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('should allow access to protected route with token', async () => {
    localStorage.setItem('token', 'test-token')
    const router = createTestRouter()

    await router.push('/student/courses')

    expect(router.currentRoute.value.path).toBe('/student/courses')
  })

  it('should allow access to teacher route with token', async () => {
    localStorage.setItem('token', 'test-token')
    const router = createTestRouter()

    await router.push('/teacher/courses')

    expect(router.currentRoute.value.path).toBe('/teacher/courses')
  })

  it('should allow access to counselor route with token', async () => {
    localStorage.setItem('token', 'test-token')
    const router = createTestRouter()

    await router.push('/counselor/pending')

    expect(router.currentRoute.value.path).toBe('/counselor/pending')
  })

  it('should redirect root path to login without token', async () => {
    const router = createTestRouter()

    await router.push('/')

    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('should redirect to login when token is cleared and navigating to new route', async () => {
    localStorage.setItem('token', 'test-token')
    const router = createTestRouter()

    await router.push('/student/courses')
    expect(router.currentRoute.value.path).toBe('/student/courses')

    localStorage.removeItem('token')
    await router.push('/teacher/courses')

    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('should stay on login page when already on login', async () => {
    const router = createTestRouter()

    await router.push('/login')
    await router.push('/login')

    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('should handle nested routes correctly', async () => {
    localStorage.setItem('token', 'test-token')
    const router = createTestRouter()

    await router.push('/student')

    expect(router.currentRoute.value.path).toBe('/student/courses')
  })

  it('should redirect unknown protected routes to login', async () => {
    const router = createTestRouter()

    await router.push('/unknown/route')

    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('should allow navigation between protected routes with token', async () => {
    localStorage.setItem('token', 'test-token')
    const router = createTestRouter()

    await router.push('/student/courses')
    expect(router.currentRoute.value.path).toBe('/student/courses')

    await router.push('/teacher/courses')
    expect(router.currentRoute.value.path).toBe('/teacher/courses')

    await router.push('/counselor/pending')
    expect(router.currentRoute.value.path).toBe('/counselor/pending')
  })
})
