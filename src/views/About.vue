<template>
  <div class="page-container">
    <el-container>
      <el-main class="about-main">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header about-card-header">
              <span>프로젝트 구성</span>
            </div>
          </template>
          
          <el-descriptions :column="1">
            <el-descriptions-item label="프레임워크">
              <el-tag type="success">Vue 3</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="UI 라이브러리">
              <el-tag type="primary">Element Plus</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="라우터">
              <el-tag type="info">Vue Router 4</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="상태 관리">
              <el-tag type="warning">Pinia</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="HTTP 클라이언트">
              <el-tag type="danger">Axios</el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <el-divider />
          
          <el-space direction="vertical" :size="16" style="width: 100%">
            <h3>설치된 패키지</h3>
            <div class="package-list">
              <div class="package-item">
                <el-icon><Check /></el-icon>
                <span style="margin-left: 10px">vue@{{ vueVersion }}</span>
              </div>
              <div class="package-item">
                <el-icon><Check /></el-icon>
                <span style="margin-left: 10px">vue-router@{{ routerVersion }}</span>
              </div>
              <div class="package-item">
                <el-icon><Check /></el-icon>
                <span style="margin-left: 10px">pinia@{{ piniaVersion }}</span>
              </div>
              <div class="package-item">
                <el-icon><Check /></el-icon>
                <span style="margin-left: 10px">element-plus@{{ elementPlusVersion }}</span>
              </div>
              <div class="package-item">
                <el-icon><Check /></el-icon>
                <span style="margin-left: 10px">axios@{{ axiosVersion }}</span>
              </div>
            </div>
          </el-space>

          <el-divider />

          <el-space>
            <el-button type="primary" @click="$router.push('/')">홈으로</el-button>
            <el-button type="success" @click="$router.back()">뒤로가기</el-button>
          </el-space>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { Check } from '@element-plus/icons-vue'

// 버전 정보 (실제로는 package.json에서 가져올 수 있음)
const vueVersion = '^3.2.13'
const routerVersion = '^4.6.4'
const piniaVersion = '^3.0.4'
const elementPlusVersion = '^2.13.0'
const axiosVersion = '^1.13.2'
</script>


