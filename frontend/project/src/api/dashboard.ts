import apiClient from './axios'
import type  {ApiResponse} from './axios'

export const fetchDashboardOverview = async (): Promise<ApiResponse<any>> => {
  return await apiClient.get('/api/admin/dashboard/overview')
} 