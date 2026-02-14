import { defineStore } from 'pinia'
import { ref } from 'vue'
import { aiHistoryApi } from '@/api/aiHistory'

export type AIHistoryType = 'chat' | 'image' | 'video'

export interface AIHistoryItem {
  id: number
  userId: number
  type: AIHistoryType
  prompt: string
  resultText?: string
  resultUrl?: string
  createdAt: string
}

export const useAIHistoryStore = defineStore('aiHistory', () => {
  const items = ref<AIHistoryItem[]>([])

  const load = async () => {
    const response = await aiHistoryApi.list()
    items.value = (response.data.items || []) as AIHistoryItem[]
  }

  const addItem = async (item: Omit<AIHistoryItem, 'id' | 'userId' | 'createdAt'>) => {
    await aiHistoryApi.create(item)
    await load()
  }

  const removeItem = async (id: number) => {
    await aiHistoryApi.delete(id)
    await load()
  }

  const clearMine = async () => {
    await aiHistoryApi.deleteAll()
    await load()
  }

  return {
    items,
    load,
    addItem,
    removeItem,
    clearMine
  }
})

