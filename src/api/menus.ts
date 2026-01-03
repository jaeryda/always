import api from './index'

export interface Menu {
  id?: number
  name: string
  path: string
  icon?: string | null
  displayOrder: number
  visible: boolean
  createdAt?: string
  updatedAt?: string
}

export interface MenuResponse {
  menus: Menu[]
  count: number
  timestamp: string
}

export interface CreateMenuData {
  name: string
  path: string
  icon?: string
  displayOrder: number
  visible?: boolean
}

export const menusApi = {
  // 모든 메뉴 가져오기
  getAllMenus(visible?: boolean) {
    const params = visible !== undefined ? { visible } : {}
    return api.get<MenuResponse>('/menus', { params })
  },

  // 특정 메뉴 가져오기
  getMenuById(id: number) {
    return api.get<{ menu: Menu; timestamp: string }>(`/menus/${id}`)
  },

  // 메뉴 생성
  createMenu(data: CreateMenuData) {
    return api.post<{ message: string; menu: Menu; timestamp: string }>('/menus', data)
  },

  // 메뉴 업데이트
  updateMenu(id: number, data: CreateMenuData) {
    return api.put<{ message: string; menu: Menu; timestamp: string }>(`/menus/${id}`, data)
  },

  // 메뉴 삭제
  deleteMenu(id: number) {
    return api.delete<{ message: string; id: number; timestamp: string }>(`/menus/${id}`)
  }
}

