<script setup>
import { ref, computed, watch } from 'vue'
import { ui, closeAuth, setAuth, setUser, toast } from '@/store'
import { api } from '@/api'

const username = ref('')
const password = ref('')
const nickname = ref('')
const loading = ref(false)

const isLogin = computed(() => ui.authMode === 'login')

watch(
  () => ui.authOpen,
  (open) => {
    if (!open) return
    username.value = isLogin.value ? 'user1' : ''
    password.value = isLogin.value ? 'user123' : ''
    nickname.value = ''
  }
)

async function submit() {
  if (!username.value.trim() || !password.value) {
    toast('请填写用户名和密码', 'err')
    return
  }
  loading.value = true
  try {
    if (!isLogin.value) {
      await api('/user/register', {
        method: 'POST',
        body: {
          username: username.value.trim(),
          password: password.value,
          nickname: nickname.value.trim()
        }
      })
    }
    const token = await api('/user/login', {
      method: 'POST',
      body: { username: username.value.trim(), password: password.value }
    })
    setAuth(token)
    setUser(await api('/user/info'))
    closeAuth()
    toast(isLogin.value ? '登录成功' : '注册成功，已自动登录', 'ok')
  } catch (e) {
    toast(e.message, 'err')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-if="ui.authOpen" class="overlay" @click.self="closeAuth">
    <div class="modal">
      <h3>{{ isLogin ? '欢迎回来' : '加入校园二手' }}</h3>
      <p class="modal-sub">
        {{ isLogin ? '演示账号：user1 / user123，管理员：admin / admin123' : '注册后即可发布、收藏、下单' }}
      </p>

      <form class="form" @submit.prevent="submit">
        <div class="field">
          <label>用户名</label>
          <input v-model="username" class="input" required minlength="4" maxlength="20" placeholder="4~20 位字母 / 数字 / 下划线" />
        </div>
        <div class="field">
          <label>密码</label>
          <input v-model="password" class="input" type="password" required minlength="6" maxlength="20" placeholder="至少 6 位" />
        </div>
        <div v-if="!isLogin" class="field">
          <label>昵称 <span class="hint">选填</span></label>
          <input v-model="nickname" class="input" maxlength="50" placeholder="别人看到的名字" />
        </div>

        <div class="modal-actions">
          <button type="button" class="btn btn-ghost" @click="closeAuth">取消</button>
          <button class="btn" type="submit" :disabled="loading">{{ isLogin ? '登录' : '注册并登录' }}</button>
        </div>
      </form>

      <div class="auth-switch">
        {{ isLogin ? '还没有账号？' : '已有账号？' }}
        <a @click="ui.authMode = isLogin ? 'register' : 'login'">{{ isLogin ? '立即注册' : '去登录' }}</a>
      </div>
    </div>
  </div>
</template>
