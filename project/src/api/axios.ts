import axios from 'axios'

// Create an axios instance
const instance = axios.create({
  baseURL: 'http://127.0.0.1:4523/m1/6341276-6036787-default',  // 使用本地后端服务器
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Add a request interceptor
instance.interceptors.request.use(
  config => {
    console.log('APIfox 请求:', config.method?.toUpperCase(), config.url);
    
    // Get token from localStorage
    const token = localStorage.getItem('token')

    // If token exists, add to headers
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  error => {
    console.error('APIfox 请求错误:', error);
    return Promise.reject(error)
  }
)

// Add a response interceptor
instance.interceptors.response.use(
  response => {
    console.log('APIfox 响应:', response.status, response.config.url);
    return response
  },
  error => {
    console.log('APIfox 错误:', error.response?.status || 'Network Error', error.config?.url);
    
    // Handle common errors
    if (error.response?.status === 401) {
      // Unauthorized - clear local storage and redirect to login
      localStorage.removeItem('token')
      localStorage.removeItem('user')

      // If in browser environment and not already on login page
      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }

    return Promise.reject(error)
  }
)

export default instance
