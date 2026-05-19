import { vi } from 'vitest'

globalThis.localStorage = {
  _data: {},
  getItem(key) {
    return this._data[key] || null
  },
  setItem(key, value) {
    this._data[key] = String(value)
  },
  removeItem(key) {
    delete this._data[key]
  },
  clear() {
    this._data = {}
  }
}

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn()
  }
}))

vi.mock('@element-plus/icons-vue', () => ({
  User: { name: 'User' },
  UserFilled: { name: 'UserFilled' },
  Reading: { name: 'Reading' }
}))
