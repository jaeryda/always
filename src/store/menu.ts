import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { menusApi, type Menu } from '@/api/menus'

export const useMenuStore = defineStore('menu', () => {
  // state
  const menus = ref<Menu[]>([])
  const loading = ref<boolean>(false)

  // getters
  const visibleMenus = computed(() => {
    return menus.value
      .filter(menu => menu.visible && menu.path !== '/') // 홈 메뉴 제외 (로고 클릭으로 이동)
      .sort((a, b) => a.displayOrder - b.displayOrder)
  })

  // actions
  async function fetchMenus(visibleOnly = true): Promise<void> {
    loading.value = true
    try {
      const response = await menusApi.getAllMenus(visibleOnly)
      if (response.data && 'menus' in response.data) {
        menus.value = response.data.menus
      } else {
        menus.value = Array.isArray(response.data) ? response.data : []
      }
    } catch (error) {
      console.error('메뉴를 가져오는데 실패했습니다:', error)
      menus.value = []
    } finally {
      loading.value = false
    }
  }

  async function createMenu(menuData: {
    name: string
    path: string
    icon?: string
    displayOrder: number
    visible?: boolean
  }): Promise<void> {
    try {
      await menusApi.createMenu(menuData)
      await fetchMenus(false) // 전체 메뉴 다시 가져오기
    } catch (error) {
      console.error('메뉴 생성에 실패했습니다:', error)
      throw error
    }
  }

  async function updateMenu(id: number, menuData: {
    name: string
    path: string
    icon?: string
    displayOrder: number
    visible?: boolean
  }): Promise<void> {
    try {
      await menusApi.updateMenu(id, menuData)
      await fetchMenus(false) // 전체 메뉴 다시 가져오기
    } catch (error) {
      console.error('메뉴 업데이트에 실패했습니다:', error)
      throw error
    }
  }

  async function deleteMenu(id: number): Promise<void> {
    try {
      await menusApi.deleteMenu(id)
      await fetchMenus(false) // 전체 메뉴 다시 가져오기
    } catch (error) {
      console.error('메뉴 삭제에 실패했습니다:', error)
      throw error
    }
  }

  return {
    menus,
    loading,
    visibleMenus,
    fetchMenus,
    createMenu,
    updateMenu,
    deleteMenu
  }
})

