// 新增：知识库存储相关接口
import axios from './axios'

export interface BasePathRequest {
  base_path: string
}

const StorageApi = {
  /**
   * 设置本地存储根路径
   */
  setBasePath(basePath: string) {
    const payload: BasePathRequest = { base_path: basePath }
    return axios.post('/api/ai/embedding/base_path', payload)
  },

  /**
   * 检查指定文件（可选课程 ID）是否已在知识库中
   */
  documentExists(filename: string, courseId?: string) {
    const params: Record<string, string> = { filename }
    if (courseId) params.course_id = courseId
    return axios.get<{ exists: boolean }>('/api/ai/storage/document_exists', { params })
  },

  /** 恢复默认知识库路径 */
  resetBasePath() {
    return axios.post('/api/ai/embedding/base_path/reset')
  },

  /** 获取已激活知识库列表 */
  listKnowledgeBases() {
    return axios.get<string[]>('/api/ai/storage/list')
  },

  /** 设置当前激活的知识库列表 */
  setSelectedKBs(paths: string[]) {
    return axios.post('/api/ai/storage/selected', paths)
  }
}

export default StorageApi; 