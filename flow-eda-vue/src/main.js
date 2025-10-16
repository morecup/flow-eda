import vClickOutside from "click-outside-vue3";
import ElementPlus from "element-plus";
import localeZH from "element-plus/es/locale/lang/zh-cn";
import "element-plus/lib/theme-chalk/index.css";
import { createApp } from "vue";
import App from "./App.vue";
import "./assets/iconfont/iconfont.css";
import router from "./router";
import store from "./store";

const app = createApp(App);
app
  .use(ElementPlus, { locale: localeZH })
  .use(vClickOutside)
  .use(store)
  .use(router)
  .mount("#app");
// 不强制指定 WS 基址，交由前端根据当前站点自动推断（见 utils/websocket.js）
