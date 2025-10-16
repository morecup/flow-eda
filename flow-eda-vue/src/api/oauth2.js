// OAuth2 已移除，占位导出防止引用报错
export async function userRegister() { return true; }
export async function userLogin() { return true; }
export async function userLogout() { localStorage.clear(); }
export async function userInfo() { return { result: { username: "guest" } }; }
export async function refreshToken() { return false; }
