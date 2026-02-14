import api from './index'

export interface NotificationItem {
  id: number
  userId: number
  title: string
  message: string
  read: boolean
  createdAt: string
}

export const notificationsApi = {
  list() {
    return api.get<{ success: boolean; notifications: NotificationItem[] }>('/notifications')
  },
  create(title: string, message: string) {
    return api.post<{ success: boolean; notification: NotificationItem }>('/notifications', { title, message })
  },
  markAsRead(id: number) {
    return api.put<{ success: boolean }>(`/notifications/${id}/read`)
  },
  markAllAsRead() {
    return api.put<{ success: boolean }>('/notifications/read-all')
  },
  delete(id: number) {
    return api.delete<{ success: boolean }>(`/notifications/${id}`)
  },
  deleteAll() {
    return api.delete<{ success: boolean }>('/notifications')
  }
}

