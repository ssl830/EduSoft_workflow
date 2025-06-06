import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import authApi from '../api/auth'

interface User {
  id: number;
  userId: string;
  username: string;
  email: string;
  role: string;
}

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  
  // Initialize state from localStorage
  if (typeof window !== 'undefined') {
    const savedUser = localStorage.getItem('user')
    const savedToken = localStorage.getItem('token')
    
    if (savedUser) user.value = JSON.parse(savedUser)
    if (savedToken) token.value = savedToken
  }
  
  // Computed
  const isAuthenticated = computed(() => !!token.value)
  const userRole = computed(() => user.value?.role || '')
  
  // Actions
  const login = async (userId: string, password: string) => {
    try {
      const response = await authApi.login({ userId, password })
      
      if (response.data.code === 200) {
        const userData = {
          id: response.data.data.userInfo.id,
          userId: response.data.data.userInfo.userid,
          username: response.data.data.userInfo.username,
          email: response.data.data.userInfo.email,
          role: response.data.data.userInfo.role
        }
        
        user.value = userData
        token.value = response.data.data.token
        
        // Save to localStorage
        localStorage.setItem('user', JSON.stringify(userData))
        localStorage.setItem('token', response.data.data.token)
        
        return response.data
      } else {
        throw new Error(response.data.msg || '登录失败')
      }
    } catch (error) {
      console.error('Login error:', error)
      throw error
    }
  }
  
  const register = async (data: {
    username: string;
    password: string;
    role: string;
    email: string;
    name?: string;
    userId: string;
  }) => {
    try {
      const registerData = {
        ...data,
        passwordHash: data.password // 后端需要 passwordHash 而不是 password
      }
      delete (registerData as any).password // 删除 password 字段

      const response = await authApi.register(registerData)
      
      if (response.data.code === 200) {
        return response.data
      } else {
        throw new Error(response.data.msg || '注册失败')
      }
    } catch (error) {
      console.error('Register error:', error)
      throw error
    }
  }
  
  const logout = async () => {
    try {
      if (token.value) {
        const response = await authApi.logout()
        if (response.data.code !== 200) {
          console.warn('Logout API failed:', response.data.msg)
        }
      }
      
      // Clear local state
      user.value = null
      token.value = null
      
      // Clear localStorage
      localStorage.removeItem('user')
      localStorage.removeItem('token')
    } catch (error) {
      console.error('Logout error:', error)
      // 即使API调用失败，也需要清除本地状态
      user.value = null
      token.value = null
      localStorage.removeItem('user')
      localStorage.removeItem('token')
      throw error
    }
  }
    const fetchUserInfo = async () => {
    try {
      const response = await authApi.getProfile()
      
      console.log('API响应:', response.data);
      
      if (response.data.code === 200) {
        const userData = {
          id: response.data.data.id,
          userId: response.data.data.userId,
          username: response.data.data.username,
          email: response.data.data.email,
          role: response.data.data.role
        }
        
        user.value = userData
        // Update localStorage
        localStorage.setItem('user', JSON.stringify(userData))
        
        return response.data.data
      } else {
        // 处理非200响应
        const errorMsg = response.data.msg || '获取用户信息失败';
        console.error('API返回错误:', errorMsg);
        throw new Error(errorMsg)
      }
    } catch (error: any) {
      console.error('Fetch user info error:', error)
      
      // 检查是否是网络错误或API错误
      if (error.response) {
        // API返回了错误响应
        const errorMsg = error.response.data?.msg || error.response.data?.message || '服务器错误';
        throw new Error(errorMsg)
      } else if (error.request) {
        // 请求发出但没有收到响应
        throw new Error('网络连接失败，请检查网络连接')
      } else {
        // 其他错误
        throw error
      }
    }
  }
    const updateProfile = async (data: {
    email?: string;
    username?: string;
    bio?: string; // 添加简介字段
  }) => {
    try {
      const response = await authApi.updateProfile(data)
      
      if (response.data.code === 200) {
        // 更新成功后重新获取用户信息
        await fetchUserInfo()
        return response.data
      } else {
        throw new Error(response.data.msg || '更新用户信息失败')
      }
    } catch (error) {
      console.error('Update profile error:', error)
      throw error
    }  }
    const changePassword = async (oldPassword: string, newPassword: string) => {
    try {
      console.log('密码修改请求: 发送到APIfox');
      
      const response = await authApi.changePassword({ oldPassword, newPassword })
      
      console.log('APIfox 密码修改响应:', response.status, response.data);
      
      // 检查APIfox响应格式
      const responseData = response.data as any;
      const isSuccess = response.status === 200 || 
                       responseData?.code === 200 || 
                       responseData?.code === '200' ||
                       responseData?.success === true ||
                       (!responseData?.error && !responseData?.err);
      
      if (isSuccess) {
        console.log('APIfox: 密码修改成功');
        return response.data
      } else {
        const errorMsg = responseData?.msg || responseData?.message || responseData?.error || responseData?.err || '密码修改失败';
        console.error('Auth store: 密码修改API返回错误:', errorMsg);
        throw new Error(errorMsg)
      }    } catch (error: any) {
      console.log('APIfox 密码修改错误:', error.response?.status || 'Network Error');
      
      if (error.response) {
        const errorMsg = error.response.data?.msg || error.response.data?.message || '服务器返回错误';
        throw new Error(errorMsg)
      } else if (error.request) {
        throw new Error('网络连接失败，请检查网络连接')
      } else {
        throw error
      }
    }
  }

  const uploadAvatar = async (file: File) => {
    try {
      const response = await authApi.uploadAvatar(file)
      
      if (response.data.code === 200) {
        // 上传成功后重新获取用户信息
        await fetchUserInfo()
        return response.data
      } else {
        throw new Error(response.data.msg || '头像上传失败')
      }
    } catch (error) {
      console.error('Upload avatar error:', error)
      throw error
    }
  }

  return {
    user,
    token,
    isAuthenticated,
    userRole,
    login,
    register,
    logout,
    fetchUserInfo,
    updateProfile,
    changePassword,
    uploadAvatar
  }
})