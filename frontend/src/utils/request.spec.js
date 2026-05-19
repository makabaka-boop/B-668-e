import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import axios from 'axios'
import * as routerModule from '../router'
import * as elementPlus from 'element-plus'

vi.mock('axios')
vi.mock('element-plus', () => ({
  ElMessage: {
    error: vi.fn(),
    success: vi.fn(),
  },
}))
vi.mock('../router', () => ({
  default: {
    push: vi.fn(),
  },
}))

describe('request.js', () => {
  let request
  let mockRouter
  let mockElMessage

  beforeEach(async () => {
    vi.resetModules()
    vi.clearAllMocks()
    localStorage.clear()

    mockRouter = routerModule.default
    mockElMessage = elementPlus.ElMessage

    axios.create.mockReturnValue({
      interceptors: {
        request: { use: vi.fn() },
        response: { use: vi.fn() },
      },
      get: vi.fn(),
      post: vi.fn(),
    })

    const requestModule = await import('./request.js')
    request = requestModule.default
  })

  afterEach(() => {
    vi.resetAllMocks()
  })

  it('should create axios instance with correct config', () => {
    expect(axios.create).toHaveBeenCalledWith({
      baseURL: expect.any(String),
      timeout: 10000,
    })
  })

  it('should add Authorization header when token exists in localStorage', () => {
    localStorage.setItem('token', 'test-token-123')

    const requestInterceptor = axios.create().interceptors.request.use.mock.calls[0][0]
    const config = { headers: {} }
    const result = requestInterceptor(config)

    expect(result.headers['Authorization']).toBe('Bearer test-token-123')
  })

  it('should not add Authorization header when token does not exist', () => {
    const requestInterceptor = axios.create().interceptors.request.use.mock.calls[0][0]
    const config = { headers: {} }
    const result = requestInterceptor(config)

    expect(result.headers['Authorization']).toBeUndefined()
  })

  it('should handle request error', () => {
    const errorHandler = axios.create().interceptors.request.use.mock.calls[0][1]
    const error = new Error('Request error')

    return errorHandler(error).catch((err) => {
      expect(err).toBe(error)
    })
  })

  it('should return data directly on successful response', () => {
    const responseInterceptor = axios.create().interceptors.response.use.mock.calls[0][0]
    const response = {
      data: {
        code: 200,
        message: '成功',
        data: { id: 1, name: 'test' },
      },
    }

    const result = responseInterceptor(response)

    expect(result).toEqual({ id: 1, name: 'test' })
  })

  it('should handle business error with code 401', () => {
    localStorage.setItem('token', 'test-token')
    localStorage.setItem('userInfo', JSON.stringify({ name: 'test' }))

    const responseInterceptor = axios.create().interceptors.response.use.mock.calls[0][0]
    const response = {
      data: {
        code: 401,
        message: '未登录',
        data: null,
      },
    }

    return responseInterceptor(response).catch(() => {
      expect(localStorage.getItem('token')).toBeNull()
      expect(localStorage.getItem('userInfo')).toBeNull()
      expect(mockRouter.push).toHaveBeenCalledWith('/login')
      expect(mockElMessage.error).toHaveBeenCalledWith('未登录')
    })
  })

  it('should handle business error with code 4003', () => {
    localStorage.setItem('token', 'test-token')

    const responseInterceptor = axios.create().interceptors.response.use.mock.calls[0][0]
    const response = {
      data: {
        code: 4003,
        message: 'Token无效',
        data: null,
      },
    }

    return responseInterceptor(response).catch(() => {
      expect(localStorage.getItem('token')).toBeNull()
      expect(mockRouter.push).toHaveBeenCalledWith('/login')
    })
  })

  it('should handle business error with code 4004', () => {
    localStorage.setItem('token', 'test-token')

    const responseInterceptor = axios.create().interceptors.response.use.mock.calls[0][0]
    const response = {
      data: {
        code: 4004,
        message: 'Token已过期',
        data: null,
      },
    }

    return responseInterceptor(response).catch(() => {
      expect(localStorage.getItem('token')).toBeNull()
      expect(mockRouter.push).toHaveBeenCalledWith('/login')
    })
  })

  it('should handle other business errors', () => {
    const responseInterceptor = axios.create().interceptors.response.use.mock.calls[0][0]
    const response = {
      data: {
        code: 400,
        message: '参数错误',
        data: null,
      },
    }

    return responseInterceptor(response).catch(() => {
      expect(mockElMessage.error).toHaveBeenCalledWith('参数错误')
      expect(mockRouter.push).not.toHaveBeenCalled()
    })
  })

  it('should handle network error with 401 status', () => {
    localStorage.setItem('token', 'test-token')

    const errorHandler = axios.create().interceptors.response.use.mock.calls[0][1]
    const error = {
      response: {
        status: 401,
        data: null,
      },
    }

    return errorHandler(error).catch(() => {
      expect(localStorage.getItem('token')).toBeNull()
      expect(mockRouter.push).toHaveBeenCalledWith('/login')
    })
  })

  it('should handle network error with 403 status', () => {
    const errorHandler = axios.create().interceptors.response.use.mock.calls[0][1]
    const error = {
      response: {
        status: 403,
        data: null,
      },
    }

    return errorHandler(error).catch(() => {
      expect(mockElMessage.error).toHaveBeenCalledWith('无权限访问')
    })
  })

  it('should handle network error with 404 status', () => {
    const errorHandler = axios.create().interceptors.response.use.mock.calls[0][1]
    const error = {
      response: {
        status: 404,
        data: null,
      },
    }

    return errorHandler(error).catch(() => {
      expect(mockElMessage.error).toHaveBeenCalledWith('请求地址错误')
    })
  })

  it('should handle network error with 500 status', () => {
    const errorHandler = axios.create().interceptors.response.use.mock.calls[0][1]
    const error = {
      response: {
        status: 500,
        data: null,
      },
    }

    return errorHandler(error).catch(() => {
      expect(mockElMessage.error).toHaveBeenCalledWith('服务器内部错误')
    })
  })

  it('should handle timeout error', () => {
    const errorHandler = axios.create().interceptors.response.use.mock.calls[0][1]
    const error = {
      message: 'timeout of 10000ms exceeded',
      request: {},
    }

    return errorHandler(error).catch(() => {
      expect(mockElMessage.error).toHaveBeenCalledWith('网络请求超时')
    })
  })

  it('should handle network connection error', () => {
    const errorHandler = axios.create().interceptors.response.use.mock.calls[0][1]
    const error = {
      message: 'Network Error',
      request: {},
    }

    return errorHandler(error).catch(() => {
      expect(mockElMessage.error).toHaveBeenCalledWith('网络连接异常')
    })
  })

  it('should use custom message from error response data', () => {
    const errorHandler = axios.create().interceptors.response.use.mock.calls[0][1]
    const error = {
      response: {
        status: 400,
        data: {
          message: '自定义错误信息',
        },
      },
    }

    return errorHandler(error).catch(() => {
      expect(mockElMessage.error).toHaveBeenCalledWith('自定义错误信息')
    })
  })
})
