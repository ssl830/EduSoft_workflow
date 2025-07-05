import request from './axios'

export interface CourseOptimizationRequest {
  courseId: number
  sectionId: number
  averageScore: number
  errorRate: number
  studentCount: number
}

export interface CourseOptimizationResponse {
  suggestions: string[]
  relatedKnowledgePoints: string[]
  recommendedActions: string[]
}

export const getCourseOptimization = async (data: CourseOptimizationRequest): Promise<CourseOptimizationResponse> => {
  const response = await request.post('/api/admin/dashboard/optimize', data)
  return response.data
} 