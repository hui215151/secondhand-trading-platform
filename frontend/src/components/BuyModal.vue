<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ui, closeBuy, toast } from '@/store'
import { api } from '@/api'
import { money } from '@/constants'

const router = useRouter()
const address = ref('')
const remark = ref('')
const loading = ref(false)

watch(
  () => ui.buy,
  (goods) => {
    if (goods) {
      address.value = ''
      remark.value = ''
    }
  }
)

async function submit() {
  if (!address.value.trim()) {
    toast('请填写收货地址', 'err')
    return
  }
  loading.value = true
  try {
    await api('/order/create', {
      method: 'POST',
      form: { goodsId: ui.buy.id, address: address.value.trim(), remark: remark.value.trim() }
    })
    closeBuy()
    toast('下单成功，请尽快付款', 'ok')
    router.push({ path: '/me', query: { tab: 'orders' } })
  } catch (e) {
    toast(e.message, 'err')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-if="ui.buy" class="overlay" @click.self="closeBuy">
    <div class="modal">
      <h3>确认下单</h3>
      <p class="modal-sub">{{ ui.buy.title }} · <b style="color: var(--accent)">{{ money(ui.buy.price) }}</b></p>

      <form class="form" @submit.prevent="submit">
        <div class="field">
          <label>收货地址</label>
          <input v-model="address" class="input" required maxlength="200" placeholder="例如：3 号宿舍楼 301" />
        </div>
        <div class="field">
          <label>备注 <span class="hint">选填</span></label>
          <input v-model="remark" class="input" maxlength="500" placeholder="交易时间、联系方式…" />
        </div>
        <div class="modal-actions">
          <button type="button" class="btn btn-ghost" @click="closeBuy">再想想</button>
          <button class="btn" type="submit" :disabled="loading">提交订单</button>
        </div>
      </form>
    </div>
  </div>
</template>
