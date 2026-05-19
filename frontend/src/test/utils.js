import { createRouter, createWebHistory } from 'vue-router'
import { vi } from 'vitest'

export function createTestRouter() {
  const routes = [
    {
      path: '/login',
      name: 'Login',
      component: { template: '<div>Login</div>' },
    },
    {
      path: '/student',
      name: 'Student',
      component: { template: '<div>Student</div>' },
      children: [
        {
          path: 'courses',
          name: 'StudentCourses',
          component: { template: '<div>Courses</div>' },
        },
      ],
    },
    {
      path: '/teacher',
      name: 'Teacher',
      component: { template: '<div>Teacher</div>' },
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
      children: [
        {
          path: 'pending',
          name: 'PendingList',
          component: { template: '<div>Pending</div>' },
        },
      ],
    },
  ]

  return createRouter({
    history: createWebHistory(),
    routes,
  })
}

export function mockAxios() {
  const mockGet = vi.fn()
  const mockPost = vi.fn()
  const mockRequest = vi.fn()

  return {
    get: mockGet,
    post: mockPost,
    request: mockRequest,
    create: vi.fn(() => ({
      get: mockGet,
      post: mockPost,
      request: mockRequest,
      interceptors: {
        request: { use: vi.fn() },
        response: { use: vi.fn() },
      },
    })),
  }
}

export function flushPromises() {
  return new Promise((resolve) => setTimeout(resolve, 0))
}
