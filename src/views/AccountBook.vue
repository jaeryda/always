<template>
  <div class="page-container account-book-page">
    <el-container>
      <el-main class="page-main">
        <div class="month-selector">
          <el-button :icon="ArrowLeft" circle @click="changeMonth(-1)" :disabled="loading" />
          <div class="month-display"><span class="month-text">{{ currentYear }}년 {{ currentMonth }}월</span></div>
          <el-button :icon="ArrowRight" circle @click="changeMonth(1)" :disabled="loading" />
        </div>

        <el-row :gutter="24" style="margin-top: 24px;">
          <el-col :span="24" :md="8"><el-card class="stat-card" shadow="hover"><div class="stat-value">수입 {{ formatCurrency(statistics.income) }}</div></el-card></el-col>
          <el-col :span="24" :md="8"><el-card class="stat-card" shadow="hover"><div class="stat-value">지출 {{ formatCurrency(statistics.expense) }}</div></el-card></el-col>
          <el-col :span="24" :md="8"><el-card class="stat-card" shadow="hover"><div class="stat-value">잔액 {{ formatCurrency(statistics.balance) }}</div></el-card></el-col>
        </el-row>

        <el-card class="transactions-card" shadow="hover" style="margin-top: 24px;">
          <template #header>
            <div class="card-header">
              <span>카테고리별 예산</span>
              <el-button type="primary" @click="budgetDialogVisible = true">예산 설정</el-button>
            </div>
          </template>

          <el-empty v-if="budgetRows.length === 0" description="이번 달 지출 데이터가 없습니다." />

          <el-table v-else :data="budgetRows" stripe>
            <el-table-column prop="categoryName" label="카테고리" min-width="160" />
            <el-table-column label="예산" width="180">
              <template #default="{ row }">{{ formatCurrency(row.budget) }}</template>
            </el-table-column>
            <el-table-column label="지출" width="180">
              <template #default="{ row }">{{ formatCurrency(row.spent) }}</template>
            </el-table-column>
            <el-table-column label="진행률" min-width="220">
              <template #default="{ row }">
                <el-progress :percentage="row.percent" :status="row.percent > 100 ? 'exception' : 'success'" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card class="transactions-card" shadow="hover" style="margin-top: 24px;">
          <template #header>
            <div class="card-header">
              <span>최근 거래 내역</span>
              <el-button type="primary" @click="$router.push('/transactions')">전체보기</el-button>
            </div>
          </template>

          <el-skeleton v-if="loading" :rows="5" animated />
          <el-empty v-else-if="recentTransactions.length === 0" description="거래 내역이 없습니다" />
          <div v-else class="transaction-list">
            <div v-for="transaction in recentTransactions" :key="transaction.id" class="transaction-item" @click="$router.push(`/transactions?edit=${transaction.id}`)">
              <div>{{ transaction.category?.name || '미분류' }} - {{ transaction.description || '-' }}</div>
              <strong :style="{ color: transaction.type === 'INCOME' ? '#67c23a' : '#f56c6c' }">
                {{ transaction.type === 'INCOME' ? '+' : '-' }}{{ formatCurrency(transaction.amount) }}
              </strong>
            </div>
          </div>
        </el-card>

        <el-dialog v-model="budgetDialogVisible" title="월 예산 설정" width="520px">
          <el-form label-position="top">
            <el-form-item label="카테고리">
              <el-select v-model="budgetForm.categoryId" style="width:100%">
                <el-option v-for="item in expenseCategories" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="예산 금액 (원)">
              <el-input-number v-model="budgetForm.amount" :min="0" :step="10000" style="width:100%" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="budgetDialogVisible = false">취소</el-button>
            <el-button type="primary" @click="saveBudget">저장</el-button>
          </template>
        </el-dialog>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { accountBookApi, type Transaction, type StatisticsResponse, type Category } from '@/api/accountBook'
import { ElMessage } from 'element-plus'
import { useBudgetStore } from '@/store/budget'
import { useNotificationStore } from '@/store/notifications'

const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1)
const loading = ref(false)
const statistics = ref<StatisticsResponse>({ success: true, year: currentYear.value, month: currentMonth.value, income: 0, expense: 0, balance: 0 })
const recentTransactions = ref<Transaction[]>([])
const allTransactions = ref<Transaction[]>([])
const categories = ref<Category[]>([])

const budgetStore = useBudgetStore()
const notificationStore = useNotificationStore()

const budgetDialogVisible = ref(false)
const budgetForm = ref<{ categoryId: number | null; amount: number }>({ categoryId: null, amount: 0 })

const monthKey = computed(() => `${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}`)
const expenseCategories = computed(() => categories.value.filter((item) => item.type === 'EXPENSE'))

const budgetRows = computed(() => {
  const expenseMap = new Map<number, { categoryName: string; spent: number }>()
  allTransactions.value
    .filter((item) => item.type === 'EXPENSE')
    .forEach((item) => {
      const categoryId = item.categoryId || item.category?.id
      if (!categoryId) return
      const prev = expenseMap.get(categoryId)
      const amount = Number(item.amount || 0)
      if (prev) {
        prev.spent += amount
      } else {
        expenseMap.set(categoryId, {
          categoryName: item.category?.name || `카테고리 ${categoryId}`,
          spent: amount
        })
      }
    })

  return Array.from(expenseMap.entries()).map(([categoryId, data]) => {
    const budget = budgetStore.getBudget(currentYear.value, currentMonth.value, categoryId)
    const percent = budget > 0 ? Math.round((data.spent / budget) * 100) : 0
    return {
      categoryId,
      categoryName: data.categoryName,
      spent: data.spent,
      budget,
      percent
    }
  })
})

const changeMonth = (delta: number) => {
  const date = new Date(currentYear.value, currentMonth.value - 1 + delta, 1)
  currentYear.value = date.getFullYear()
  currentMonth.value = date.getMonth() + 1
  loadData()
}

const loadStatistics = async () => {
  const response = await accountBookApi.getStatistics(currentYear.value, currentMonth.value)
  if (response.data.success) statistics.value = response.data
}

const loadCategories = async () => {
  const response = await accountBookApi.getAllCategories()
  if (response.data.success && response.data.categories) {
    categories.value = response.data.categories
  }
}

const loadRecentTransactions = async () => {
  const response = await accountBookApi.getAllTransactions(currentYear.value, currentMonth.value)
  if (response.data.success && response.data.transactions) {
    allTransactions.value = response.data.transactions
    recentTransactions.value = response.data.transactions.slice(0, 10)
  } else {
    allTransactions.value = []
    recentTransactions.value = []
  }
}

const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([loadStatistics(), loadRecentTransactions(), loadCategories()])
    await budgetStore.load(currentYear.value, currentMonth.value)
  } catch (error) {
    console.error(error)
    ElMessage.error('가계부 데이터를 불러오지 못했습니다.')
  } finally {
    loading.value = false
  }
}

const saveBudget = async () => {
  if (!budgetForm.value.categoryId) {
    ElMessage.warning('카테고리를 선택해 주세요.')
    return
  }
  await budgetStore.setBudget(
    currentYear.value,
    currentMonth.value,
    budgetForm.value.categoryId,
    Number(budgetForm.value.amount || 0)
  )
  await notificationStore.pushNotification('예산 설정', `${monthKey.value} 예산이 저장되었습니다.`)
  budgetDialogVisible.value = false
  ElMessage.success('예산을 저장했습니다.')
}

const formatCurrency = (amount: number | string): string => {
  const num = typeof amount === 'string' ? parseFloat(amount) : amount
  return new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW' }).format(num)
}

onMounted(loadData)
</script>

<style scoped>
.month-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  padding: 24px 0;
}
.month-text {
  font-size: 24px;
  font-weight: 600;
}
.stat-card {
  height: 120px;
  display: flex;
  align-items: center;
}
.stat-value {
  font-size: 22px;
  font-weight: 700;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.transaction-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
  cursor: pointer;
}
</style>
