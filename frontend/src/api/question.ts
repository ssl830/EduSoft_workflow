import axios from './axios'
import {useAuthStore} from "../stores/auth.ts";
const authStore = useAuthStore()

const QuestionApi = {
  getQuestionList(data: { courseId?: number } = {}){
    return axios.get('/api/practice/question/list', { params: data })
  },
  uploadQuestion(data: any){
    return axios.post('/api/question/upload', data)
  },
  createQuestion(data: any){
    return axios.post('/api/practice/question/create', data)
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
  },
  addWrongQuestion(questionId: number, data: { wrongAnswer: string}) {
    return axios.post(`/api/practice/questions/${questionId}/wrong`, data, {
      headers: {
        "free-fs-token": authStore.token
      }
    })
  },
  generateExercise(data: any) {
    return axios.post('/api/ai/rag/generate_exercise', data);
  }
}

export default QuestionApi
