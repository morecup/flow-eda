import { createInstanceApi } from '../src/api/instance.js';

const isCreateRequest = (url) => url.endsWith('/api/flow/instances') || url.endsWith('/api/instances');
const isDetailRequest = (url) => url.includes('/api/flow/instances/') || url.includes('/api/instances/');

async function fakeReqSuccess(config) {
  if (config.method === 'post' && isCreateRequest(config.url)) {
    return { instanceId: 'inst-x' };
  }
  if (config.method === 'get' && isDetailRequest(config.url)) {
    return { instanceId: 'inst-x', status: 'FINISHED' };
  }
  throw new Error('unexpected request');
}

async function fakeReqPoll(config) {
  // first GET -> RUNNING, second -> FINISHED
  fakeReqPoll.count = (fakeReqPoll.count || 0) + 1;
  if (config.method === 'post' && isCreateRequest(config.url)) {
    return { instanceId: 'inst-y' };
  }
  if (config.method === 'get') {
    return fakeReqPoll.count < 3
      ? { instanceId: 'inst-y', status: 'RUNNING' }
      : { instanceId: 'inst-y', status: 'FINISHED' };
  }
  throw new Error('unexpected request');
}

async function fakeReqTimeout() {
  return { instanceId: 'inst-z', status: 'RUNNING' };
}

(async () => {
  // start + get
  const api = createInstanceApi(fakeReqSuccess);
  const startRes = await api.startInstance('flow-1', 'tester');
  if (!startRes.instanceId) throw new Error('start failed');
  const getRes = await api.getInstance(startRes.instanceId);
  if (!getRes.status) throw new Error('get failed');

  // poll success
  const api2 = createInstanceApi(fakeReqPoll);
  const start2 = await api2.startInstance('flow-2');
  const polled = await api2.pollInstanceStatus(start2.instanceId, { intervalMs: 1, maxAttempts: 5 });
  if (polled.status !== 'FINISHED') throw new Error('poll not finished');

  // poll timeout
  const api3 = createInstanceApi(fakeReqTimeout);
  let threw = false;
  try {
    await api3.pollInstanceStatus('inst-z', { intervalMs: 1, maxAttempts: 2 });
  } catch (e) {
    threw = true;
  }
  if (!threw) throw new Error('expected timeout');

  console.log('instance.api.test OK');
})();


