import  http  from './axios'

export interface AiAskRequest {
  question: string;
  userId: string;
  context?: string;
}

export interface AiAskResponse {
  answer: string;
  sources: string[];
}

export interface TeachingContentRequest {
  courseOutline: string
  courseId: number
  courseName: string
  expectedHours: number
}

export interface LessonContent {
  title: string
  content: string
  practiceContent: string
  teachingGuidance: string
  suggestedHours: number
  knowledgeSources: string[]
}

export interface TeachingContentResponse {
  lessons: LessonContent[]
  totalHours: number
  timeDistribution: string
  teachingAdvice: string
}

export interface AssistantRequest {
  question: string
  courseName?: string
  chatHistory?: { role: 'user' | 'assistant'; content: string }[]
}

export interface AssistantResponse {
  answer: string
  references: { source: string; content: string }[]
  knowledgePoints: string[]
}

// AI问答
export function askAI(question: string) {
  return http.post('/api/ai/ask', { question })
}

// 生成教学内容
export function generateTeachingContent(request: TeachingContentRequest) {
  return http.post<TeachingContentResponse>('/api/ai/teaching/generate', request)
}

export function askQuestion(data: AiAskRequest) {
  return http.post<AiAskResponse>('/api/ai/ask', data);
}

// 在线学习助手
export function askAssistant(data: AssistantRequest) {
  return http.post<AssistantResponse>('/api/ai/rag/assistant', data)
}
