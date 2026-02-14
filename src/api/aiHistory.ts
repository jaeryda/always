import api from './index'

export interface AIHistoryItemDTO {
  id: number
  userId: number
  type: 'chat' | 'image' | 'video'
  prompt: string
  resultText?: string
  resultUrl?: string
  createdAt: string
}

export const aiHistoryApi = {
  list() {
    return api.get<{ success: boolean; items: AIHistoryItemDTO[] }>('/ai-history')
  },
  create(data: { type: 'chat' | 'image' | 'video'; prompt: string; resultText?: string; resultUrl?: string }) {
    return api.post<{ success: boolean; item: AIHistoryItemDTO }>('/ai-history', data)
  },
  delete(id: number) {
    return api.delete<{ success: boolean }>(`/ai-history/${id}`)
  },
  deleteAll() {
    return api.delete<{ success: boolean }>('/ai-history')
  }
}

