import api from './index'
import { AxiosResponse } from 'axios'

export interface CompletionRequest {
  prompt: string
  maxTokens?: number
  temperature?: number
}

export interface CompletionResponse {
  success: boolean
  message: string
  result?: string
  timestamp: string
}

export const openaiApi = {
  /**
   * OpenAI Completion 생성
   */
  generateCompletion: async (data: CompletionRequest): Promise<AxiosResponse<CompletionResponse>> => {
    return api.post('/openai/completion', data)
  }
}

