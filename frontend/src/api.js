import { auth, clearAuth } from './store'

let unauthorizedHandler = () => {}

export function onUnauthorized(fn) {
  unauthorizedHandler = fn
}

/**
 * 统一请求封装：
 * - 自动带 Bearer token
 * - 401 自动清登录态并弹登录框
 * - 后端统一返回 { code, message, data }，code !== 200 视为失败
 */
export async function api(path, options = {}) {
  const headers = {}
  if (auth.token) headers.Authorization = 'Bearer ' + auth.token

  let payload
  if (options.form) {
    headers['Content-Type'] = 'application/x-www-form-urlencoded'
    payload = new URLSearchParams(options.form).toString()
  } else if (options.body instanceof FormData) {
    payload = options.body
  } else if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json'
    payload = JSON.stringify(options.body)
  }

  const res = await fetch('/api' + path, {
    method: options.method || 'GET',
    headers: headers,
    body: payload
  })

  let json = null
  try {
    json = await res.json()
  } catch (e) {
    /* 忽略非 JSON 响应 */
  }

  if (res.status === 401) {
    clearAuth()
    unauthorizedHandler()
    throw new Error((json && json.message) || '请先登录')
  }
  if (!res.ok) {
    throw new Error((json && json.message) || '请求失败 HTTP ' + res.status)
  }
  if (!json) {
    throw new Error('服务器返回格式异常')
  }
  if (json.code !== 200) {
    throw new Error(json.message || '操作失败')
  }
  return json.data
}
