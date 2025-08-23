import axios from './axios';

const PracticeApi = {
  // 获取教师相关的所有练习信息
  getTeacherPractices() {
    return axios.get('/api/practice/teacher/practices');
  },
  // 获取某个练习的统计信息
  getPracticeStats(practiceId: number) {
    return axios.get(`/api/practice/stats/${practiceId}`);
  },
  // 查询班级总人数
  getClassStudentCount(classId: number) {
    return axios.get(`/api/classes/${classId}/student-count`);
  },
  // 统计得分率
  updateScoreRate(practiceId: number) {
    // 必须加上 /api/practice 前缀，否则会被当成静态资源路由
    return axios.post(`/api/practice/update-score-rate/${practiceId}`);
  },
};

export default PracticeApi;
