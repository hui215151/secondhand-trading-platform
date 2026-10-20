<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/api'
import { toast } from '@/store'
import GoodsCard from '@/components/GoodsCard.vue'
import Pager from '@/components/Pager.vue'
import { CATEGORIES } from '@/constants'

const route = useRoute()
const router = useRouter()

const filters = reactive({ category: '', minPrice: '', maxPrice: '' })
const keyword = ref('')
const page = ref(1)
const size = 12
const list = ref([])
const meta = ref({ total: 0, pages: 0, current: 1 })
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const q = new URLSearchParams({ page: page.value, size })
    if (keyword.value) q.set('keyword', keyword.value)
    if (filters.category) q.set('categoryId', filters.category)
    if (filters.minPrice) q.set('minPrice', filters.minPrice)
    if (filters.maxPrice) q.set('maxPrice', filters.maxPrice)
    const data = await api('/goods/list?' + q.toString())
    list.value = data.records || []
    meta.value = { total: data.total || 0, pages: data.pages || 0, current: data.current || 1 }
  } catch (e) {
    toast(e.message, 'err')
  } finally {
    loading.value = false
  }
}

function goPage(p) {
  page.value = p
  load()
}

function applyFilters() {
  page.value = 1
  load()
}

function resetFilters() {
  filters.category = ''
  filters.minPrice = ''
  filters.maxPrice = ''
  page.value = 1
  load()
}

function clearKeyword() {
  router.push({ path: '/' })
}

onMounted(() => {
  keyword.value = route.query.keyword || ''
  load()
})

watch(
  () => route.query.keyword,
  (k) => {
    keyword.value = k || ''
    page.value = 1
    load()
  }
)
</script>

<template>
  <div class="filters">
    <select v-model="filters.category" class="select" style="width: 132px">
      <option value="">全部分类</option>
      <option v-for="c in CATEGORIES" :key="c.id" :value="c.id">{{ c.name }}</option>
    </select>
    <input v-model="filters.minPrice" class="input" type="number" min="0" placeholder="最低价" style="width: 110px" />
    <span class="sep">—</span>
    <input v-model="filters.maxPrice" class="input" type="number" min="0" placeholder="最高价" style="width: 110px" />
    <button class="btn btn-sm" @click="applyFilters">筛选</button>
    <button class="btn btn-ghost btn-sm" @click="resetFilters">重置</button>
    <span v-if="keyword" class="muted small">
      关键词：<b>{{ keyword }}</b>
      <a class="link-btn" @click="clearKeyword">清除</a>
    </span>
  </div>

  <div v-if="list.length" class="grid">
    <GoodsCard v-for="g in list" :key="g.id" :goods="g" />
  </div>
  <div v-else-if="!loading" class="empty">
    <span class="emoji">🛍️</span>还没有符合条件的商品，换个关键词试试？
  </div>

  <Pager :current="meta.current" :pages="meta.pages" :total="meta.total" @change="goPage" />
</template>
