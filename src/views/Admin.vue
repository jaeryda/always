<template>
  <div class="page-container">
    <el-container>
      <el-main class="page-main">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>관리자 페이지</span>
            </div>
          </template>
          <el-alert type="success" :closable="false" title="관리자 기능 준비됨">
            <p>권한 기반 라우팅이 적용되어 관리자만 접근 가능합니다.</p>
          </el-alert>
          <el-divider />
          <el-space direction="vertical" style="width:100%">
            <el-card shadow="never">메뉴 관리: 기존 메뉴 API와 연동 가능</el-card>
            <el-card shadow="never">사용자 관리: 추후 관리자 전용 API 연동</el-card>
            <el-card shadow="never">시스템 로그/알림 관리: 알림센터와 연동 가능</el-card>
          </el-space>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>
