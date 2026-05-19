import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
	baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
	timeout: 10000,
})

// 请求拦截器
request.interceptors.request.use(
	(config) => {
		const token = localStorage.getItem('token')
		if (token) {
			config.headers['Authorization'] = `Bearer ${token}`
		}
		return config
	},
	(error) => {
		console.error('请求错误:', error)
		return Promise.reject(error)
	},
)

// 响应拦截器
request.interceptors.response.use(
	(response) => {
		const res = response.data

		// 如果返回的状态码不是200，说明接口请求失败
		if (res.code !== 200) {
			// 业务错误时，统一报错
			ElMessage.error(res.message || '操作失败')

			// 401: 未登录或登录失效 (业务码)
			if (res.code === 401 || res.code === 4003 || res.code === 4004) {
				localStorage.removeItem('token')
				localStorage.removeItem('userInfo')
				router.push('/login')
			}

			return Promise.reject(new Error(res.message || '操作失败'))
		}

		return res.data
	},
	(error) => {
		console.error('响应错误:', error)

		// 如果是取消请求，则不进行语义化报错
		if (axios.isCancel(error)) return Promise.reject(error)

		// 如果是业务层面已处理的错误（由成功回调中 reject 的），不再重复提示
		if (!error.response && !error.request) {
			return Promise.reject(error)
		}

		let message = '网络连接异常'
		if (error.response) {
			const { status, data } = error.response
			// 如果后端在 HTTP 错误响应中也带了业务 message，则优先使用
			if (data && data.message) {
				message = data.message
			} else {
				// 根据 HTTP 状态码给出默认提示
				switch (status) {
					case 401:
						message = '未登录或登录已失效'
						localStorage.removeItem('token')
						localStorage.removeItem('userInfo')
						router.push('/login')
						break
					case 403:
						message = '无权限访问'
						break
					case 404:
						message = '请求地址错误'
						break
					case 500:
						message = '服务器内部错误'
						break
					default:
						message = error.message || '网络请求错误'
				}
			}
		} else if (error.message && error.message.includes('timeout')) {
			message = '网络请求超时'
		}

		ElMessage.error(message)
		return Promise.reject(error)
	},
)

export default request
