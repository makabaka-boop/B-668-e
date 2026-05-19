import { describe, it, expect } from 'vitest'
import { formatDate, formatDateTime } from './date'

describe('date 工具函数', () => {
  describe('formatDate', () => {
    it('格式化日期为 YYYY-MM-DD', () => {
      const result = formatDate('2026-01-20T10:30:00')
      expect(result).toBe('2026-01-20')
    })

    it('传入Date对象格式化正确', () => {
      const date = new Date(2026, 0, 20)
      const result = formatDate(date)
      expect(result).toBe('2026-01-20')
    })

    it('传入null返回空字符串', () => {
      expect(formatDate(null)).toBe('')
    })

    it('传入undefined返回空字符串', () => {
      expect(formatDate(undefined)).toBe('')
    })

    it('传入空字符串返回空字符串', () => {
      expect(formatDate('')).toBe('')
    })
  })

  describe('formatDateTime', () => {
    it('格式化时间为 YYYY-MM-DD HH:mm:ss', () => {
      const result = formatDateTime('2026-01-20T10:30:00')
      expect(result).toBe('2026-01-20 10:30:00')
    })

    it('传入Date对象格式化正确', () => {
      const date = new Date(2026, 0, 20, 10, 30, 0)
      const result = formatDateTime(date)
      expect(result).toBe('2026-01-20 10:30:00')
    })

    it('传入null返回空字符串', () => {
      expect(formatDateTime(null)).toBe('')
    })

    it('传入undefined返回空字符串', () => {
      expect(formatDateTime(undefined)).toBe('')
    })

    it('传入空字符串返回空字符串', () => {
      expect(formatDateTime('')).toBe('')
    })
  })
})
