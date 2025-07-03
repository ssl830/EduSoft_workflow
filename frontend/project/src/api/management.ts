import axios from './axios'

const ManageApi = {
  getTeachersList() {
    return axios.get(`/api/user/teachers`)
  },

  getStudentsList(){
    return axios.get(`/api/user/students`)
  },

  getTutorsList(){
    return axios.get(`/api/user/tutor`)
  },

  deleteUser(userId: number) {
    return axios.delete(`/api/user/delete/${userId}`)
  },
}

export default ManageApi

