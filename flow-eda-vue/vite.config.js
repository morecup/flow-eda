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
      // 已移除 WS 代理
    },
  },
  plugins: [vue()],
});
