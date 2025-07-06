<script setup lang="ts">
import { ref, onMounted } from 'vue'
import ManageApi from "@/api/management.ts";

interface Teacher {
  id: number
  userId: string
  username: string
  email: string
  role: string
  createdAt?: string
  updatedAt?: string
}

const teachers = ref<Teacher[]>([])
const loading = ref(false)
const error = ref('')

const fetchTeachers = async () => {
  loading.value = true
  error.value = ''
  try {
      const res = await ManageApi.getTeachersList();
      console.log(res)
      teachers.value = res.data
  } catch (e: any) {
    error.value = e?.message || '请求失败'
  } finally {
    loading.value = false
  }
}

const showDeleteModal = ref(false)
const deleteTargetId = ref<number | null>(null)

const openDeleteModal = (id: number) => {
  deleteTargetId.value = id
  showDeleteModal.value = true
}

const closeDeleteModal = () => {
  showDeleteModal.value = false
  deleteTargetId.value = null
}

const confirmDelete = async () => {
  if (deleteTargetId.value == null) return
  try {
    await ManageApi.deleteUser(deleteTargetId.value)
    await fetchTeachers()
  } catch (e: any) {
    error.value = e?.message || '删除失败'
  } finally {
    closeDeleteModal()
  }
}

onMounted(fetchTeachers)
</script>

<template>
  <div class="teachers-list-container">
    <h2>教师基本信息列表</h2>
    <div v-if="loading">加载中...</div>
    <div v-else-if="error" style="color:red">{{ error }}</div>
    <div class="table-scroll-wrapper" v-else>
      <table class="teachers-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户ID</th>
            <th>姓名</th>
            <th>邮箱</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in teachers.filter(t => t.id !== -1)" :key="t.id">
            <td>{{ t.id }}</td>
            <td>{{ t.userId }}</td>
            <td>{{ t.username }}</td>
            <td>{{ t.email }}</td>
            <td>{{ t.createdAt }}</td>
            <td class="actions">
              <button
                  class="btn-action danger"
                  @click="openDeleteModal(t.id)"
                  title="删除"
              >
                  删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <!-- 自定义删除确认弹窗 -->
    <div v-if="showDeleteModal" class="modal-mask">
      <div class="modal-dialog">
        <div class="modal-title">确认删除</div>
        <div class="modal-content">确定要删除该教师吗？</div>
        <div class="modal-actions">
          <button class="btn-action danger" @click="confirmDelete">删除</button>
            <button class="btn-action" @click="closeDeleteModal">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.actions {
    display: flex;
    gap: 0.5rem;
}

.btn-action {
    padding: 0.375rem 0.75rem;
    border-radius: 4px;
    border: none;
    font-size: 0.875rem;
    cursor: pointer;
    transition: background-color 0.2s;
}

.btn-action.danger {
    background-color: #ffebee;
    color: #d32f2f;
}

.btn-action.danger:hover {
    background-color: #ffcdd2;
}

.teachers-list-container {
  max-width: 1000px;
  margin: 40px auto 0 auto;
  background: #f8fafc;
  padding: 24px 32px 32px 32px;
  border-radius: 16px;
  box-shadow: 0 4px 24px 0 rgba(60, 72, 88, 0.10);
}

.table-scroll-wrapper {
  max-height: 600px;
  overflow-y: auto;
  border-radius: 12px;
  /* 保证阴影和圆角效果 */
  box-shadow: 0 2px 8px 0 rgba(60, 72, 88, 0.07);
  background: #fff;
}

h2 {
  margin-bottom: 20px;
  text-align: center;
  color: #3a466e;
  font-weight: 600;
}

.teachers-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  background: #fff;
  border-radius: 12px;
  /* 移除box-shadow，已在外层加 */
}

.teachers-table th, .teachers-table td {
  padding: 12px 16px;
  text-align: left;
}

.teachers-table th {
  background: #e6ecfa;
  color: #47507a;
  font-weight: 500;
  border-bottom: 2px solid #d3dff7;
}

.teachers-table tbody tr {
  transition: background 0.2s;
}

.teachers-table tbody tr:nth-child(even) {
  background: #f4f7fb;
}

.teachers-table tbody tr:hover {
  background: #e0eaff;
}

.teachers-table td {
  color: #3a466e;
  border-bottom: 1px solid #f0f2f7;
}

.teachers-table tbody tr:last-child td {
  border-bottom: none;
}

.modal-mask {
  position: fixed;
  z-index: 9999;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(60, 72, 88, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-dialog {
  background: #fff;
  border-radius: 10px;
  min-width: 320px;
  max-width: 90vw;
  box-shadow: 0 8px 32px 0 rgba(60, 72, 88, 0.18);
  padding: 28px 24px 20px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.modal-title {
  font-size: 1.15rem;
  font-weight: 600;
  color: #3a466e;
  margin-bottom: 12px;
}

.modal-content {
  color: #47507a;
  margin-bottom: 22px;
}

.modal-actions {
  display: flex;
  gap: 16px;
  width: 100%;
  justify-content: flex-end;
}
</style>
