import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: 'jsdom',
    deps: {
      inline: ['element-plus', '@element-plus/icons-vue']
    },
    server: {
      deps: {
        inline: ['element-plus']
      }
    }
  }
})
