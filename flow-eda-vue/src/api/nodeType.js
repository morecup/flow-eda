import request from "../utils/request";

// 查询节点类型
export function getNodeTypes() {
  return request({
        url: "/flow-eda-web/api/v1/node/type",
    method: "get",
  });
}
