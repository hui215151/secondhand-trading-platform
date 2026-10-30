import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'home', component: () => import('./views/HomeView.vue') },
  { path: '/goods/:id', name: 'goods', component: () => import('./views/GoodsDetailView.vue') },
  { path: '/publish', name: 'publish', component: () => import('./views/PublishView.vue') },
  { path: '/me', name: 'me', component: () => import('./views/MeView.vue') },
  { path: '/admin', name: 'admin', component: () => import('./views/AdminView.vue') },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

export default router
