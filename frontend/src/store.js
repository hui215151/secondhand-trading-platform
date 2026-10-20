import { reactive, computed, ref } from 'vue'

function readUser() {
  try {
    return JSON.parse(localStorage.getItem('sh_user') || 'null')
  } catch (e) {
    return null
  }
}

export const auth = reactive({
  token: localStorage.getItem('sh_token') || '',
  user: readUser()
})

// 登录态变化时自增，页面 watch 它来重新拉数据
export const authVersion = ref(0)

export const isLoggedIn = computed(() => !!auth.token)
export const isAdmin = computed(() => Number(auth.user && auth.user.role) === 1)

export function setAuth(token) {
  auth.token = token
  localStorage.setItem('sh_token', token)
  authVersion.value++
}

export function setUser(user) {
  auth.user = user
  if (user) {
    localStorage.setItem('sh_user', JSON.stringify(user))
  } else {
    localStorage.removeItem('sh_user')
  }
  authVersion.value++
}

export function clearAuth() {
  auth.token = ''
  auth.user = null
  localStorage.removeItem('sh_token')
  localStorage.removeItem('sh_user')
  authVersion.value++
}

// ---------- 全局 UI 状态 ----------
export const ui = reactive({
  authOpen: false,
  authMode: 'login',
  buy: null,
  toast: { show: false, msg: '', type: '' }
})

let toastTimer = null
export function toast(msg, type = '') {
  ui.toast = { show: true, msg: msg, type: type }
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    ui.toast.show = false
  }, 2600)
}

export function openAuth(mode = 'login') {
  ui.authMode = mode
  ui.authOpen = true
}

export function closeAuth() {
  ui.authOpen = false
}

export function openBuy(goods) {
  ui.buy = goods
}

export function closeBuy() {
  ui.buy = null
}
