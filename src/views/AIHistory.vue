<template>
  <div class="page-container">
    <el-container>
      <el-main class="page-main">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>AI 히스토리</span>
              <el-button size="small" type="danger" plain @click="clearMine">내 기록 전체 삭제</el-button>
            </div>
          </template>

          <el-empty v-if="historyStore.items.length === 0" description="AI 기록이 없습니다." />

          <el-table v-else :data="historyStore.items" stripe>
            <el-table-column label="유형" width="100">
              <template #default="{ row }">
                <el-tag>{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="prompt" label="Prompt" min-width="280" show-overflow-tooltip />
            <el-table-column label="결과" min-width="240">
              <template #default="{ row }">
                <a v-if="row.resultUrl" :href="row.resultUrl" target="_blank" rel="noopener noreferrer">미리보기/열기</a>
                <span v-else>{{ row.resultText?.slice(0, 80) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="시간" width="170">
              <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="작업" width="100">
              <template #default="{ row }">
                <el-button size="small" type="danger" plain @click="historyStore.removeItem(row.id)">삭제</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import dayjs from 'dayjs'
import { useAIHistoryStore } from '@/store/aiHistory'

const historyStore = useAIHistoryStore()
const formatDateTime = (value: string) => dayjs(value).format('YYYY-MM-DD HH:mm')

const clearMine = async () => {
  await historyStore.clearMine()
}

onMounted(() => {
  historyStore.load()
})
</script>
