/** 创建WebSocket连接（无用户鉴权） */
export function newWebSocket(url, callback) {
  const socket = new WebSocket(window.$wsIp + url);
  socket.onmessage = function (msg) { callback(msg.data); };
  return socket;
}
