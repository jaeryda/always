import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Home from '../views/Home.vue'
import Posts from '../views/Posts.vue'
import PostDetail from '../views/PostDetail.vue'
import About from '../views/About.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import SocialRegister from '../views/SocialRegister.vue'
import AI from '../views/AI.vue'
import ImageGenerator from '../views/ImageGenerator.vue'
import VideoGenerator from '../views/VideoGenerator.vue'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

const routes: Array<RouteRecordRaw> = [
  { path: '/', name: 'Home', component: Home },
  { path: '/login', name: 'Login', component: Login, meta: { guestOnly: true } },
  { path: '/register', name: 'Register', component: Register, meta: { guestOnly: true } },
  { path: '/social-register', name: 'SocialRegister', component: SocialRegister, meta: { guestOnly: true } },
  { path: '/posts', name: 'Posts', component: Posts },
  { path: '/posts/:id', name: 'PostDetail', component: PostDetail, props: true },
  { path: '/bookmarks', name: 'Bookmarks', component: () => import('../views/Bookmarks.vue'), meta: { requiresAuth: true } },
  { path: '/about', name: 'About', component: About },
  { path: '/ai', name: 'AI', component: AI },
  { path: '/ai-history', name: 'AIHistory', component: () => import('../views/AIHistory.vue'), meta: { requiresAuth: true } },
  { path: '/image-generator', name: 'ImageGenerator', component: ImageGenerator },
  { path: '/video-generator', name: 'VideoGenerator', component: VideoGenerator },
  { path: '/notifications', name: 'Notifications', component: () => import('../views/Notifications.vue'), meta: { requiresAuth: true } },
  { path: '/admin', name: 'Admin', component: () => import('../views/Admin.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/profile/:id?', name: 'Profile', component: () => import('../views/Profile.vue'), props: true, meta: { requiresAuth: true } },
  { path: '/account-book', name: 'AccountBook', component: () => import('../views/AccountBook.vue'), meta: { requiresAuth: true } },
  { path: '/transactions', name: 'Transactions', component: () => import('../views/Transactions.vue'), meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

NProgress.configure({
  showSpinner: false,
  trickleSpeed: 100
})

const hasAuthState = (): boolean => localStorage.getItem('always_auth_state') === '1'
const getRole = (): string => localStorage.getItem('always_user_role') || 'user'

router.beforeEach((to, from, next) => {
  NProgress.start()

  const authed = hasAuthState()
  if (to.meta.requiresAuth && !authed) {
    next('/login')
    return
  }

  if (to.meta.guestOnly && authed) {
    next('/')
    return
  }

  if (to.meta.requiresAdmin && getRole() !== 'admin') {
    next('/')
    return
  }

  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
