<script setup>
defineProps({
  current: { type: Number, default: 1 },
  pages: { type: Number, default: 0 },
  total: { type: Number, default: 0 }
})
defineEmits(['change'])
</script>

<template>
  <div v-if="pages > 1" class="pager">
    <button class="btn btn-ghost btn-sm" :disabled="current <= 1" @click="$emit('change', current - 1)">上一页</button>
    <span class="page-info">第 {{ current }} / {{ pages }} 页 · 共 {{ total }} 件</span>
    <button class="btn btn-ghost btn-sm" :disabled="current >= pages" @click="$emit('change', current + 1)">下一页</button>
  </div>
  <div v-else-if="total" class="pager">
    <span class="page-info">共 {{ total }} 件</span>
  </div>
</template>
