import api from './index'
import { AxiosResponse } from 'axios'
import { GoogleGenAI } from '@google/genai'

export interface CompletionRequest {
  prompt: string
  maxTokens?: number
  temperature?: number
  messages?: Array<{ role: 'user' | 'assistant'; content: string }> // 대화 히스토리
}

export interface CompletionResponse {
  success: boolean
  message: string
  result?: string
  timestamp: string
}

export interface GeminiChatRequest {
  prompt: string
  model?: 'gemini-3-pro-preview' | 'gemini-3-flash-preview' | 'gemini-2.5-flash'
  messages?: Array<{ role: 'user' | 'assistant'; content: string }> // 대화 히스토리
  temperature?: number
  maxTokens?: number
}

export interface GeminiChatResponse {
  success: boolean
  message?: string
  result?: string
}

export interface GeminiImageRequest {
  prompt: string
  model?: 'gemini-2.5-flash-image' | 'gemini-3-pro-image-preview'
  aspectRatio?: '1:1' | '2:3' | '3:2' | '3:4' | '4:3' | '4:5' | '5:4' | '9:16' | '16:9' | '21:9'
  imageSize?: '1K' | '2K' | '4K' // gemini-3-pro-image-preview만 사용 가능
  referenceImage?: string // base64 인코딩된 참고 이미지 (선택사항)
}

export interface GeminiImageResponse {
  success: boolean
  message?: string
  imageData?: string // base64 인코딩된 이미지 데이터
  imageUrl?: string // data URL (base64)
  text?: string // 응답에 포함된 텍스트가 있는 경우
}

export interface GeminiVideoRequest {
  prompt: string
  model?: 'veo-3.1-generate-preview' | 'veo-3.1-fast-generate-preview' | 'veo-3.0-generate-001' | 'veo-3.0-fast-generate-001'
  referenceImages?: string[] // base64 인코딩된 참고 이미지들 (최대 3개)
  aspectRatio?: '16:9' | '9:16' | '1:1'
  resolution?: '720p' | '1080p'
}

export interface GeminiVideoResponse {
  success: boolean
  message?: string
  videoUrl?: string // 생성된 비디오 URL
  operationId?: string // 작업 ID (폴링용)
  operation?: any // operation 객체 전체 (폴링용)
}

export const openaiApi = {
  /**
   * OpenAI Completion 생성 (레거시 - 백엔드 API 사용)
   */
  generateCompletion: async (data: CompletionRequest): Promise<AxiosResponse<CompletionResponse>> => {
    return api.post('/openai/completion', data)
  },

  /**
   * Gemini AI 채팅 (Gemini 3 사용)
   * @see https://ai.google.dev/gemini-api/docs/gemini-3
   */
  generateChat: async (request: GeminiChatRequest): Promise<GeminiChatResponse> => {
    try {
      // API 키는 환경 변수에서 가져옵니다
      const apiKey = 
        (import.meta.env?.VITE_GEMINI_API_KEY as string) ||
        (process.env?.VUE_APP_GEMINI_API_KEY as string) ||
        (process.env?.VITE_GEMINI_API_KEY as string)
      
      if (!apiKey) {
        throw new Error(
          'Gemini API 키가 설정되지 않았습니다.\n' +
          '프로젝트 루트에 .env 파일을 생성하고 다음을 추가하세요:\n' +
          'VUE_APP_GEMINI_API_KEY=your_api_key_here\n' +
          '또는\n' +
          'VITE_GEMINI_API_KEY=your_api_key_here\n' +
          '\n그리고 개발 서버를 재시작하세요.'
        )
      }

      const ai = new GoogleGenAI({ apiKey })
      const model = request.model || 'gemini-3-flash-preview' // 기본값: 무료 티어가 있는 Flash

      // 대화 히스토리 구성
      // Gemini는 대화 히스토리를 배열로 받습니다
      const contents: any[] = []
      
      if (request.messages && request.messages.length > 0) {
        // 이전 대화 히스토리를 Gemini 형식으로 변환
        for (const msg of request.messages) {
          contents.push({
            role: msg.role === 'user' ? 'user' : 'model',
            parts: [{ text: msg.content }]
          })
        }
      }
      
      // 현재 프롬프트 추가
      contents.push({
        role: 'user',
        parts: [{ text: request.prompt }]
      })

      // generationConfig 설정
      const generationConfig: any = {}
      if (request.temperature !== undefined) {
        generationConfig.temperature = request.temperature
      }
      if (request.maxTokens !== undefined) {
        generationConfig.maxOutputTokens = request.maxTokens
      }

      const response = await ai.models.generateContent({
        model,
        contents: contents.length > 0 ? contents : request.prompt,
        ...(Object.keys(generationConfig).length > 0 ? { generationConfig } : {})
      })

      // 응답 처리
      let result = ''
      if (response.candidates && response.candidates[0]?.content?.parts) {
        for (const part of response.candidates[0].content.parts) {
          if (part.text) {
            result += part.text
          }
        }
      }

      if (!result) {
        return {
          success: false,
          message: '응답을 받지 못했습니다.'
        }
      }

      return {
        success: true,
        result
      }
    } catch (error: any) {
      console.error('Gemini 채팅 오류:', error)
      
      // 429 오류 (할당량 초과) 처리
      if (error?.code === 429 || error?.status === 'RESOURCE_EXHAUSTED') {
        let retryAfter = ''
        
        if (error?.details) {
          const retryInfo = error.details.find((detail: any) => detail['@type'] === 'type.googleapis.com/google.rpc.RetryInfo')
          if (retryInfo?.retryDelay) {
            const seconds = Math.ceil(parseFloat(retryInfo.retryDelay.replace('s', '')))
            const minutes = Math.floor(seconds / 60)
            const remainingSeconds = seconds % 60
            if (minutes > 0) {
              retryAfter = `${minutes}분 ${remainingSeconds}초`
            } else {
              retryAfter = `${seconds}초`
            }
          }
        }
        
        return {
          success: false,
          message: `할당량이 초과되었습니다.\n\n` +
                   `무료 티어의 일일/분당 요청 제한에 도달했습니다.\n\n` +
                   (retryAfter ? `약 ${retryAfter} 후에 다시 시도해주세요.\n\n` : '') +
                   `해결 방법:\n` +
                   `1. 잠시 후 다시 시도하세요\n` +
                   `2. Google AI Studio에서 할당량 확인: https://ai.dev/rate-limit\n` +
                   `3. 유료 플랜으로 업그레이드 고려`
        }
      }
      
      // 일반 오류 처리
      let errorMsg = 'AI 응답 중 오류가 발생했습니다.'
      
      if (error?.error?.message) {
        errorMsg = error.error.message
      } else if (error?.response?.data?.error?.message) {
        errorMsg = error.response.data.error.message
      } else if (error?.message) {
        errorMsg = error.message
      }
      
      return {
        success: false,
        message: errorMsg
      }
    }
  },

  /**
   * Gemini AI 이미지 생성
   * @see https://ai.google.dev/gemini-api/docs/image-generation
   */
  generateImage: async (request: GeminiImageRequest): Promise<GeminiImageResponse> => {
    try {
      // API 키는 환경 변수에서 가져옵니다
      // Vue CLI와 Vite 모두 지원
      const apiKey = 
        (import.meta.env?.VITE_GEMINI_API_KEY as string) ||
        (process.env?.VUE_APP_GEMINI_API_KEY as string) ||
        (process.env?.VITE_GEMINI_API_KEY as string)
      
      console.log('환경 변수 확인:', {
        'import.meta.env.VITE_GEMINI_API_KEY': import.meta.env?.VITE_GEMINI_API_KEY,
        'process.env.VUE_APP_GEMINI_API_KEY': process.env?.VUE_APP_GEMINI_API_KEY,
        'process.env.VITE_GEMINI_API_KEY': process.env?.VITE_GEMINI_API_KEY,
        '최종 apiKey': apiKey ? '설정됨' : '없음'
      })
      
      if (!apiKey) {
        throw new Error(
          'Gemini API 키가 설정되지 않았습니다.\n' +
          '프로젝트 루트에 .env 파일을 생성하고 다음을 추가하세요:\n' +
          'VUE_APP_GEMINI_API_KEY=your_api_key_here\n' +
          '또는\n' +
          'VITE_GEMINI_API_KEY=your_api_key_here\n' +
          '\n그리고 개발 서버를 재시작하세요.'
        )
      }

      const ai = new GoogleGenAI({ apiKey })

      const model = request.model || 'gemini-2.5-flash-image'
      
      // generationConfig 설정
      const generationConfig: any = {}
      if (request.aspectRatio) {
        generationConfig.imageConfig = {
          aspectRatio: request.aspectRatio
        }
        // gemini-3-pro-image-preview의 경우 imageSize도 설정 가능
        if (model === 'gemini-3-pro-image-preview' && request.imageSize) {
          generationConfig.imageConfig.imageSize = request.imageSize
        }
      }

      // contents 구성: 이미지가 있으면 이미지와 텍스트를 함께 전송
      let contents: any = request.prompt
      
      if (request.referenceImage) {
        // 이미지와 텍스트를 함께 전송
        contents = [
          {
            inlineData: {
              data: request.referenceImage,
              mimeType: 'image/png' // base64 이미지의 경우
            }
          },
          { text: request.prompt }
        ]
      }

      const response = await ai.models.generateContent({
        model,
        contents,
        ...(Object.keys(generationConfig).length > 0 ? { generationConfig } : {})
      })

      // 응답 처리
      let imageData: string | undefined
      let text: string | undefined

      if (response.candidates && response.candidates[0]?.content?.parts) {
        for (const part of response.candidates[0].content.parts) {
          if (part.text) {
            text = part.text
          } else if (part.inlineData) {
            imageData = part.inlineData.data
          }
        }
      }

      if (!imageData) {
        return {
          success: false,
          message: '이미지 데이터를 받지 못했습니다.',
          text
        }
      }

      // base64 데이터를 data URL로 변환
      const imageUrl = `data:image/png;base64,${imageData}`

      return {
        success: true,
        imageData,
        imageUrl,
        text
      }
    } catch (error: any) {
      console.error('Gemini 이미지 생성 오류:', error)
      
      // 429 오류 (할당량 초과) 처리
      if (error?.code === 429 || error?.status === 'RESOURCE_EXHAUSTED') {
        let retryAfter = ''
        
        // 재시도 시간 추출
        if (error?.details) {
          const retryInfo = error.details.find((detail: any) => detail['@type'] === 'type.googleapis.com/google.rpc.RetryInfo')
          if (retryInfo?.retryDelay) {
            const seconds = Math.ceil(parseFloat(retryInfo.retryDelay.replace('s', '')))
            const minutes = Math.floor(seconds / 60)
            const remainingSeconds = seconds % 60
            if (minutes > 0) {
              retryAfter = `${minutes}분 ${remainingSeconds}초`
            } else {
              retryAfter = `${seconds}초`
            }
          }
        }
        
        return {
          success: false,
          message: `할당량이 초과되었습니다.\n\n` +
                   `무료 티어의 일일/분당 요청 제한에 도달했습니다.\n\n` +
                   (retryAfter ? `약 ${retryAfter} 후에 다시 시도해주세요.\n\n` : '') +
                   `해결 방법:\n` +
                   `1. 잠시 후 다시 시도하세요\n` +
                   `2. Google AI Studio에서 할당량 확인: https://ai.dev/rate-limit\n` +
                   `3. 유료 플랜으로 업그레이드 고려`
        }
      }
      
      // 일반 오류 처리
      // 에러 객체에서 더 자세한 정보 추출
      let errorMsg = '이미지 생성 중 오류가 발생했습니다.'
      
      if (error?.error?.message) {
        errorMsg = error.error.message
      } else if (error?.response?.data?.error?.message) {
        errorMsg = error.response.data.error.message
      } else if (error?.message) {
        errorMsg = error.message
      }
      
      return {
        success: false,
        message: errorMsg
      }
    }
  },

  /**
   * Veo AI 동영상 생성
   * @see https://ai.google.dev/gemini-api/docs/video
   */
  generateVideo: async (request: GeminiVideoRequest): Promise<GeminiVideoResponse> => {
    try {
      // API 키는 환경 변수에서 가져옵니다
      const apiKey = 
        (import.meta.env?.VITE_GEMINI_API_KEY as string) ||
        (process.env?.VUE_APP_GEMINI_API_KEY as string) ||
        (process.env?.VITE_GEMINI_API_KEY as string)
      
      if (!apiKey) {
        throw new Error(
          'Gemini API 키가 설정되지 않았습니다.\n' +
          '프로젝트 루트에 .env 파일을 생성하고 다음을 추가하세요:\n' +
          'VUE_APP_GEMINI_API_KEY=your_api_key_here\n' +
          '또는\n' +
          'VITE_GEMINI_API_KEY=your_api_key_here\n' +
          '\n그리고 개발 서버를 재시작하세요.'
        )
      }

      const ai = new GoogleGenAI({ apiKey })
      const model = request.model || 'veo-3.1-generate-preview'

      // 참고 이미지 처리
      const referenceImagesData = request.referenceImages && request.referenceImages.length > 0
        ? request.referenceImages.map(img => {
            // base64 데이터에서 data URL 접두사 제거 (있다면)
            const base64Data = img.includes(',') ? img.split(',')[1] : img
            return {
              inlineData: {
                data: base64Data,
                mimeType: 'image/png' // 또는 실제 이미지 타입에 맞게 조정
              }
            }
          })
        : undefined

      console.log('참고 이미지 처리:', {
        count: request.referenceImages?.length || 0,
        hasData: !!referenceImagesData,
        firstImagePreview: referenceImagesData?.[0]?.inlineData?.data?.substring(0, 50) + '...'
      })

      // 비디오 생성 요청
      const generateRequest: any = {
        model,
        prompt: request.prompt
      }

      if (referenceImagesData && referenceImagesData.length > 0) {
        generateRequest.referenceImages = referenceImagesData
      }

      if (request.aspectRatio) {
        generateRequest.aspectRatio = request.aspectRatio
      }

      if (request.resolution) {
        generateRequest.resolution = request.resolution
      }

      console.log('Veo API 요청:', {
        model: generateRequest.model,
        prompt: generateRequest.prompt,
        hasReferenceImages: !!generateRequest.referenceImages,
        referenceImagesCount: generateRequest.referenceImages?.length || 0,
        aspectRatio: generateRequest.aspectRatio,
        resolution: generateRequest.resolution
      })

      const operation = await ai.models.generateVideos(generateRequest)

      // operation 객체에서 name 추출
      const operationId = (operation as any)?.name || (operation as any)?.operationId || ''

      return {
        success: true,
        operationId,
        operation: operation, // operation 객체 전체를 반환
        message: '동영상 생성이 시작되었습니다. 완료될 때까지 기다려주세요.'
      }
    } catch (error: any) {
      console.error('Veo 동영상 생성 오류:', error)
      
      // 429 오류 (할당량 초과) 처리
      if (error?.code === 429 || error?.status === 'RESOURCE_EXHAUSTED') {
        let retryAfter = ''
        
        if (error?.details) {
          const retryInfo = error.details.find((detail: any) => detail['@type'] === 'type.googleapis.com/google.rpc.RetryInfo')
          if (retryInfo?.retryDelay) {
            const seconds = Math.ceil(parseFloat(retryInfo.retryDelay.replace('s', '')))
            const minutes = Math.floor(seconds / 60)
            const remainingSeconds = seconds % 60
            if (minutes > 0) {
              retryAfter = `${minutes}분 ${remainingSeconds}초`
            } else {
              retryAfter = `${seconds}초`
            }
          }
        }
        
        return {
          success: false,
          message: `할당량이 초과되었습니다.\n\n` +
                   `무료 티어의 일일/분당 요청 제한에 도달했습니다.\n\n` +
                   (retryAfter ? `약 ${retryAfter} 후에 다시 시도해주세요.\n\n` : '') +
                   `해결 방법:\n` +
                   `1. 잠시 후 다시 시도하세요\n` +
                   `2. Google AI Studio에서 할당량 확인: https://ai.dev/rate-limit\n` +
                   `3. 유료 플랜으로 업그레이드 고려`
        }
      }
      
      // 일반 오류 처리
      let errorMsg = '동영상 생성 중 오류가 발생했습니다.'
      
      if (error?.error?.message) {
        errorMsg = error.error.message
      } else if (error?.response?.data?.error?.message) {
        errorMsg = error.response.data.error.message
      } else if (error?.message) {
        errorMsg = error.message
      }
      
      return {
        success: false,
        message: errorMsg
      }
    }
  },

  /**
   * Veo 동영상 생성 작업 상태 확인 및 다운로드
   * @see https://ai.google.dev/gemini-api/docs/video
   */
  checkVideoOperation: async (operation: any): Promise<GeminiVideoResponse> => {
    try {
      const apiKey = 
        (import.meta.env?.VITE_GEMINI_API_KEY as string) ||
        (process.env?.VUE_APP_GEMINI_API_KEY as string) ||
        (process.env?.VITE_GEMINI_API_KEY as string)
      
      if (!apiKey) {
        throw new Error('Gemini API 키가 설정되지 않았습니다.')
      }

      const ai = new GoogleGenAI({ apiKey })

      // 작업 상태 확인 - 문서에 따르면 operation 객체를 그대로 전달해야 함
      const updatedOperation = await ai.operations.getVideosOperation({
        operation: operation
      })

      if (!updatedOperation.done) {
        return {
          success: false,
          message: '동영상 생성이 아직 진행 중입니다.',
          operation: updatedOperation // 업데이트된 operation 반환
        }
      }

      // 동영상 다운로드
      if (updatedOperation.response?.generatedVideos && updatedOperation.response.generatedVideos.length > 0) {
        const video = updatedOperation.response.generatedVideos[0].video
        
        if (video) {
          try {
            const videoFile = video as any
            
            // 디버깅: video 객체 구조 확인
            console.log('Video 객체 구조:', videoFile)
            console.log('Video 객체 키:', Object.keys(videoFile))
            
            // 파일 정보에서 URI 추출
            // video 객체는 File 타입이거나 URI를 가질 수 있음
            let videoUri = ''
            
            // 다양한 가능한 속성 확인
            if (videoFile.uri) {
              videoUri = videoFile.uri
            } else if (videoFile.name) {
              videoUri = videoFile.name
            } else if (videoFile.fileUri) {
              videoUri = videoFile.fileUri
            } else if (videoFile.downloadUri) {
              videoUri = videoFile.downloadUri
            } else if (typeof videoFile === 'string') {
              videoUri = videoFile
            }
            
            console.log('추출된 videoUri:', videoUri)
            
            // API 키를 포함한 다운로드 URL 생성 시도
            const apiKey = 
              (import.meta.env?.VITE_GEMINI_API_KEY as string) ||
              (process.env?.VUE_APP_GEMINI_API_KEY as string) ||
              (process.env?.VITE_GEMINI_API_KEY as string)
            
            if (videoUri && apiKey) {
              // Files API를 통해 다운로드 가능한 URL 생성
              // videoUri가 파일 이름인 경우 Files API 엔드포인트 사용
              if (videoUri.startsWith('files/') || (!videoUri.startsWith('http') && !videoUri.startsWith('blob'))) {
                // Files API를 통해 다운로드
                // name이 "files/xxx" 형식이거나 단순 파일명인 경우
                const filePath = videoUri.startsWith('files/') ? videoUri : `files/${videoUri}`
                const downloadUrl = `https://generativelanguage.googleapis.com/v1beta/${filePath}?alt=media&key=${apiKey}`
                console.log('생성된 다운로드 URL:', downloadUrl)
                return {
                  success: true,
                  videoUrl: downloadUrl,
                  message: '동영상이 생성되었습니다!'
                }
              } else {
                // 이미 완전한 URL인 경우
                console.log('완전한 URL 사용:', videoUri)
                return {
                  success: true,
                  videoUrl: videoUri,
                  message: '동영상이 생성되었습니다!'
                }
              }
            }
            
            // 파일 객체 자체를 반환 (클라이언트에서 처리)
            console.warn('videoUri를 찾을 수 없음. videoFile:', videoFile)
            return {
              success: true,
              videoUrl: '', // 파일 정보는 videoFile 객체에 있음
              message: '동영상이 생성되었습니다. 파일 정보를 확인하세요.'
            }
          } catch (error: any) {
            console.error('동영상 처리 오류:', error)
            return {
              success: true,
              message: '동영상이 생성되었습니다. API에서 직접 확인해주세요.'
            }
          }
        }
      }

      return {
        success: false,
        message: '동영상 데이터를 받지 못했습니다.'
      }
    } catch (error: any) {
      console.error('동영상 작업 확인 오류:', error)
      return {
        success: false,
        message: error?.message || '동영상 작업 확인 중 오류가 발생했습니다.'
      }
    }
  }
}

