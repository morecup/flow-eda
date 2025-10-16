import { defineConfig } from 'vite'
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  server: {
    port: 3000,
    proxy: {
      // Web服务 API 请求通过网关
      "^/api/v1": {
        target: `http://localhost:8090`,
        ws: true,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/v1/, '/flow-eda-web/api/v1'),
      },
      // Runner服务 WebSocket 通过网关
      "^/ws/flow": {
        target: `http://localhost:8090`,
        ws: true,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/ws\/flow/, '/flow-eda-runner/ws/flow'),
      },
      // Logger服务 WebSocket 通过网关
      "^/ws/logs": {
        target: `http://localhost:8090`,
        ws: true,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/ws\/logs/, '/flow-eda-logger/ws/logs'),
      },
    },
  },
  plugins: [vue()],
});
