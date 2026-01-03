<template>
  <div id="app">
    <el-container>
      <el-header class="app-header">
        <div class="app-header-content">
          <h1 class="app-logo" @click="$router.push('/')">Always</h1>
          <el-menu
            :default-active="activeIndex"
            class="nav-menu"
            mode="horizontal"
            router
            @select="handleSelect">
            <el-menu-item 
              v-for="menu in menuStore.visibleMenus" 
              :key="menu.id"
              :index="menu.path">
              <el-icon v-if="getIconComponent(menu.icon)">
                <component :is="getIconComponent(menu.icon)" />
              </el-icon>
              <span style="margin-left: 8px;">{{ menu.name }}</span>
            </el-menu-item>
            
            <!-- 인증 메뉴 -->
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
              <el-menu-item 
                index="logout"
                @click="handleLogout"
                style="cursor: pointer;">
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
import { House, Document, InfoFilled, ChatDotRound, User, UserFilled, Right, CreditCard } from '@element-plus/icons-vue'

const route = useRoute()
const menuStore = useMenuStore()
const authStore = useAuthStore()
const activeIndex = computed(() => route.path)

// overflow 메뉴 닫기 타이머 관리
let closeMenuTimer: ReturnType<typeof setTimeout> | null = null

// Element Plus의 overflow 메뉴(드롭다운) 닫기
const closeOverflowMenu = () => {
  // 기존 타이머가 있으면 취소
  if (closeMenuTimer) {
    clearTimeout(closeMenuTimer)
    closeMenuTimer = null
  }
  
  // 즉시 실행하여 빠른 반응
  // 1. sub-menu의 is-opened 클래스 제거
  const overflowSubMenu = document.querySelector('.nav-menu .el-sub-menu.is-opened')
  if (overflowSubMenu) {
    overflowSubMenu.classList.remove('is-opened')
    
    // 2. sub-menu의 aria-expanded 속성 제거
    const trigger = overflowSubMenu.querySelector('.el-sub-menu__title') as HTMLElement
    if (trigger) {
      trigger.setAttribute('aria-expanded', 'false')
    }
  }
  
  // 3. popper를 직접 숨기기
  const poppers = document.querySelectorAll('.el-popper.el-menu--horizontal')
  poppers.forEach(popper => {
    const popperElement = popper as HTMLElement
    popperElement.style.display = 'none'
    popperElement.style.visibility = 'hidden'
    popperElement.style.opacity = '0'
  })
  
  // 4. 한 번만 추가 확인 (Element Plus 내부 상태 반영 대기)
  closeMenuTimer = setTimeout(() => {
    const stillOpen = document.querySelector('.nav-menu .el-sub-menu.is-opened')
    if (stillOpen) {
      stillOpen.classList.remove('is-opened')
      const trigger = stillOpen.querySelector('.el-sub-menu__title') as HTMLElement
      if (trigger) {
        trigger.setAttribute('aria-expanded', 'false')
      }
    }
    
    // popper 재확인 및 제거
    const remainingPoppers = document.querySelectorAll('.el-popper.el-menu--horizontal')
    remainingPoppers.forEach(popper => {
      const popperElement = popper as HTMLElement
      popperElement.style.display = 'none'
      popperElement.style.visibility = 'hidden'
      popperElement.style.opacity = '0'
    })
    
    closeMenuTimer = null
  }, 150)
}

const handleSelect = (key: string): void => {
  console.log('Selected menu:', key)
  
  // 메뉴 선택 시 overflow 메뉴 닫기 (handleSelect에서만 실행)
  closeOverflowMenu()
}

// 라우트 변경 시 overflow 메뉴 닫기 (handleSelect와 중복 방지를 위해 약간의 지연)
watch(() => route.path, () => {
  // handleSelect가 이미 실행했을 수 있으므로 약간의 지연 후 실행
  setTimeout(() => {
    closeOverflowMenu()
  }, 100)
})

const handleLogout = (): void => {
  authStore.logout()
}

const getIconComponent = (iconName: string | null | undefined) => {
  if (!iconName) return null
  
  const iconMap: Record<string, any> = {
    'Home': House,
    'Document': Document,
    'InfoFilled': InfoFilled,
    'ChatDotRound': ChatDotRound,
    'Wallet': CreditCard
  }
  
  return iconMap[iconName] || null
}
onMounted(() => {
  menuStore.fetchMenus(true)
})
</script>