import axios from './axios'

const QuestionApi = {
  getQuestionList(data: {
    courseId: number,
    sectionId: number,
  }){
    return axios.get('/api/question/list', data)
  },
  uploadQuestion(data){
    return axios.post('/api/question/upload', data)
  },
  getFavorQuestionList(data: {
    studentId: number,
    courseId: number,
    sectionId: number,
  }){
    return axios.get('/api/practice/questions/favorites', data)
  },
  getWrongQuestionList(data: {
    studentId: number,
    courseId: number,
    sectionId: number,
  }){
    return axios.get('/api/practice/questions/wrong', data)
  },
  removeWrongQuestion(questionId: string) {
    return axios.delete(`/api/practice/questions/${questionId}/wrong`)
  }
}

export default QuestionApi
