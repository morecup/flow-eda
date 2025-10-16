import axios from "axios";
import { ElMessage } from "element-plus";
// 无鉴权环境，无需刷新 token

const service = axios.create({
  timeout: 8000,
  headers: {
    "Content-Type": "application/json;charset=utf-8",
    "Access-Control-Allow-Origin": "*",
  },
  withCredentials: true,
});

service.interceptors.request.use(
  (config) => {
    return config;
  },
  (error) => {
    console.log(error);
    return Promise.reject();
  }
);

service.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (res.message) {
      ElMessage.error(res.message);
    } else {
      return res;
    }
  },
  async (error) => {
    if (error.response) {
      // 请求出错，弹出错误信息
      const res = error.response.data;
      if (res && (res.message || res.error)) {
        ElMessage.error(res.message || res.error);
      }
    } else {
      ElMessage.error("request timeout");
    }
  }
);

export default service;
