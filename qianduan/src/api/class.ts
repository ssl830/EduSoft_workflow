import axios from './axios'

const ClassApi = {
  // Get all classes for current user
  getUserClasses(id: string) {
    return axios.get(`/api/classes/user/${id}`)
  },

  uploadStudents(data: { classId: string, operatorId: string, students: any[] }) {
    return axios.post(`/api/imports/students`, data,
      {
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );
  },

  // Get class by ID
  getClassById(classID: string) {
    return axios.get(`/api/classes/${classID}`)
  },

  // Create new class (teacher only)
  createClass(data: {
    courseId: string;
    name: string;
    code?: string;
  }) {
    return axios.post('/api/classes', data)
  },

  getHomeworkList(classID: string) {
    return axios.get(`/api/homework/list?classId=${classID}`)
  },

  deleteHomework(homeworkId: string) {
    return axios.delete(`/api/homework/${homeworkId}`)
  },

  fetchSubmissions(homeworkId: string) {
    return axios.get(`/api/homework/submissions/${homeworkId}`)
  },

  // Download resource
  downloadSubmissionFile(submissionId: string) {
    return axios.get(`/api/homework/submission/file/${submissionId}`)
  },

  // Upload resource
  uploadSubmissionFile(homeworkId: string, formData: FormData) {
    return axios.post(`/api/homework/submit/${homeworkId}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
    })
  },

  createHomework(formData: FormData) {
    return axios.post('/api/homework/create', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
    })
  },

  downloadHomeworkFile(homeworkId: string) {
    return axios.get(`/api/homework/file/${homeworkId}`)
  },

  // Update class (teacher only)
  updateClass(id: string, data: {
    name?: string;
    code?: string;
    syllabus?: string;
    objectives?: string;
    assessment?: string;
  }) {
    return axios.put(`/classes/${id}`, data)
  },

  // Get classes for a class
  getClassClasses(classId: string) {
    return axios.get(`/classes/${classId}/classes`)
  },

  // Create class for a class (teacher only)
  // createClass(classId: string, data: {
  //   name: string;
  // }) {
  //   return axios.post(`/classes/${classId}/classes`, data)
  // },

  // Get class details
  getClassDetails(classId: string) {
    return axios.get(`/classes/${classId}`)
  },

  // Join class (student only)
  joinClass(userId: string, classCode: string) {
    return axios.post(`/api/classes/join/${userId}`, { classCode: classCode })
  },

  // Get students in a class (teacher/tutor only)
  getClassStudents(classId: string) {
    return axios.get(`/api/classes/${classId}/users`)
  },

  deleteStudents(classId: string, userId: string){
    return axios.delete(`/api/classes/${classId}/leave/${userId}`)
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
  }
}

export default ClassApi
