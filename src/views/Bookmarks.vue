<template>
  <div class="page-container">
    <el-container>
      <el-main class="page-main">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>북마크한 글</span>
            </div>
          </template>

          <el-empty v-if="posts.length === 0" description="북마크한 글이 없습니다." />

          <el-table v-else :data="posts" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="제목" min-width="220">
              <template #default="{ row }">
                <el-link type="primary" @click="$router.push(`/posts/${row.id}`)">{{ row.title }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="내용" min-width="260" show-overflow-tooltip />
          </el-table>
          <el-pagination
            v-if="total > pageSize"
            style="margin-top: 16px; justify-content: center;"
            :current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="handlePageChange" />
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import type { Post } from '@/api/posts'
import { socialApi } from '@/api/social'

const posts = ref<Post[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const load = async () => {
  const response = await socialApi.getBookmarks(currentPage.value - 1, pageSize.value)
  posts.value = (response.data.posts || []) as Post[]
  total.value = response.data.total || 0
}

const handlePageChange = async (page: number) => {
  currentPage.value = page
  await load()
}

onMounted(load)
</script>
