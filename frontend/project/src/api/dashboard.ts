import apiClient from './axios'

export const fetchDashboardOverview = async () => {
  return await apiClient.get('/api/admin/dashboard/overview')
} 