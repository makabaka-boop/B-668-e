import dayjs from 'dayjs'

/**
 * 格式化日期 (YYYY-MM-DD)
 * @param {string | Date} date
 * @returns {string}
 */
export const formatDate = (date) => {
	if (!date) return ''
	return dayjs(date).format('YYYY-MM-DD')
}

/**
 * 格式化时间 (YYYY-MM-DD HH:mm:ss)
 * @param {string | Date} dateTime
 * @returns {string}
 */
export const formatDateTime = (dateTime) => {
	if (!dateTime) return ''
	return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}
