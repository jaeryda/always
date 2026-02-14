<template>
  <div id="app">
    <el-container>
      <el-header class="app-header">
        <div class="app-header-content">
          <div class="app-logo" @click="$router.push('/')">
            <img src="@/assets/logo.svg" alt="Always Logo" class="logo-image" />
          </div>

          <el-menu :default-active="activeIndex" class="nav-menu" mode="horizontal" router>
            <el-menu-item v-for="menu in menuStore.visibleMenus" :key="menu.id" :index="menu.path">
              <el-icon v-if="getIconComponent(menu.icon)">
                <component :is="getIconComponent(menu.icon)" />
              </el-icon>
              <span style="margin-left: 8px;">{{ menu.name }}</span>
            </el-menu-item>

            <template v-if="authStore.isAuthenticated">
              <el-menu-item index="/bookmarks">
                <el-icon><Star /></el-icon>
                <span style="margin-left: 8px;">북마크</span>
              </el-menu-item>
              <el-menu-item index="/ai-history">
                <el-icon><Clock /></el-icon>
                <span style="margin-left: 8px;">AI 히스토리</span>
              </el-menu-item>
              <el-menu-item index="/notifications">
                <el-badge :value="notificationStore.unreadCount" :hidden="notificationStore.unreadCount === 0" :max="99">
                  <el-icon><Bell /></el-icon>
                </el-badge>
                <span style="margin-left: 8px;">알림</span>
              </el-menu-item>
              <el-menu-item v-if="authStore.isAdmin" index="/admin">
                <el-icon><Setting /></el-icon>
                <span style="margin-left: 8px;">관리자</span>
              </el-menu-item>
            </template>

            <template v-if="!authStore.isAuthenticated">
              <el-menu-item index="/login">
                <el-icon><User /></el-icon>
                <span style="margin-left: 8px;">로그인</span>
              </el-menu-item>
              <el-menu-item index="/register">
                <el-icon><UserFilled /></el-icon>
                <span style="margin-left: 8px;">회원가입</span>
              </el-menu-item>
            </template>

            <template v-else>
              <el-menu-item index="user-info" disabled>
                <el-icon><User /></el-icon>
                <span style="margin-left: 8px;">{{ authStore.username }}님</span>
              </el-menu-item>
              <el-menu-item index="logout" @click="handleLogout" style="cursor: pointer;">
                <el-icon><Right /></el-icon>
                <span style="margin-left: 8px;">로그아웃</span>
              </el-menu-item>
            </template>
          </el-menu>
        </div>
      </el-header>

      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useMenuStore } from '@/store/menu'
import { useAuthStore } from '@/store/auth'
import { useNotificationStore } from '@/store/notifications'
import {
  House,
  Document,
  InfoFilled,
  ChatDotRound,
  User,
  UserFilled,
  Right,
  CreditCard,
  Camera,
  VideoCamera,
  Star,
  Bell,
  Clock,
  Setting
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const menuStore = useMenuStore()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()
const activeIndex = computed(() => route.path)

const handleLogout = (): void => {
  authStore.logout()
}

const getIconComponent = (iconName: string | null | undefined) => {
  if (!iconName) return null

  const iconMap: Record<string, any> = {
    Home: House,
    Document,
    InfoFilled,
    ChatDotRound,
    Wallet: CreditCard,
    Picture: Camera,
    VideoCamera
  }

  return iconMap[iconName] || null
}

onMounted(() => {
  menuStore.fetchMenus(true)
  if (authStore.isAuthenticated) {
    notificationStore.load()
    notificationStore.connectStream()
  }

  const urlParams = new URLSearchParams(window.location.search)
  if (urlParams.get('kakaoLogin') === 'success' || urlParams.get('naverLogin') === 'success') {
    setTimeout(() => {
      authStore.restoreAuth(true).then(() => {
        ElMessage.success('소셜 로그인이 완료되었습니다.')
        window.history.replaceState({}, document.title, window.location.pathname)
      })
    }, 100)
  }
})

watch(
  () => authStore.isAuthenticated,
  (isAuthed) => {
    if (isAuthed) {
      notificationStore.load()
      notificationStore.connectStream()
    } else {
      notificationStore.disconnectStream()
    }
  }
)
</script>
