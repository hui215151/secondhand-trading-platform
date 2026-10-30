export const CATEGORIES = [
  { id: 1, name: '数码' },
  { id: 2, name: '电脑' },
  { id: 3, name: '图书' },
  { id: 4, name: '生活用品' },
  { id: 5, name: '运动户外' },
  { id: 6, name: '其他' }
]

export const CAT_EMOJI = { 1: '📱', 2: '💻', 3: '📚', 4: '🏠', 5: '🏀', 6: '📦' }

export const GOODS_STATUS = {
  0: ['待审核', 'badge-warn'],
  1: ['在售', 'badge-ok'],
  2: ['已下架', 'badge-muted'],
  3: ['已售出', 'badge-danger']
}

export const ORDER_STATUS = {
  0: ['待付款', 'badge-warn'],
  1: ['已付款', 'badge-info'],
  2: ['已发货', 'badge-info'],
  3: ['已完成', 'badge-ok'],
  4: ['已取消', 'badge-muted']
}

export function catName(id) {
  const c = CATEGORIES.find((x) => x.id === Number(id))
  return c ? c.name : '其他'
}

export function catEmoji(id) {
  return CAT_EMOJI[Number(id)] || '📦'
}

export function firstImage(images) {
  return images ? String(images).split(',')[0] : ''
}

export function money(n) {
  return '¥' + Number(n || 0).toFixed(2)
}

export function fmtTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 16) : '—'
}
