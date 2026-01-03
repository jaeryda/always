<template>
  <div class="page-container account-book-page">
    <el-container>
      <el-main class="page-main">
        <!-- 월 선택 -->
        <div class="month-selector">
          <el-button 
            :icon="ArrowLeft" 
            circle 
            @click="changeMonth(-1)"
            :disabled="loading" />
          <div class="month-display">
            <span class="month-text">{{ currentYear }}년 {{ currentMonth }}월</span>
          </div>
          <el-button 
            :icon="ArrowRight" 
            circle 
            @click="changeMonth(1)"
            :disabled="loading" />
        </div>

        <!-- 통계 카드 -->
        <el-row :gutter="24" style="margin-top: 24px;">
          <!-- 수입 카드 -->
          <el-col :span="24" :md="8">
            <el-card class="stat-card income-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-header">
                  <span class="stat-label">수입</span>
                </div>
                <div class="stat-value income-value">
                  {{ formatCurrency(statistics.income) }}
                </div>
              </div>
            </el-card>
          </el-col>

          <!-- 지출 카드 -->
          <el-col :span="24" :md="8">
            <el-card class="stat-card expense-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-header">
                  <span class="stat-label">지출</span>
                </div>
                <div class="stat-value expense-value">
                  {{ formatCurrency(statistics.expense) }}
                </div>
              </div>
            </el-card>
          </el-col>

          <!-- 잔액 카드 -->
          <el-col :span="24" :md="8">
            <el-card 
              class="stat-card balance-card" 
              :class="{ 'negative-balance': statistics.balance < 0 }"
              shadow="hover">
              <div class="stat-content">
                <div class="stat-header">
                  <span class="stat-label">잔액</span>
                </div>
                <div class="stat-value balance-value">
                  {{ formatCurrency(statistics.balance) }}
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 최근 거래 내역 -->
        <el-card class="transactions-card" shadow="hover" style="margin-top: 24px;">
          <template #header>
            <div class="card-header">
              <span>거래 내역</span>
              <el-button type="primary" @click="$router.push('/transactions')">
                전체보기
              </el-button>
            </div>
          </template>

          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>

          <div v-else-if="recentTransactions.length === 0" class="empty-container">
            <el-empty description="거래 내역이 없습니다" />
            <el-button type="primary" @click="$router.push('/transactions')">
              거래 추가하기
            </el-button>
          </div>

          <div v-else class="transaction-list">
            <div 
              v-for="transaction in recentTransactions" 
              :key="transaction.id"
              class="transaction-item"
              @click="$router.push(`/transactions?edit=${transaction.id}`)">
              <div class="transaction-left">
                <div 
                  class="category-icon" 
                  :style="{ backgroundColor: transaction.category?.color || '#909399' }">
                  <el-icon v-if="transaction.category?.icon">
                    <component :is="getIcon(transaction.category.icon)" />
                  </el-icon>
                </div>
                <div class="transaction-info">
                  <div class="transaction-category">
                    {{ transaction.category?.name || '미분류' }}
                  </div>
                  <div class="transaction-description">
                    {{ transaction.description || '-' }}
                  </div>
                </div>
              </div>
              <div 
                class="transaction-amount"
                :class="transaction.type === 'INCOME' ? 'income' : 'expense'">
                {{ transaction.type === 'INCOME' ? '+' : '-' }}{{ formatCurrency(transaction.amount) }}
              </div>
            </div>
          </div>

          <!-- 홈으로 가기 버튼 -->
          <div style="margin-top: 24px; text-align: center;">
            <el-button type="primary" @click="$router.push('/')">
              홈으로
            </el-button>
          </div>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { accountBookApi, type Transaction, type StatisticsResponse } from '@/api/accountBook'
import { ElMessage } from 'element-plus'

const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1)
const loading = ref(false)
const statistics = ref<StatisticsResponse>({
  success: true,
  year: currentYear.value,
  month: currentMonth.value,
  income: 0,
  expense: 0,
  balance: 0
})
const recentTransactions = ref<Transaction[]>([])

const changeMonth = (delta: number) => {
  const date = new Date(currentYear.value, currentMonth.value - 1 + delta, 1)
  currentYear.value = date.getFullYear()
  currentMonth.value = date.getMonth() + 1
  loadData()
}

const loadStatistics = async () => {
  try {
    const response = await accountBookApi.getStatistics(currentYear.value, currentMonth.value)
    if (response.data.success) {
      statistics.value = response.data
    }
  } catch (error: any) {
    console.error('통계 조회 실패:', error)
    ElMessage.error('통계 조회 중 오류가 발생했습니다.')
  }
}

const loadRecentTransactions = async () => {
  try {
    const response = await accountBookApi.getAllTransactions(currentYear.value, currentMonth.value)
    if (response.data.success && response.data.transactions) {
      // 최근 10개만 표시
      recentTransactions.value = response.data.transactions.slice(0, 10)
    }
  } catch (error: any) {
    console.error('거래 내역 조회 실패:', error)
    ElMessage.error('거래 내역 조회 중 오류가 발생했습니다.')
  }
}

const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([loadStatistics(), loadRecentTransactions()])
  } finally {
    loading.value = false
  }
}

const formatCurrency = (amount: number | string): string => {
  const num = typeof amount === 'string' ? parseFloat(amount) : amount
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW'
  }).format(num)
}

const getIcon = (iconName?: string) => {
  // 아이콘 이름을 컴포넌트로 변환 (간단한 예시)
  // 실제로는 더 많은 아이콘 매핑이 필요할 수 있습니다
  return null
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.account-book-page {
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 200px);
  min-height: calc(100vh - 60px);
}

.month-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  padding: 24px 0;
}

.month-display {
  min-width: 150px;
  text-align: center;
}

.month-text {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-card {
  border-radius: 16px;
  border: none;
  height: 140px;
  display: flex;
  align-items: center;
}

.stat-content {
  width: 100%;
}

.stat-header {
  margin-bottom: 12px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.2;
}

.income-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.income-value {
  color: white;
}

.expense-card {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.expense-value {
  color: white;
}

.balance-card {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.balance-card.negative-balance {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.balance-value {
  color: white;
}

.transactions-card {
  border-radius: 16px;
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
}

.loading-container,
.empty-container {
  padding: 40px 0;
  text-align: center;
}

.transaction-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.transaction-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.transaction-item:hover {
  background-color: #f5f7fa;
}

.transaction-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.category-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.transaction-info {
  flex: 1;
}

.transaction-category {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.transaction-description {
  font-size: 14px;
  color: #909399;
}

.transaction-amount {
  font-size: 20px;
  font-weight: 700;
}

.transaction-amount.income {
  color: #67c23a;
}

.transaction-amount.expense {
  color: #f56c6c;
}

@media (max-width: 768px) {
  .month-text {
    font-size: 20px;
  }

  .stat-value {
    font-size: 24px;
  }

  .stat-card {
    height: 120px;
    margin-bottom: 16px;
  }
}
</style>

