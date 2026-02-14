<template>
  <div class="page-container">
    <el-container>
      <el-main class="page-main">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>알림 센터</span>
              <el-space>
                <el-button size="small" @click="notificationStore.markAllAsRead">전체 읽음</el-button>
                <el-button size="small" type="danger" plain @click="notificationStore.clearAll">전체 삭제</el-button>
              </el-space>
            </div>
          </template>

          <el-empty v-if="notificationStore.notifications.length === 0" description="알림이 없습니다." />

          <el-timeline v-else>
            <el-timeline-item
              v-for="item in notificationStore.notifications"
              :key="item.id"
              :timestamp="formatDateTime(item.createdAt)"
              :type="item.read ? 'info' : 'primary'">
              <el-card shadow="never">
                <div class="notice-row">
                  <div>
                    <strong>{{ item.title }}</strong>
                    <div>{{ item.message }}</div>
                  </div>
                  <el-space>
                    <el-button size="small" @click="notificationStore.markAsRead(item.id)">읽음</el-button>
                    <el-button size="small" type="danger" plain @click="notificationStore.removeNotification(item.id)">삭제</el-button>
                  </el-space>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import dayjs from 'dayjs'
import { useNotificationStore } from '@/store/notifications'

const notificationStore = useNotificationStore()

const formatDateTime = (value: string) => dayjs(value).format('YYYY-MM-DD HH:mm')

onMounted(() => {
  notificationStore.load()
})
</script>

<style scoped>
.notice-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
</style>
