<script setup>
import { computed } from 'vue'
import { catName, catEmoji, firstImage } from '@/constants'

const props = defineProps({
  goods: { type: Object, required: true }
})

const img = computed(() => firstImage(props.goods.images))
</script>

<template>
  <router-link class="goods-card" :to="'/goods/' + goods.id">
    <div class="thumb">
      <img v-if="img" :src="img" :alt="goods.title" loading="lazy" />
      <span v-else>{{ catEmoji(goods.categoryId) }}</span>
    </div>
    <div class="goods-body">
      <div class="goods-title">{{ goods.title }}</div>
      <div class="price-row">
        <span class="price"><small>¥</small>{{ Number(goods.price).toFixed(2) }}</span>
        <span v-if="goods.originalPrice" class="origin">¥{{ Number(goods.originalPrice).toFixed(2) }}</span>
      </div>
      <div class="meta">
        <span>{{ catName(goods.categoryId) }}</span>
        <span>浏览 {{ goods.viewCount || 0 }} · 收藏 {{ goods.favoriteCount || 0 }}</span>
      </div>
    </div>
  </router-link>
</template>
