import axios from './axios'
import {useAuthStore} from "../stores/auth.ts";
const authStore = useAuthStore()

const QuestionApi = {
  getQuestionList(data: {
    courseId: number,
    sectionId: number,
  }){
    return axios.get('/api/question/list', {
      params: data
    })
  },
  uploadQuestion(data){
    return axios.post('/api/question/upload', data)
  },
  getFavorQuestionList(){
    return axios.get('/api/practice/questions/favorites',{
    headers: {
      "free-fs-token": authStore.token  // APIKey
    }
    })
  },
  getWrongQuestionList(){
    return axios.get('/api/practice/questions/wrong', {
      headers: {
        "free-fs-token": authStore.token  // APIKey
      }
    })
  },
  removeWrongQuestion(questionId: string) {
    return axios.delete(`/api/practice/questions/${questionId}/wrong`, {
      headers: {
        "free-fs-token": authStore.token  // APIKey
      }
    })
  }
}

export default QuestionApi
