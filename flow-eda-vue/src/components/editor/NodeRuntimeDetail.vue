<template>
  <el-dialog
    v-model="dialogVisible"
    :title="`节点运行详情 - ${node?.nodeName || ''}`"
    width="80%"
    @close="handleClose"
  >
    <div class="runtime-detail">
      <!-- 节点基本信息 -->
      <el-descriptions :column="2" border>
        <el-descriptions-item label="节点名称">{{ node?.nodeName }}</el-descriptions-item>
        <el-descriptions-item label="节点类型">{{ node?.nodeType?.typeName }}</el-descriptions-item>
        <el-descriptions-item label="运行状态">
          <el-tag v-if="node?.status === 'RUNNING'" type="warning">运行中</el-tag>
          <el-tag v-else-if="node?.status === 'FINISHED'" type="success">已完成</el-tag>
          <el-tag v-else-if="node?.status === 'FAILED'" type="danger">失败</el-tag>
          <el-tag v-else type="info">未运行</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="runtimeInfo?.jobId" label="Job ID">{{ runtimeInfo.jobId }}</el-descriptions-item>
        <el-descriptions-item label="执行时间">{{ formatDuration(runtimeInfo?.durationMs) }}</el-descriptions-item>
      </el-descriptions>

      <!-- 标签页 -->
      <el-tabs v-model="activeTab" class="detail-tabs">
        <!-- 入参 -->
        <el-tab-pane label="入参 (Input)" name="input">
          <div v-if="runtimeInfo?.inputJson" class="json-content">
            <json-viewer
              :value="parseJson(runtimeInfo.inputJson)"
              :expand-depth="3"
              copyable
              sort
            />
          </div>
          <el-empty v-else description="暂无入参数据" />
        </el-tab-pane>

        <!-- 出参 -->
        <el-tab-pane label="出参 (Output)" name="output">
          <div v-if="runtimeInfo?.outputJson" class="json-content">
            <json-viewer
              :value="parseJson(runtimeInfo.outputJson)"
              :expand-depth="3"
              copyable
              sort
            />
          </div>
          <el-empty v-else description="暂无出参数据" />
        </el-tab-pane>

        <!-- 错误信息 -->
        <el-tab-pane label="错误信息 (Error)" name="error">
          <div v-if="runtimeInfo?.errorStack" class="error-content">
            <el-alert
              :title="'节点执行失败'"
              type="error"
              :closable="false"
              show-icon
            >
              <pre class="error-stack">{{ runtimeInfo.errorStack }}</pre>
            </el-alert>
          </div>
          <el-empty v-else description="无错误信息" />
        </el-tab-pane>

        <!-- 日志 -->
        <el-tab-pane label="日志 (Logs)" name="logs">
          <div v-if="nodeLogs && nodeLogs.length > 0" class="logs-content">
            <div
              v-for="(log, index) in nodeLogs"
              :key="index"
              :class="['log-item', `log-${log.level?.toLowerCase()}`]"
            >
              <span class="log-time">{{ formatTime(log.logTime) }}</span>
              <span class="log-level">{{ log.level }}</span>
              <span class="log-message">{{ log.message }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无日志数据" />
        </el-tab-pane>
      </el-tabs>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button type="primary" @click="handleRefresh">刷新</el-button>
    </template>
  </el-dialog>
</template>

<script>
import { ref, computed, watch } from 'vue';
import JsonViewer from 'vue-json-viewer';
import { defaultApi as instanceApi } from '../../api/instance.js';

export default {
  name: 'NodeRuntimeDetail',
  components: {
    JsonViewer
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    node: {
      type: Object,
      default: null
    },
    instanceId: {
      type: String,
      default: ''
    }
  },
  emits: ['update:visible'],
  setup(props, { emit }) {
    const dialogVisible = ref(false);
    const activeTab = ref('input');
    const runtimeInfo = ref(null);
    const nodeLogs = ref([]);

    // 同步 visible 属性
    watch(() => props.visible, (val) => {
      dialogVisible.value = val;
      if (val) {
        loadRuntimeInfo();
      }
    });

    watch(dialogVisible, (val) => {
      emit('update:visible', val);
    });

    // 加载节点运行时信息
    const loadRuntimeInfo = async () => {
      if (!props.instanceId || !props.node) return;

      try {
        // 获取节点状态信息
        const nodes = await instanceApi.getInstanceNodes(props.instanceId);
        const nodeInfo = nodes.find(n => n.nodeId === props.node.id);
        if (nodeInfo) {
          runtimeInfo.value = nodeInfo;
        }

        // 获取节点日志
        const logs = await instanceApi.getInstanceLogs(props.instanceId);
        nodeLogs.value = logs.filter(log => log.nodeId === props.node.id);
      } catch (e) {
        console.error('加载节点运行时信息失败:', e);
      }
    };

    // 解析 JSON 字符串
    const parseJson = (jsonStr) => {
      if (!jsonStr) return {};
      try {
        return JSON.parse(jsonStr);
      } catch (e) {
        return { error: '无效的 JSON 格式', raw: jsonStr };
      }
    };

    // 格式化时长
    const formatDuration = (ms) => {
      if (!ms) return '-';
      if (ms < 1000) return `${ms}ms`;
      return `${(ms / 1000).toFixed(2)}s`;
    };

    // 格式化时间
    const formatTime = (timeStr) => {
      if (!timeStr) return '-';
      const date = new Date(timeStr);
      return date.toLocaleString('zh-CN', {
        hour12: false,
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    };

    // 关闭对话框
    const handleClose = () => {
      dialogVisible.value = false;
    };

    // 刷新数据
    const handleRefresh = () => {
      loadRuntimeInfo();
    };

    return {
      dialogVisible,
      activeTab,
      runtimeInfo,
      nodeLogs,
      parseJson,
      formatDuration,
      formatTime,
      handleClose,
      handleRefresh
    };
  }
};
</script>

<style lang="less" scoped>
.runtime-detail {
  .detail-tabs {
    margin-top: 20px;

    .json-content {
      max-height: 500px;
      overflow-y: auto;
      padding: 10px;
      background: #f5f5f5;
      border-radius: 4px;
    }

    .error-content {
      .error-stack {
        max-height: 400px;
        overflow-y: auto;
        margin: 10px 0 0 0;
        padding: 10px;
        background: #2b2b2b;
        color: #ff6b6b;
        border-radius: 4px;
        font-family: 'Courier New', monospace;
        font-size: 12px;
        line-height: 1.5;
        white-space: pre-wrap;
        word-break: break-all;
      }
    }

    .logs-content {
      max-height: 500px;
      overflow-y: auto;
      padding: 10px;
      background: #1e1e1e;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
      font-size: 13px;

      .log-item {
        padding: 4px 8px;
        margin-bottom: 2px;
        border-radius: 2px;

        .log-time {
          color: #888;
          margin-right: 10px;
        }

        .log-level {
          display: inline-block;
          width: 60px;
          margin-right: 10px;
          font-weight: bold;
        }

        .log-message {
          color: #d4d4d4;
        }

        &.log-info {
          .log-level {
            color: #4fc3f7;
          }
        }

        &.log-warn {
          background: rgba(255, 193, 7, 0.1);
          .log-level {
            color: #ffc107;
          }
        }

        &.log-error {
          background: rgba(244, 67, 54, 0.1);
          .log-level {
            color: #f44336;
          }
          .log-message {
            color: #ff6b6b;
          }
        }
      }
    }
  }
}
</style>
