import request from "../utils/request";

// 获取节点数据
export function getNodeData(params) {
  return request({
    url: "/flow-eda-server/api/v1/node/data",
    method: "get",
    params: params,
  });
}

// 更新节点数据
export function setNodeData(body) {
  return request({
    url: "/flow-eda-server/api/v1/node/data",
    method: "post",
    data: body,
  });
}

// 获取流程数据版本
export function getVersion(params) {
  return request({
    url: "/flow-eda-server/api/v1/node/data/version",
    method: "get",
    params: params,
  });
}

// 保存流程数据版本
export function saveVersion(version, body) {
  return request({
    url: "/flow-eda-server/api/v1/node/data/version?version=" + version,
    method: "post",
    data: body,
  });
}

