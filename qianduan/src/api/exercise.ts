import axios from './axios'

const ExerciseApi = {
  // Get exercises for a course
  getClassExercises(classId: string, data: {
    practice_id: number,
    type: string,
    name: string,
  }) {
    return axios.get(`/classes/${classId}/exercises`, data)
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
    courseId: number;
    startTime?: string;
    endTime?: string;
    timeLimit?: number;
    allowedAttempts?: number;
    questions: Array<{
      type: string;
      range: string;
      content: string;
      options?: Array<{ key: string; text: string; }>;
      explanation?: string;
      answer?: string | string[];
      points: number;
    }>;
  }) {
    return axios.post(`/api/courses/exercise`, data)
  },

  takeExercise(classId: string) {
    return axios.get(`/api/practice/${classId}`)
  },

  submitExercise(data: { studentId: any; practiceId: string; answers: any }) {
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

  favouriteQuestions(questionId: string) {
    return axios.post(`/api/practice/questions/${questionId}/favorite`
      // headers: {
      //   Authorization: `Bearer ${authStore.token}`
      // }
    )
  },
  enFavouriteQuestions(questionId: string) {
    return axios.delete(`/api/practice/questions/${questionId}/favorite`
      // headers: {
      //   Authorization: `Bearer ${authStore.token}`
      // }
    )
  },
  fetchPendingAnswers(data){
    return axios.get('/api/judge/pending', data)
  },
  gradeAnswer(data) {
    return axios.post('/api/judge/judge', data)
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

}

export default ExerciseApi
