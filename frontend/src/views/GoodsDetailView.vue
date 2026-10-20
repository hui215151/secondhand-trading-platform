<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '@/api'
import { auth, isLoggedIn, openAuth, openBuy, toast, authVersion } from '@/store'
import { catName, catEmoji, firstImage, money, fmtTime, GOODS_STATUS } from '@/constants'

const route = useRoute()

const goods = ref(null)
const seller = ref(null)
const faved = ref(false)
const loading = ref(true)
const busy = ref(false)

const img = computed(() => (goods.value ? firstImage(goods.value.images) : ''))
const status = computed(() => GOODS_STATUS[goods.value ? goods.value.status : 0] || ['未知', 'badge-muted'])
const isMine = computed(() => !!(auth.user && goods.value && Number(auth.user.id) === Number(goods.value.userId)))

async function load() {
  loading.value = true
  try {
    goods.value = await api('/goods/detail/' + route.params.id)
    try {
      seller.value = await api('/user/profile/' + goods.value.userId)
    } catch (e) {
      seller.value = null
    }
    faved.value = false
    if (auth.token) {
      try {
        faved.value = await api('/favorite/check/' + route.params.id)
      } catch (e) {
        faved.value = false
      }
    }
  } catch (e) {
    toast(e.message, 'err')
  } finally {
    loading.value = false
  }
}

async function toggleFav() {
  if (!isLoggedIn.value) {
    openAuth('login')
    return
  }
  busy.value = true
  try {
    await api('/favorite/' + (faved.value ? 'remove' : 'add') + '/' + goods.value.id, {
      method: faved.value ? 'DELETE' : 'POST'
    })
    faved.value = !faved.value
    goods.value.favoriteCount = Math.max(0, (goods.value.favoriteCount || 0) + (faved.value ? 1 : -1))
    toast(faved.value ? '收藏成功' : '已取消收藏', 'ok')
  } catch (e) {
    toast(e.message, 'err')
  } finally {
    busy.value = false
  }
}

function buy() {
  if (!isLoggedIn.value) {
    openAuth('login')
    return
  }
  openBuy(goods.value)
}

onMounted(load)
watch(() => route.params.id, load)
watch(authVersion, () => {
  if (goods.value) load()
})
</script>

<template>
  <div v-if="loading" class="empty"><span class="emoji">⏳</span>加载中…</div>

  <div v-else-if="goods" class="detail">
    <div>
      <div class="detail-media">
        <img v-if="img" :src="img" :alt="goods.title" />
        <span v-else>{{ catEmoji(goods.categoryId) }}</span>
      </div>
    </div>

    <div>
      <h1 class="detail-title">{{ goods.title }}</h1>

      <div class="detail-price">
        <span class="price"><small>¥</small>{{ Number(goods.price).toFixed(2) }}</span>
        <span v-if="goods.originalPrice" class="origin">原价 {{ money(goods.originalPrice) }}</span>
      </div>

      <div>
        <span class="badge" :class="status[1]">{{ status[0] }}</span>
        <span class="badge badge-muted">{{ catName(goods.categoryId) }}</span>
      </div>

      <p class="detail-desc">{{ goods.description || '卖家很懒，什么都没写~' }}</p>

      <div class="detail-meta">
        <div>卖家：<b>{{ seller ? seller.nickname : '用户' + goods.userId }}</b></div>
        <div>浏览：<b>{{ goods.viewCount || 0 }}</b> 次</div>
        <div>收藏：<b>{{ goods.favoriteCount || 0 }}</b> 人</div>
        <div>发布：<b>{{ fmtTime(goods.createTime) }}</b></div>
      </div>

      <div class="detail-actions">
        <button v-if="isMine" class="btn btn-ghost" disabled>这是你发布的商品</button>
        <button v-else class="btn" :disabled="goods.status !== 1" @click="buy">
          {{ goods.status === 1 ? '立即购买' : '暂时无法购买' }}
        </button>
        <button class="btn btn-ghost" :disabled="busy" @click="toggleFav">
          {{ faved ? '❤️ 已收藏' : '🤍 收藏' }}
        </button>
        <router-link class="btn btn-ghost" to="/">继续逛逛</router-link>
      </div>
    </div>
  </div>

  <div v-else class="empty"><span class="emoji">😶</span>商品不存在或已下架</div>
</template>

<style scoped>
.detail-price .origin {
  margin-left: 12px;
  vertical-align: 4px;
}
</style>
