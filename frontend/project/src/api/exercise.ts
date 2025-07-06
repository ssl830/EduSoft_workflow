import axios from './axios'

import {useAuthStore} from "../stores/auth.ts";
const authStore = useAuthStore()

const ExerciseApi = {
  // Get exercises for a course
  getClassExercises(classId: string, data: {
    practice_id: number,
    type: string,
    name: string,
  }) {
    return axios.get(`/classes/${classId}/exercises`, data)
  },

  // 从题库中导入题目
  importQuestionsToPractice(data){
    return axios.post('/api/practice/question/import', data)
  },

  // 获取待批改练习列表
  getPendingJudgeList(data:{
    practiceId: bigint,
    classId: string,
  }){
    console.log("data:", data)
    console.log()
    return axios.post('/api/judge/pendinglist', data)
  },

  // Get teacher's courses (for exercise creation)
  getTeacherCourses() {
    return axios.get('/courses/teaching')
  },

  // Get classes for a course
  getCourseClasses(courseId: string) {
    return axios.get(`/courses/${courseId}/classes`)
  },

  // Create exercise (teacher/tutor only)
  createExercise(data: {
    title: string;
    classId: number;
    courseId: number;
    startTime?: string;
    endTime?: string;
    createdBy?: string | number;
    allowMultipleSubmission?: boolean;
  }) {
    return axios.post(`/api/practice/create`, data)
  },

  deleteExercise(id: string) {
    return axios.delete(`/api/practice/${id}`)
  },

  takeExercise(practiceId: string) {
    return axios.get(`/api/practice/${practiceId}`)
  },

  submitExercise(data: { practiceId: string; studentId: string; answers: any }) {
    return axios.post('/api/submission/submit', data)
  },

  // Get exercise details
  getExerciseDetails(exerciseId: string, data: {submissionId: any}) {
    return axios.get(`/api/practice/${exerciseId}`, data
      // ,{
      // headers: {
      //     Authorization: `Bearer ${authStore.token}`
      // }
    // }
    )
  },

  // 获取学生所有练习
  getPracticeList(studentId: string, classId: string) {
    const params = new URLSearchParams({
      studentId,
      classId
    });
    return axios.get(`/api/practice/student/list?${params.toString()}`)
  },

  getPracticeTeachList(classId: string) {
    const params = new URLSearchParams({
      classId
    });
    return axios.get(`/api/practice/list/teacher?${params.toString()}`)
  },

  favouriteQuestions(questionId: string) {
    return axios.post(`/api/practice/questions/${questionId}/favorite`,{
        headers: {
          "free-fs-token": authStore.token  // APIKey
        }
      }
    )
  },
  enFavouriteQuestions(questionId: string) {
    return axios.delete(`/api/practice/questions/${questionId}/favorite`,{
        headers: {
          "free-fs-token": authStore.token  // APIKey
        }
      }
    )
  },
  fetchPendingAnswers(data){
    console.log(data)
    console.log()
    return axios.post('/api/judge/pending', data)
  },

  gradeAnswer(data: {
    submissionId: string | number,
    questions: Array<{
      answerText: string,
      score: number,
      maxScore: number,
      sortOrder: number
    }>
  }) {
    // 确保submissionId是数字类型
    const payload = {
      submissionId: Number(data.submissionId),
      questions: data.questions
    }
    return axios.post('/api/judge/judge', payload)
  },

  // Start exercise attempt (student only)
  startExerciseAttempt(exerciseId: string) {
    return axios.post(`/practices/${exerciseId}/attempts`)
  },

  // Submit answer (student only)
  submitAnswer(attemptId: string, data: {
    question_id: string;
    answer_content: string | string[] | object;
  }) {
    return axios.post(`/attempts/${attemptId}/answers`, data)
  },

  // Finish exercise attempt (student only)
  finishAttempt(attemptId: string) {
    return axios.post(`/attempts/${attemptId}/finish`)
  },

  // Get exercise result (student)
  getExerciseResult(attemptId: string) {
    return axios.get(`/attempts/${attemptId}/result`)
  },

  // Get all results for an exercise (teacher/tutor)
  getExerciseResults(exerciseId: string) {
    return axios.get(`/practices/${exerciseId}/results`)
  },

  // Grade a subjective answer (teacher/tutor)

  // 获取某个练习的题目列表
  getPracticeQuestions(practiceId: number|string) {
    return axios.get(`/api/practice/${practiceId}/questions`)
  },

  // 更新练习基本信息
  updateExercise(practiceId: number|string, data: any) {
    return axios.put(`/api/practice/${practiceId}`, data)
  },

  // 修改练习中题目的分值
  updatePracticeQuestionScore(practiceId: number|string, questionId: number|string, score: number) {
    return axios.put(`/api/practice/${practiceId}/questions/${questionId}?score=${score}`)
  },

  // 移除练习中的题目
  removePracticeQuestion(practiceId: number|string, questionId: number|string) {
    return axios.delete(`/api/practice/${practiceId}/questions/${questionId}`)
  },
}

export default ExerciseApi
