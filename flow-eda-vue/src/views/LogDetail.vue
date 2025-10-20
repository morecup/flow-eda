<template>
  <div id="log-detail" class="log-detail">
    <Codemirror
      v-model:value="logContent"
      :options="{
        mode: 'javascript',
        styleActiveLine: true,
        theme: 'dracula',
        readOnly: true,
      }"
      style="font-size: 14px"
    />
  </div>
</template>

<script>
import Codemirror from "codemirror-editor-vue3";
import "codemirror/mode/javascript/javascript.js";
import "codemirror/theme/dracula.css";
import { onBeforeUnmount, ref, watch } from "vue";
// WS 已移除，使用轮询替代

export default {
  name: "LogDetail",
  props: {
    path: String,
  },
  components: {
    Codemirror,
  },
  setup(props) {
    const logContent = ref("");

    // 监听参数变化，加载新数据，关闭旧连接
    watch(
      () => props.path,
      (n, o) => {
        if (n) {
          if (n !== o) {
            onCloseLogDetail(o);
            getData(n);
          }
        }
      }
    );

    // 获取日志内容
    let timer;
    const getData = (path) => {
      clearInterval(timer);
      timer = setInterval(async () => {
        const res = await fetch(`/api/v1/logs/content?path=${encodeURIComponent(path)}`);
        if (res.ok) {
          const text = await res.text();
          try { const json = JSON.parse(text); logContent.value = json.result || ""; }
          catch { logContent.value = text; }
        }
      }, 2000);
    };

    // 初始加载
    getData(props.path);

    // 组件被销毁之前，关闭socket连接
    onBeforeUnmount(() => {
      clearInterval(timer);
    });

    return {
      logContent,
    };
  },
};
</script>

<style>
.log-detail {
  width: 100%;
  height: 100%;
}
</style>
