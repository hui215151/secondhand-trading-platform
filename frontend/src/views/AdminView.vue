<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/api'
import { auth, isAdmin, isLoggedIn, openAuth, toast, authVersion } from '@/store'
import { money, fmtTime, catEmoji } from '@/constants'

const route = useRoute()
const router = useRouter()

const tab = ref(route.query.tab || 'pending')
const pending = ref([])
const users = ref([])
const loading = ref(false)

async function load() {
  if (!isLoggedIn.value || !isAdmin.value) return
  loading.value = true
  try {
    if (tab.value === 'pending') {
      pending.value = await api('/admin/goods/pending')
    } else {
      users.value = await api('/admin/users')
    }
  } catch (e) {
    toast(e.message, 'err')
  } finally {
    loading.value = false
  }
}

function setTab(t) {
  if (tab.value === t) return
  tab.value = t
  router.replace({ path: '/admin', query: { tab: t } })
  load()
}

async function audit(goods) {
  try {
    await api('/admin/goods/audit/' + goods.id, { method: 'POST' })
    toast('已审核通过', 'ok')
    load()
  } catch (e) {
    toast(e.message, 'err')
  }
}

onMounted(load)
watch(authVersion, load)
watch(
  () => route.query.tab,
  (t) => {
    if (t && t !== tab.value) {
      tab.value = t
      load()
    }
  }
)
</script>

<template>
  <div v-if="!isLoggedIn" class="empty">
    <span class="emoji">🔐</span>请先登录<br /><br />
    <button class="btn" @click="openAuth('login')">立即登录</button>
  </div>

  <div v-else-if="!isAdmin" class="empty">
    <span class="emoji">⛔</span>需要管理员权限（演示账号：admin / admin123）
  </div>

  <template v-else>
    <div class="section-title">管理后台</div>

    <div class="tabs">
      <button class="tab" :class="{ active: tab === 'pending' }" @click="setTab('pending')">商品审核</button>
      <button class="tab" :class="{ active: tab === 'users' }" @click="setTab('users')">用户管理</button>
    </div>

    <div v-if="loading" class="empty"><span class="emoji">⏳</span>加载中…</div>

    <template v-else>
      <template v-if="tab === 'pending'">
        <div v-if="!pending.length" class="empty"><span class="emoji">✅</span>没有待审核的商品</div>
        <div v-for="g in pending" :key="g.id" class="order-item">
          <div class="order-icon">{{ catEmoji(g.categoryId) }}</div>
          <div>
            <div class="order-title">{{ g.title }}</div>
            <div class="order-sub">{{ money(g.price) }} · 发布人 ID {{ g.userId }} · {{ fmtTime(g.createTime) }}</div>
            <div class="order-sub">{{ (g.description || '').slice(0, 60) }}</div>
          </div>
          <div class="order-right">
            <span class="badge badge-warn">待审核</span>
            <button class="btn btn-sm" @click="audit(g)">审核通过</button>
          </div>
        </div>
      </template>

      <table v-else class="table">
        <thead>
          <tr><th>ID</th><th>用户名</th><th>昵称</th><th>角色</th><th>状态</th><th>注册时间</th></tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id">
            <td>{{ u.id }}</td>
            <td>{{ u.username }}</td>
            <td>{{ u.nickname || '—' }}</td>
            <td>
              <span v-if="Number(u.role) === 1" class="badge badge-info">管理员</span>
              <span v-else>普通用户</span>
            </td>
            <td>
              <span class="badge" :class="Number(u.status) === 1 ? 'badge-ok' : 'badge-danger'">
                {{ Number(u.status) === 1 ? '正常' : '禁用' }}
              </span>
            </td>
            <td>{{ fmtTime(u.createTime) }}</td>
          </tr>
        </tbody>
      </table>
    </template>
  </template>
</template>
