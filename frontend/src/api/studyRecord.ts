import request from './axios'

export interface StudyRecord {
  id: number
  studentId: number
  courseId: number
  sectionId: number
  completed: boolean
  completedAt: string
  courseName: string
  sectionTitle: string
}

export default {
  // 获取所有学习记录
  getAllStudyRecords() {
    return request({
      url: '/api/record/study',
      method: 'get'
    })
  }
}
