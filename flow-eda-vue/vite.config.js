import { defineConfig } from 'vite'
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  server: {
    host: '0.0.0.0',
    port: 3000,
    cors: true,
    proxy: {
      // 统一的 Server 服务 API 请求通过网关
      "^/flow-eda-server": {
        target: `http://localhost:8090`,
        ws: true,
        changeOrigin: true,
      },
    },
  },
  plugins: [vue()],
});
