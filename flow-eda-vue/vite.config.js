import { defineConfig } from 'vite'
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  server: {
    port: 3000,
    proxy: {
      // 所有 API 请求都通过网关
      "^/api/v1": {
        target: `http://localhost:8090`,
        ws: true,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/v1/, '/web'),
      },
      "^/oauth": {
        target: `http://localhost:8090`,
        changeOrigin: true,
      },
    },
  },
  plugins: [vue()],
});
