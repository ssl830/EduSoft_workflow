import axios from './axios'

const ResourceApi = {
  // 获取课程视频列表
  getChapterResources(courseId: string, formData: FormData){
    return axios.post(`/api/resources/chapter/${courseId}`, formData)
  },
  // 记录视频播放进度
  recordWatchProgress(formData: FormData){
    return axios.post('/api/resources/progress', formData)
  },
  // 上传视频
  uploadVideo(formData: FormData, onUploadProgress?: (progressEvent: number) => void) {
    return axios.post(`/api/resources/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: progressEvent => {
        if (progressEvent.total && onUploadProgress) {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onUploadProgress(percentCompleted)
        }
      }
    })
  },
 


  // Get resources for a course 
  getCourseResources(courseId: number, data: {
    courseId: number;
    userId: number;
    chapter?: number; 
    type?: string;
    title?: string;
    isTeacher: boolean;
  }) {
    return axios.post(`/api/courses/${courseId}/filelist`, { 
      ...data,
      courseId: Number(courseId), 
    })
  },

  // Upload resource
  uploadResource(courseId: string, formData: FormData, onUploadProgress?: (progressEvent: number) => void) {
    return axios.post(`/api/courses/${courseId}/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: progressEvent => {
        if (progressEvent.total && onUploadProgress) {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onUploadProgress(percentCompleted)
        }
      }
    })
  },

  // Download resource
  downloadResource(resourceId: string) {
    return axios.get<{
      code: number;
      message: string;
      data: {
        url: string;
        fileName: string;
        fileType?: string;
        expiresIn: number;
      }
    }>(`/api/resources/${resourceId}/download`)
  },

  // Preview resource
  previewResource(resourceId: string) {
    return axios.get<{
      code: number;
      message: string;
      data: {
        url: string;
        fileName: string;
        fileType?: string;
        expiresIn: number;
      }
    }>(`/api/resources/${resourceId}/preview`)
  },

  // Update resource (teacher/tutor only)
  updateResource(resourceId: string, data: {
    title?: string;
    chapter?: string;
    type?: string;
    visible_to_classes?: string[];
    new_file?: File;
  }) {
    const formData = new FormData()

    if (data.title) formData.append('title', data.title)
    if (data.chapter) formData.append('chapter', data.chapter)
    if (data.type) formData.append('type', data.type)

    if (data.visible_to_classes) {
      data.visible_to_classes.forEach((classId, index) => {
        formData.append(`visible_to_classes[${index}]`, classId)
      })
    }

    if (data.new_file) formData.append('new_file', data.new_file)

    return axios.put(`/resources/${resourceId}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // Delete resource (teacher/tutor only)
  deleteResource(resourceId: string) {
    return axios.delete(`/resources/${resourceId}`)
  },

  // Update resource duration
  updateResourceDuration(resourceId: string, duration: number) {
    return axios.put(`/api/resources/${resourceId}/duration`, { duration })
  },

  // Get video progress
  getVideoProgress(resourceId: string, studentId: string) {
    return axios.get<{
      code: number;
      message: string;
      data: {
        progress: number;
        lastPosition: number;
        watchCount: number;
      }
    }>(`/api/resources/progress/${resourceId}/${studentId}`)
  }
}

export default ResourceApi
