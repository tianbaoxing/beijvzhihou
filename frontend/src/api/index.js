import axios from 'axios'
import { useUserStore } from '../stores/user'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截：自动带 token
http.interceptors.request.use(config => {
  const store = useUserStore()
  if (store.token) {
    config.headers.Authorization = `Bearer ${store.token}`
  }
  return config
})

// 响应拦截：统一错误处理
http.interceptors.response.use(
  res => {
    const data = res.data
    if (data.code && data.code !== 200) {
      return Promise.reject(data)
    }
    return data
  },
  err => {
    if (err.response?.status === 401) {
      const store = useUserStore()
      store.logout()
      const current = window.location.pathname
      const redirect = current !== '/login' && current !== '/register' ? `?redirect=${encodeURIComponent(current)}` : ''
      window.location.href = `/login${redirect}`
    }
    return Promise.reject(err.response?.data || err)
  }
)

// ===== Auth API =====
export const authApi = {
  sendCode: (email, type) => http.post('/auth/send-code', { email, type }),
  register: (data) => http.post('/auth/register', data),
  login: (data) => http.post('/auth/login', data),
  getMe: () => http.get('/auth/me'),
  updateProfile: (data) => http.put('/auth/me', data)
}

// ===== Post API =====
export const postApi = {
  list: (page = 1, size = 20, sort = 'time') => 
    http.get('/posts', { params: { page, size, sort } }),
  detail: (id, fingerprint) => http.get(`/posts/${id}`, { params: { fingerprint } }),
  create: (data) => http.post('/posts', data),
  toggleLike: (id, fingerprint) => http.post(`/posts/${id}/like`, { fingerprint })
}

// ===== Comment API =====
export const commentApi = {
  list: (postId, page = 1, size = 10) =>
    http.get(`/posts/${postId}/comments`, { params: { page, size } }),
  create: (postId, data) => http.post(`/posts/${postId}/comments`, data)
}

// ===== Browser Fingerprint =====
export function getBrowserFingerprint() {
  let fp = localStorage.getItem('browser_fp')
  if (!fp) {
    fp = 'fp_' + Date.now().toString(36) + '_' + Math.random().toString(36).substring(2, 10)
    localStorage.setItem('browser_fp', fp)
  }
  return fp
}

// ===== Admin API =====
export const adminApi = {
  pendingPosts: () => http.get('/admin/review/pending'),
  reviewPost: (id, result) => http.post(`/admin/review/${id}`, { result }),
  stats: () => http.get('/admin/stats'),
  listUsers: (page = 1, size = 20) => http.get('/admin/users', { params: { page, size } }),
  listPosts: (page = 1, size = 20, status) => http.get('/admin/posts', { params: { page, size, status } })
}

export default http
