import axios from './axios'

interface LoginResponse {
  code: number;
  msg: string;
  data: {
    userInfo: {
      role: string;
      id: number;
      userid: string;
      email: string;
      username: string;
    };
    token: string;
  };
}

interface UserResponse {
  code: number;
  msg: string;
  data: {
    id: number;
    userId: string;
    username: string;
    passwordHash: string | null;
    role: string;
    email: string;
    avatar?: string; // 头像URL，可选字段
    bio?: string; // 用户简介，可选字段
    createdAt: string;
    updatedAt: string;
  };
}

interface CommonResponse {
  code: number;
  msg: string;
  data: null;
}

const AuthApi = {
  // Login
  login(data: { userId: string; password: string }) {
    // 直接使用形如 userId=xxx&password=xxx 的格式
    const formData = new URLSearchParams();
    formData.append('userId', data.userId);
    formData.append('password', data.password);
    
    return axios.post<LoginResponse>('/api/user/login', formData, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })
  },
  
  // Register
  register(data: { 
    username: string;
    passwordHash: string;
    role: string;
    email: string;
    name?: string;
    userId: string;
  }) {
    return axios.post<CommonResponse>('/api/user/register', data)
  },
  
  // Update Profile
  updateProfile(data: {
    email?: string;
    username?: string;
    bio?: string;
  }) {
    return axios.post<CommonResponse>('/api/user/update', data)
  },
  
  // Change Password
  changePassword(data: {
    oldPassword: string;
    newPassword: string;
  }) {
    console.log('发送密码修改请求到APIfox:', data);
    
    const formData = new URLSearchParams();
    formData.append('oldPassword', data.oldPassword);
    formData.append('newPassword', data.newPassword);
    
    return axios.post<CommonResponse>('/api/user/changePassword', formData, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })
  },
  
  // Get Current User Profile
  getProfile() {
    return axios.get<UserResponse>('/api/user/info')
  },
  
  // Upload Avatar
  uploadAvatar(formData: FormData) {
    return axios.post<CommonResponse>('/api/user/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // Logout
  logout() {
    return axios.post<CommonResponse>('/api/user/logout')
  }
}

export default AuthApi