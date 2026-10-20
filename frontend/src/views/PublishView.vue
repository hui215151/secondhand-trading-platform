<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/api'
import { isLoggedIn, openAuth, toast } from '@/store'
import { CATEGORIES } from '@/constants'

const router = useRouter()
const form = reactive({ title: '', description: '', price: '', originalPrice: '', categoryId: 1 })
const images = ref([])
const loading = ref(false)
const fileInput = ref(null)

async function onFile(e) {
  const files = Array.from(e.target.files || [])
  for (const file of files) {
    if (images.value.length >= 3) {
      toast('最多上传 3 张图片', 'err')
      break
    }
    if (file.size > 5 * 1024 * 1024) {
      toast('图片「' + file.name + '」超过 5MB', 'err')
      continue
    }
    const fd = new FormData()
    fd.append('file', file)
    try {
      images.value.push(await api('/file/upload', { method: 'POST', body: fd }))
    } catch (err) {
      toast(err.message, 'err')
    }
  }
  e.target.value = ''
}

function removeImage(i) {
  images.value.splice(i, 1)
}

async function submit() {
  if (!form.title.trim()) {
    toast('请填写商品标题', 'err')
    return
  }
  if (!form.price || Number(form.price) <= 0) {
    toast('请填写正确的价格', 'err')
    return
  }
  loading.value = true
  try {
    await api('/goods/publish', {
      method: 'POST',
      body: {
        title: form.title.trim(),
        description: form.description.trim(),
        price: Number(form.price),
        originalPrice: form.originalPrice ? Number(form.originalPrice) : null,
        categoryId: Number(form.categoryId),
        images: images.value.join(',')
      }
    })
    toast('发布成功，等待管理员审核', 'ok')
    router.push({ path: '/me', query: { tab: 'mine' } })
  } catch (e) {
    toast(e.message, 'err')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-if="!isLoggedIn" class="empty">
    <span class="emoji">🔐</span>发布闲置需要先登录<br /><br />
    <button class="btn" @click="openAuth('login')">立即登录</button>
  </div>

  <template v-else>
    <div class="section-title">发布闲置</div>
    <div class="form-card">
      <form class="form" @submit.prevent="submit">
        <div class="field">
          <label>商品标题</label>
          <input v-model="form.title" class="input" required maxlength="100" placeholder="例如：iPhone 13 128G 九成新" />
        </div>

        <div class="field">
          <label>商品描述</label>
          <textarea v-model="form.description" class="textarea" maxlength="2000" placeholder="成色、使用时长、配件、交易方式…"></textarea>
        </div>

        <div class="row-2">
          <div class="field">
            <label>售价（元）</label>
            <input v-model="form.price" class="input" type="number" min="0.01" step="0.01" required placeholder="0.00" />
          </div>
          <div class="field">
            <label>原价（元，选填）</label>
            <input v-model="form.originalPrice" class="input" type="number" min="0" step="0.01" placeholder="0.00" />
          </div>
        </div>

        <div class="field">
          <label>分类</label>
          <select v-model="form.categoryId" class="select">
            <option v-for="c in CATEGORIES" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
        </div>

        <div class="field">
          <label>商品图片 <span class="hint">最多 3 张，单张不超过 5MB</span></label>
          <div class="thumbs">
            <div v-for="(u, i) in images" :key="u" class="thumb-item">
              <img :src="u" alt="" />
              <button type="button" @click="removeImage(i)">✕</button>
            </div>
            <label v-if="images.length < 3" class="upload-box">
              <span class="plus">＋</span><span>上传图片</span>
              <input ref="fileInput" type="file" accept="image/*" multiple hidden @change="onFile" />
            </label>
          </div>
        </div>

        <div class="modal-actions" style="justify-content: flex-start">
          <button class="btn" type="submit" :disabled="loading">提交发布</button>
          <router-link class="btn btn-ghost" to="/me">取消</router-link>
        </div>
      </form>
    </div>
  </template>
</template>
