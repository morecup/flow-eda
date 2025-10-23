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
    instanceId: String,
  },
  components: {
    Codemirror,
  },
  setup(props) {
    const logContent = ref("");

    // 监听参数变化，加载新数据，关闭旧连接
    watch(() => props.instanceId, (n, o) => { if (n && n !== o) { getData(n); } });

    // 获取日志内容
    let timer;
    const getData = (instanceId) => {
      clearInterval(timer);
      timer = setInterval(async () => {
        const res = await fetch(`/flow-eda-runner/api/instances/${encodeURIComponent(instanceId)}/logs`);
        if (res.ok) {
          const text = await res.text();
          try {
            const json = JSON.parse(text);
            if (Array.isArray(json)) {
              logContent.value = json.map(i => i.message || "").join("\n");
            } else if (Array.isArray(json.result)) {
              logContent.value = json.result.map(i => i.message || "").join("\n");
            } else {
              logContent.value = "";
            }
          } catch { logContent.value = text; }
        }
      }, 2000);
    };

    // 初始加载
    getData(props.instanceId);

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
