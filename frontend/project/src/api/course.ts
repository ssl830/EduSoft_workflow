import axios from './axios'

interface Course {
  id: number;
  name: string;
  code: string;
  outline?: string;
  objective?: string;
  assessment?: string;
  teacherId: number;
}

const CourseApi = {
  // Get all courses for current user
  getUserCourses(userId: string) {
    if (!userId) {
      console.error('userId is required')
      return Promise.reject(new Error('userId is required'))
    }
    return axios.get(`/api/courses/user/${userId}`)
  },

  // Get course by ID
  getCourseById(id: string) {
    return axios.get(`/api/courses/${id}`)
  },

  uploadSections(courseId: bigint, data: {sections: any[] }){
    return axios.post(`/api/courses/${courseId}/sections`, data,
      {
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );
  },

  deleteSection(courseId: bigint, sectionId: bigint) {
    return axios.delete(`/api/courses/${courseId}/sections/${sectionId}`)
  },

  // Create new course (teacher only)
  createCourse(data: {
    teacherId: bigint;
    name: string;
    code?: string;
    outline?: string;
    objective?: string;
    assessment?: string;
  }) {
    return axios.post('/api/courses', data)
  },

  // Update course (teacher only)
  updateCourse(id: string, data: {
    name?: string;
    code?: string;
    teacherId?: number;
    outline?: string;
    objective?: string;
    assessment?: string;
  }) {
    return axios.put(`/api/courses/${id}`, data)
  },

  // Create class for a course (teacher only)
  createClass(courseId: string, data: {
    name: string;
  }) {
    return axios.post(`/courses/${courseId}/classes`, data)
  },

  // Get class details
  getClassDetails(classId: string) {
    return axios.get(`/classes/${classId}`)
  },

  // Join class (student only)
  joinClass(classCode: string) {
    return axios.post('/classes/join', { class_code: classCode })
  },

  // Get students in a class (teacher/tutor only)
  getClassStudents(classId: string) {
    return axios.get(`/classes/${classId}/students`)
  },

  // Import students to a class (teacher only)
  importStudents(classId: string, file: File) {
    const formData = new FormData()
    formData.append('file', file)

    return axios.post(`/classes/${classId}/students/import`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 获取用户当前所在的班级
  getUserClasses() {
    return axios.get('/api/user/classes')
  },

  // 获取用户的默认班级（最近活跃的班级）
  getUserDefaultClass() {
    return axios.get('/api/user/default-class')
  },

  getTeacherCourses(teacherId: string) {
    return axios.get('/api/course/teacher', { params: { teacherId } })
  },

  getAllCourses() {
    return axios.get('/api/courses/list')
  },

  // 获取教师相关的所有练习信息
  getTeacherPractices() {
    return axios.get('/api/practices/teacher');
  },

  // 获取课程信息
  getCourseInfo: (courseId: number) => {
    return axios.get<Course>(`/api/course/${courseId}`);
  },

  // 生成教案
  generateTeachingContent(params: { course_name: string; course_outline: string; expected_hours: number }) {
    return axios.post('/api/ai/rag/generate', params)
  },

  generateTeachingContentDetail(params: { title: string; knowledgePoints: string[]; practiceContent: string; teachingGuidance: string; timePlan: any[] }) {
    return axios.post('/api/ai/rag/detail', params)
  },

  regenerateTeachingContent(params: { title: string; knowledgePoints: string[]; practiceContent: string; teachingGuidance: string; timePlan: any[] }) {
    return axios.post('/api/ai/rag/regenerate', params)
  },
}

export default CourseApi
export type { Course }
