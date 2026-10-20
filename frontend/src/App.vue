<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { auth, isAdmin, isLoggedIn, openAuth, clearAuth, setUser, toast, ui } from '@/store'
import { api, onUnauthorized } from '@/api'
import AuthModal from '@/components/AuthModal.vue'
import BuyModal from '@/components/BuyModal.vue'

const route = useRoute()
const router = useRouter()
const keyword = ref('')

onUnauthorized(() => openAuth('login'))

async function loadMe() {
  if (!auth.token) return
  try {
    setUser(await api('/user/info'))
  } catch (e) {
    clearAuth()
  }
}

const avatarText = computed(() => {
  const name = (auth.user && (auth.user.nickname || auth.user.username)) || 'U'
  return name.slice(0, 1).toUpperCase()
})

function submitSearch() {
  const q = keyword.value.trim()
  router.push({ path: '/', query: q ? { keyword: q } : {} })
}

function logout() {
  clearAuth()
  toast('已退出登录')
  router.push('/')
}

onMounted(loadMe)
watch(() => auth.token, (t) => {
  if (t) loadMe()
})
watch(() => route.query.keyword, (k) => {
  keyword.value = k || ''
})
</script>

<template>
  <header class="topbar">
    <router-link class="brand" to="/">🎒 校园二手</router-link>

    <form class="search" @submit.prevent="submitSearch">
      <input v-model="keyword" type="search" placeholder="搜索你想要的二手好物…" autocomplete="off" />
      <button type="submit">搜索</button>
    </form>

    <nav class="nav">
      <router-link to="/" :class="{ active: route.path === '/' }">首页</router-link>
      <router-link to="/publish" :class="{ active: route.path === '/publish' }">发布闲置</router-link>
      <router-link to="/me" :class="{ active: route.path === '/me' }">我的</router-link>
      <router-link v-if="isAdmin" to="/admin" :class="{ active: route.path === '/admin' }">管理后台</router-link>

      <button v-if="!isLoggedIn" class="btn btn-sm" @click="openAuth('login')">登录 / 注册</button>
      <div v-else class="user-chip">
        <span class="avatar">
          <img v-if="auth.user && auth.user.avatar" :src="auth.user.avatar" alt="" />
          <template v-else>{{ avatarText }}</template>
        </span>
        <span>{{ auth.user && (auth.user.nickname || auth.user.username) }}</span>
        <button class="link-btn" @click="logout">退出</button>
      </div>
    </nav>
  </header>

  <main class="view">
    <router-view v-slot="{ Component }">
      <component :is="Component" />
    </router-view>
  </main>

  <AuthModal />
  <BuyModal />

  <div class="toast" :class="[ui.toast.show ? 'show' : '', ui.toast.type]">{{ ui.toast.msg }}</div>
</template>
