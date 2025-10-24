async function defaultRequest(config) {
  const mod = await import('../utils/request.js');
  return mod.default(config);
}

export function createInstanceApi(req = defaultRequest) {
  const base = '/flow-eda-server/api/instances';

  async function startInstance(flowId, triggerUser = '') {
    return await req({
      url: base,
      method: 'post',
      data: { flowId, triggerUser },
    });
  }

  async function getInstance(instanceId) {
    return await req({
      url: `${base}/${encodeURIComponent(instanceId)}`,
      method: 'get',
    });
  }

  async function pollInstanceStatus(instanceId, { intervalMs = 1000, maxAttempts = 10 } = {}) {
    let attempt = 0;
    // 简单轮询直到结束态
    while (attempt < maxAttempts) {
      // eslint-disable-next-line no-await-in-loop
      const res = await getInstance(instanceId);
      if (res && res.status && (res.status === 'FINISHED' || res.status === 'FAILED')) {
        return res;
      }
      attempt += 1;
      // eslint-disable-next-line no-await-in-loop
      await new Promise((r) => setTimeout(r, intervalMs));
    }
    throw new Error('poll timeout');
  }

  async function getInstanceNodes(instanceId) {
    return await req({
      url: `${base}/${encodeURIComponent(instanceId)}/nodes`,
      method: 'get',
    });
  }

  async function stopInstance(instanceId) {
    return await req({
      url: `${base}/${encodeURIComponent(instanceId)}/stop`,
      method: 'post',
    });
  }

  async function getInstanceLogs(instanceId) {
    return await req({
      url: `${base}/${encodeURIComponent(instanceId)}/logs`,
      method: 'get',
    });
  }

  return { startInstance, getInstance, pollInstanceStatus, getInstanceNodes, getInstanceLogs, stopInstance };
}

// 可选：默认导出真实请求实现（保留命名导出，避免在 Node 测试中强制实例化）
export const defaultApi = createInstanceApi();


