import { defineConfig } from 'vite'
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  server: {
    port: 3000,
    proxy: {
      // Web服务 API 请求通过网关
      "^/flow-eda-web/api/v1": {
        target: `http://192.168.0.53:8090`,
        ws: true,
        changeOrigin: true,
      },
      // 已移除 WS 代理
    },
  },
  plugins: [vue()],
});
