import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { notificationsApi, NotificationItem } from '@/api/notifications'

export const useNotificationStore = defineStore('notifications', () => {
  const notifications = ref<NotificationItem[]>([])

  const unreadCount = computed(() => notifications.value.filter((n) => !n.read).length)

  const load = async () => {
    const response = await notificationsApi.list()
    notifications.value = response.data.notifications || []
  }

  const pushNotification = async (title: string, message: string) => {
    await notificationsApi.create(title, message)
    await load()
  }

  const markAsRead = async (id: number) => {
    await notificationsApi.markAsRead(id)
    await load()
  }

  const markAllAsRead = async () => {
    await notificationsApi.markAllAsRead()
    await load()
  }

  const removeNotification = async (id: number) => {
    await notificationsApi.delete(id)
    await load()
  }

  const clearAll = async () => {
    await notificationsApi.deleteAll()
    await load()
  }

  return {
    notifications,
    unreadCount,
    load,
    pushNotification,
    markAsRead,
    markAllAsRead,
    removeNotification,
    clearAll
  }
})

