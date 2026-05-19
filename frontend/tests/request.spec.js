import { describe, it, expect, beforeEach, vi, beforeAll } from 'vitest'
import axios from 'axios'

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn()
  }
}))

vi.mock('../src/router', () => ({
  default: {
    push: vi.fn(),
    replace: vi.fn(),
    currentRoute: { value: { path: '/login' } }
  }
}))

describe('request.js', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('should create axios instance with correct baseURL', async () => {
    const requestModule = await import('../src/utils/request.js')
    const request = requestModule.default

    expect(request.defaults.baseURL).toBe('/api')
  })

  it('should create axios instance with timeout', async () => {
    const requestModule = await import('../src/utils/request.js')
    const request = requestModule.default

    expect(request.defaults.timeout).toBe(10000)
  })

  it('should have request interceptor that adds token from localStorage', async () => {
    const requestModule = await import('../src/utils/request.js')
    const request = requestModule.default

    const originalInterceptor = request.interceptors.request.handlers[0].fulfilled

    localStorage.setItem('token', 'test-token-12345')

    const mockConfig = {
      headers: {}
    }

    const result = originalInterceptor(mockConfig)

    expect(result.headers['Authorization']).toBe('Bearer test-token-12345')
  })

  it('should have request interceptor that does not add token when not in localStorage', async () => {
    const requestModule = await import('../src/utils/request.js')
    const request = requestModule.default

    const originalInterceptor = request.interceptors.request.handlers[0].fulfilled

    localStorage.removeItem('token')

    const mockConfig = {
      headers: {}
    }

    const result = originalInterceptor(mockConfig)

    expect(result.headers['Authorization']).toBeUndefined()
  })

  it('should have response interceptor that handles successful responses with code 200', async () => {
    const requestModule = await import('../src/utils/request.js')
    const request = requestModule.default

    const originalFulfilled = request.interceptors.response.handlers[0].fulfilled

    const mockResponse = {
      data: {
        code: 200,
        message: '操作成功',
        data: { id: 1, name: '测试' }
      }
    }

    const result = originalFulfilled(mockResponse)

    expect(result).toEqual({ id: 1, name: '测试' })
  })

  it('should have response interceptor that rejects when code is not 200', async () => {
    const requestModule = await import('../src/utils/request.js')
    const request = requestModule.default

    const originalFulfilled = request.interceptors.response.handlers[0].fulfilled

    const mockResponse = {
      data: {
        code: 400,
        message: '请求参数错误',
        data: null
      }
    }

    const result = originalFulfilled(mockResponse)

    await expect(result).rejects.toThrow('请求参数错误')
  })

  it('should have response interceptor that clears token on 401', async () => {
    const requestModule = await import('../src/utils/request.js')
    const request = requestModule.default

    const originalFulfilled = request.interceptors.response.handlers[0].fulfilled

    localStorage.setItem('token', 'test-token')
    localStorage.setItem('userInfo', JSON.stringify({ id: 1 }))

    const mockResponse = {
      data: {
        code: 401,
        message: '未登录或登录已失效',
        data: null
      }
    }

    const result = originalFulfilled(mockResponse)

    await expect(result).rejects.toThrow('未登录或登录已失效')

    expect(localStorage.getItem('token')).toBeNull()
    expect(localStorage.getItem('userInfo')).toBeNull()
  })
})
