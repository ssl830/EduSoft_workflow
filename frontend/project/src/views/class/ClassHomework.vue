const fetchHomeworks = async () => {
loading.value = true
try {
const response = await HomeworkApi.getHomeworkList(aclassId.value)
console.log('作业列表响应:', response)

if (response.code === 200) {
homeworks.value = response.data || []
console.log('作业列表数据:', homeworks.value)
} else {
error.value = response.msg || '获取作业列表失败'
console.error('获取作业列表失败:', response)
homeworks.value = []
}
} catch (err) {
error.value = '获取作业列表失败，请稍后再试'
console.error('获取作业列表错误:', err)
homeworks.value = []
} finally {
loading.value = false
}
}