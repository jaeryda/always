const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8088,
    client: {
      overlay: {
        errors: true,
        warnings: false,
        runtimeErrors: (error) => {
          // ResizeObserver 관련 에러 무시
          const errorMessage = error?.message || error?.toString() || ''
          if (
            errorMessage.includes('ResizeObserver loop') ||
            errorMessage.includes('ResizeObserver loop completed')
          ) {
            return false
          }
          return true
        }
      }
    }
  }
})
