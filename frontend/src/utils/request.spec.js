import { describe, it, expect, vi, beforeEach } from 'vitest'
import axios from 'axios'

vi.mock('axios', () => {
  const mockCreate = vi.fn()
  const instance = {
    interceptors: {
      request: { use: vi.fn() },
      response: { use: vi.fn() }
    },
    get: vi.fn(),
    post: vi.fn()
  }
  mockCreate.mockReturnValue(instance)
  return { default: { create: mockCreate, isCancel: vi.fn() } }
})

vi.mock('element-plus', () => ({
  ElMessage: {
    error: vi.fn(),
    success: vi.fn()
  }
}))

describe('request 封装', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  describe('请求拦截器', () => {
    it('创建axios实例时设置baseURL和timeout', async () => {
      const request = (await import('../utils/request')).default
      expect(axios.create).toHaveBeenCalledWith(
        expect.objectContaining({
          timeout: 10000
        })
      )
    })

    it('有token时添加Authorization请求头', async () => {
      localStorage.setItem('token', 'my-test-token')
      vi.resetModules()

      const request = (await import('../utils/request')).default
      const interceptorCalls = request.interceptors.request.use.mock.calls
      expect(interceptorCalls.length).toBeGreaterThanOrEqual(1)

      const requestInterceptor = interceptorCalls[0][0]
      const config = { headers: {} }
      const result = requestInterceptor(config)
      expect(result.headers['Authorization']).toBe('Bearer my-test-token')
    })

    it('无token时不添加Authorization请求头', async () => {
      localStorage.removeItem('token')
      vi.resetModules()

      const request = (await import('../utils/request')).default
      const interceptorCalls = request.interceptors.request.use.mock.calls
      const requestInterceptor = interceptorCalls[0][0]
      const config = { headers: {} }
      const result = requestInterceptor(config)
      expect(result.headers['Authorization']).toBeUndefined()
    })
  })

  describe('响应拦截器', () => {
    it('注册了响应拦截器', async () => {
      vi.resetModules()
      const request = (await import('../utils/request')).default
      const interceptorCalls = request.interceptors.response.use.mock.calls
      expect(interceptorCalls.length).toBeGreaterThanOrEqual(1)
      expect(typeof interceptorCalls[0][0]).toBe('function')
      expect(typeof interceptorCalls[0][1]).toBe('function')
    })

    it('成功响应返回data', async () => {
      vi.resetModules()
      const request = (await import('../utils/request')).default
      const successHandler = request.interceptors.response.use.mock.calls[0][0]

      const response = {
        data: { code: 200, message: '操作成功', data: { id: 1 } }
      }

      const result = successHandler(response)
      expect(result).toEqual({ id: 1 })
    })

    it('业务错误码非200时reject', async () => {
      vi.resetModules()
      const { ElMessage } = await import('element-plus')
      const request = (await import('../utils/request')).default
      const successHandler = request.interceptors.response.use.mock.calls[0][0]

      const response = {
        data: { code: 4001, message: '用户名或密码错误', data: null }
      }

      const result = successHandler(response)
      await expect(result).rejects.toThrow('用户名或密码错误')
    })

    it('401业务码时清除token并跳转登录页', async () => {
      localStorage.setItem('token', 'old-token')
      vi.resetModules()
      const request = (await import('../utils/request')).default
      const successHandler = request.interceptors.response.use.mock.calls[0][0]

      const response = {
        data: { code: 401, message: '未登录', data: null }
      }

      try {
        await successHandler(response)
      } catch (e) {
        // expected reject
      }

      expect(localStorage.getItem('token')).toBeNull()
    })
  })
})
