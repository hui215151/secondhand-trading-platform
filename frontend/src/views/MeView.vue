<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/api'
import { auth, isLoggedIn, openAuth, toast, authVersion } from '@/store'
import { money, fmtTime, ORDER_STATUS, GOODS_STATUS, catName, catEmoji, firstImage } from '@/constants'
import GoodsCard from '@/components/GoodsCard.vue'

const route = useRoute()
const router = useRouter()

const tab = ref(route.query.tab || 'orders')
const orders = ref([])
const favorites = ref([])
const myGoods = ref([])
const loading = ref(false)

async function load() {
  if (!isLoggedIn.value) return
  loading.value = true
  try {
    if (tab.value === 'orders') {
      const d = await api('/order/list?page=1&size=50&role=all')
      orders.value = d.records || []
    } else if (tab.value === 'favs') {
      const d = await api('/favorite/list?page=1&size=50')
      favorites.value = d.records || []
    } else {
      myGoods.value = await api('/goods/mine')
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
  router.replace({ path: '/me', query: { tab: t } })
  load()
}

async function pay(order) {
  try {
    await api('/order/pay/' + order.orderNo, { method: 'POST' })
    toast('付款成功', 'ok')
    load()
  } catch (e) {
    toast(e.message, 'err')
  }
}

async function cancel(order) {
  if (!confirm('确定取消这笔订单吗？')) return
  try {
    await api('/order/cancel/' + order.orderNo, { method: 'POST' })
    toast('订单已取消', 'ok')
    load()
  } catch (e) {
    toast(e.message, 'err')
  }
}

function isBuyer(order) {
  return !!(auth.user && Number(auth.user.id) === Number(order.buyerId))
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
    <span class="emoji">🔐</span>请先登录查看个人中心<br /><br />
    <button class="btn" @click="openAuth('login')">立即登录</button>
  </div>

  <template v-else>
    <div class="profile-card">
      <span class="avatar">
        <img v-if="auth.user && auth.user.avatar" :src="auth.user.avatar" alt="" />
        <template v-else>{{ ((auth.user && (auth.user.nickname || auth.user.username)) || 'U').slice(0, 1).toUpperCase() }}</template>
      </span>
      <div>
        <h2>{{ auth.user && (auth.user.nickname || auth.user.username) }}</h2>
        <div class="sub">
          @{{ auth.user && auth.user.username }} · {{ auth.user && Number(auth.user.role) === 1 ? '管理员' : '普通用户' }} ·
          注册于 {{ fmtTime(auth.user && auth.user.createTime) }}
        </div>
      </div>
    </div>

    <div class="tabs">
      <button class="tab" :class="{ active: tab === 'orders' }" @click="setTab('orders')">我的订单</button>
      <button class="tab" :class="{ active: tab === 'favs' }" @click="setTab('favs')">我的收藏</button>
      <button class="tab" :class="{ active: tab === 'mine' }" @click="setTab('mine')">我发布的</button>
    </div>

    <div v-if="loading" class="empty"><span class="emoji">⏳</span>加载中…</div>

    <template v-else>
      <!-- 订单 -->
      <template v-if="tab === 'orders'">
        <div v-if="!orders.length" class="empty"><span class="emoji">📦</span>还没有订单，去首页逛逛吧</div>
        <div v-for="o in orders" :key="o.orderNo" class="order-item">
          <div class="order-icon">{{ isBuyer(o) ? '🛒' : '💰' }}</div>
          <div>
            <div class="order-title">{{ o.goodsTitle }}</div>
            <div class="order-sub">
              {{ isBuyer(o) ? '我买到的' : '我卖出的' }} · {{ o.orderNo }} · {{ fmtTime(o.createTime) }}
            </div>
            <div class="order-sub">收货地址：{{ o.address || '—' }}</div>
          </div>
          <div class="order-right">
            <span class="badge" :class="(ORDER_STATUS[o.status] || ['未知', 'badge-muted'])[1]">
              {{ (ORDER_STATUS[o.status] || ['未知'])[0] }}
            </span>
            <span class="price" style="font-size: 16px">{{ money(o.price) }}</span>
            <template v-if="o.status === 0 && isBuyer(o)">
              <button class="btn btn-sm" @click="pay(o)">付款</button>
              <button class="btn btn-ghost btn-sm" @click="cancel(o)">取消</button>
            </template>
          </div>
        </div>
      </template>

      <!-- 收藏 -->
      <template v-else-if="tab === 'favs'">
        <div v-if="!favorites.length" class="empty"><span class="emoji">💛</span>还没有收藏任何商品</div>
        <div v-else class="grid">
          <GoodsCard v-for="g in favorites" :key="g.id" :goods="g" />
        </div>
      </template>

      <!-- 我发布的 -->
      <template v-else>
        <div v-if="!myGoods.length" class="empty">
          <span class="emoji">📮</span>你还没有发布过闲置，<router-link to="/publish" style="color: var(--accent)">去发布</router-link>
        </div>
        <div v-for="g in myGoods" :key="g.id" class="order-item">
          <div class="order-icon">
            <img v-if="firstImage(g.images)" :src="firstImage(g.images)" style="width: 100%; height: 100%; object-fit: cover" alt="" />
            <template v-else>{{ catEmoji(g.categoryId) }}</template>
          </div>
          <div>
            <div class="order-title">{{ g.title }}</div>
            <div class="order-sub">{{ money(g.price) }} · {{ catName(g.categoryId) }} · {{ fmtTime(g.createTime) }}</div>
            <div class="order-sub">浏览 {{ g.viewCount || 0 }} · 收藏 {{ g.favoriteCount || 0 }}</div>
          </div>
          <div class="order-right">
            <span class="badge" :class="(GOODS_STATUS[g.status] || ['未知', 'badge-muted'])[1]">
              {{ (GOODS_STATUS[g.status] || ['未知'])[0] }}
            </span>
            <router-link class="link-btn" :to="'/goods/' + g.id">查看详情</router-link>
          </div>
        </div>
      </template>
    </template>
  </template>
</template>
