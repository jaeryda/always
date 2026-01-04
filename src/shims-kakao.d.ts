// Kakao SDK 타입 정의
declare global {
  interface Window {
    Kakao: {
      init: (key: string) => void
      isInitialized: () => boolean
      Auth: {
        authorize: (options: { redirectUri: string }) => void
        setAccessToken: (token: string) => void
        getAccessToken: () => string | null
        getStatusInfo: () => Promise<{
          status: 'connected' | 'not_connected'
          user: any
        }>
        logout: () => Promise<void>
      }
      API: {
        request: (options: {
          url: string
          success: (response: any) => void
          fail: (error: any) => void
        }) => void
      }
    }
  }
}

export {}

