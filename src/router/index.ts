import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Home from '../views/Home.vue'
import Posts from '../views/Posts.vue'
import PostDetail from '../views/PostDetail.vue'
import About from '../views/About.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import SocialRegister from '../views/SocialRegister.vue'
import AI from '../views/AI.vue'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/social-register',
    name: 'SocialRegister',
    component: SocialRegister
  },
  {
    path: '/posts',
    name: 'Posts',
    component: Posts
  },
  {
    path: '/posts/:id',
    name: 'PostDetail',
    component: PostDetail,
    props: true
  },
  {
    path: '/about',
    name: 'About',
    component: About
  },
  {
    path: '/ai',
    name: 'AI',
    component: AI
  },
  {
    path: '/profile/:id?',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    props: true
  },
  {
    path: '/account-book',
    name: 'AccountBook',
    component: () => import('../views/AccountBook.vue')
  },
  {
    path: '/transactions',
    name: 'Transactions',
    component: () => import('../views/Transactions.vue')
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// NProgress 설정
NProgress.configure({
  showSpinner: false,
  trickleSpeed: 100
})

// 라우터 네비게이션 가드에서 NProgress 사용
router.beforeEach((to, from, next) => {
  NProgress.start()
  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router

