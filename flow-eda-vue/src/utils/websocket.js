function wsBase() {
  const custom = typeof window !== 'undefined' ? window.$wsIp : '';
  if (custom && /^wss?:\/\//.test(custom)) return custom; // 自定义完整地址优先
  const scheme = location.protocol === 'https:' ? 'wss://' : 'ws://';
  return scheme + location.host; // 使用当前站点主机与端口，便于 dev 走 Vite 代理
}

/** 创建WebSocket连接（无用户鉴权） */
export function newWebSocket(url, callback) {
  const socket = new WebSocket(wsBase() + url);
  socket.onmessage = function (msg) { callback(msg.data); };
  return socket;
}
