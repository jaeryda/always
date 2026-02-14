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

onMounted(async () => {
  const response = await socialApi.getBookmarks()
  posts.value = (response.data.posts || []) as Post[]
})
</script>
