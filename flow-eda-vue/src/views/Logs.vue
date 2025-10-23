<template>
  <div>
    <div class="container">
      <div class="handle-box">
        <el-select v-model="params.type" class="handle-select mr10">
          <el-option key="RUNNING" label="运行日志" value="RUNNING" />
          <el-option
            v-if="username === 'admin'"
            key="OPERATION"
            label="操作日志"
            value="OPERATION"
          />
        </el-select>
        <el-button icon="el-icon-search" type="primary" @click="handleSearch"
          >查询</el-button
        >
        <el-button
          :disabled="!hasSelection"
          icon="el-icon-delete"
          style="float: right"
          type="primary"
          @click="delAllSelection"
          >批量删除
        </el-button>
      </div>
      <el-table
        ref="multipleTable"
        :data="tableData"
        border
        class="table"
        header-cell-class-name="table-header"
        size="small"
        @selection-change="handleSelectionChange"
      >
        <el-table-column align="center" type="selection" width="45" />
        <el-table-column label="实例ID" prop="instanceId" show-overflow-tooltip />
        <el-table-column label="节点ID" prop="nodeId" show-overflow-tooltip />
        <el-table-column label="级别" prop="level" width="120" />
        <el-table-column label="时间" prop="logTime" width="220" />
        <el-table-column align="center" label="操作" width="220">
          <template #default="scope">
            <el-button
              icon="el-icon-search"
              type="text"
              @click="handleShow(scope.row)"
              >查看</el-button
            >
            <el-button
              icon="el-icon-delete"
              style="color: #ff0000"
              type="text"
              @click="handleDelete()"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          :current-page="params.page"
          :total="pageTotal"
          background
          layout="total, prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { reactive, ref } from "vue";
import { listLogs, deleteLogs } from "../api/logs.js";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";

export default {
  name: "Logs",
  setup() {
    const username = null; // 无用户区分

    // 查询参数
    const params = reactive({ page: 1 });
    const tableData = ref([]);
    const pageTotal = ref(0);

    // 查询日志列表
    const getData = async () => {
      // 占位：列表页按实例维度查询，需要传入 instanceId；此页保留为按实例查看入口
      tableData.value = [];
      pageTotal.value = 0;
    };
    getData();

    // 查询操作
    const handleSearch = () => {
      params.page = 1;
      getData();
    };

    // 分页导航
    const handlePageChange = (val) => {
      params.page = val;
      getData();
    };

    const router = useRouter();

    // 查看详情，打开日志详情页
    const handleShow = (row) => {
      router.push({ path: "/logs/detail", query: { instanceId: row.instanceId } });
    };

    // 多选操作
    let multipleSelection = [];
    let hasSelection = ref(false);
    const handleSelectionChange = (val) => {
      multipleSelection = val.map((i) => i.instanceId);
      hasSelection.value = multipleSelection.length > 0;
    };

    // 删除操作
    const handleDelete = () => {};

    // 批量删除操作
    const delAllSelection = () => {};

    return {
      username,
      params,
      tableData,
      pageTotal,
      hasSelection,
      handleSearch,
      handlePageChange,
      handleShow,
      handleSelectionChange,
      handleDelete,
      delAllSelection,
    };
  },
};
</script>

<style scoped>
.handle-box {
  margin-bottom: 20px;
}

.handle-select {
  width: 300px;
}

.table {
  width: 100%;
  font-size: 14px;
}

.mr10 {
  margin-right: 10px;
}
</style>
