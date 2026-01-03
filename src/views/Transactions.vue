<template>
  <div class="page-container transactions-page">
    <el-container>
      <el-main class="page-main">
        <el-card shadow="hover" class="transactions-main-card">
          <template #header>
            <div class="card-header">
              <div style="display: flex; align-items: center; gap: 12px;">
                <el-button 
                  :icon="ArrowLeft" 
                  circle 
                  @click="$router.push('/account-book')"
                  style="margin-right: 8px;" />
                <span>거래 내역</span>
              </div>
              <el-button type="primary" :icon="Plus" @click="openCreateDialog">
                거래 추가
              </el-button>
            </div>
          </template>

          <!-- 필터 -->
          <div class="filter-section">
            <el-space :size="16" wrap>
              <el-select v-model="selectedType" placeholder="전체" clearable style="width: 120px" @change="loadTransactions">
                <el-option label="전체" value="" />
                <el-option label="수입" value="INCOME" />
                <el-option label="지출" value="EXPENSE" />
              </el-select>
              <el-date-picker
                v-model="selectedDate"
                type="month"
                placeholder="월 선택"
                format="YYYY년 MM월"
                value-format="YYYY-MM"
                @change="handleDateChange"
                style="width: 160px" />
              <el-button :icon="Refresh" @click="loadTransactions">새로고침</el-button>
            </el-space>
          </div>

          <!-- 거래 내역 목록 -->
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="8" animated />
          </div>

          <div v-else-if="transactions.length === 0" class="empty-container">
            <el-empty description="거래 내역이 없습니다" />
          </div>

          <div v-else class="transaction-list">
            <div 
              v-for="transaction in transactions" 
              :key="transaction.id"
              class="transaction-item"
              @click="openEditDialog(transaction)">
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
                  <div class="transaction-details">
                    <span class="transaction-description">
                      {{ transaction.description || '-' }}
                    </span>
                    <span class="transaction-date">
                      {{ formatDate(transaction.transactionDate) }}
                    </span>
                  </div>
                </div>
              </div>
              <div class="transaction-right">
                <div 
                  class="transaction-amount"
                  :class="transaction.type === 'INCOME' ? 'income' : 'expense'">
                  {{ transaction.type === 'INCOME' ? '+' : '-' }}{{ formatCurrency(transaction.amount) }}
                </div>
                <el-button 
                  type="danger" 
                  :icon="Delete" 
                  circle
                  size="small"
                  @click.stop="handleDelete(transaction.id!)" />
              </div>
            </div>
          </div>
        </el-card>

        <!-- 거래 추가/수정 다이얼로그 -->
        <el-dialog
          v-model="dialogVisible"
          :title="editingTransaction ? '거래 수정' : '거래 추가'"
          width="500px"
          :close-on-click-modal="false">
          <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
            <el-form-item label="유형" prop="type">
              <el-radio-group v-model="form.type">
                <el-radio label="INCOME">수입</el-radio>
                <el-radio label="EXPENSE">지출</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="금액" prop="amount">
              <el-input-number
                v-model="form.amount"
                :min="0"
                :precision="0"
                :step="1000"
                style="width: 100%"
                placeholder="금액을 입력하세요">
                <template #append>원</template>
              </el-input-number>
            </el-form-item>

            <el-form-item label="카테고리" prop="categoryId">
              <el-select 
                v-model="form.categoryId" 
                placeholder="카테고리를 선택하세요"
                style="width: 100%"
                filterable>
                <el-option
                  v-for="category in filteredCategories"
                  :key="category.id"
                  :label="category.name"
                  :value="category.id">
                  <div style="display: flex; align-items: center; gap: 8px;">
                    <div 
                      class="category-color-dot" 
                      :style="{ backgroundColor: category.color || '#909399' }" />
                    <span>{{ category.name }}</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>

            <el-form-item label="날짜" prop="transactionDate">
              <el-date-picker
                v-model="form.transactionDate"
                type="date"
                placeholder="날짜를 선택하세요"
                value-format="YYYY-MM-DD"
                style="width: 100%" />
            </el-form-item>

            <el-form-item label="설명" prop="description">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="3"
                placeholder="설명을 입력하세요 (선택사항)" />
            </el-form-item>
          </el-form>

          <template #footer>
            <el-button @click="dialogVisible = false">취소</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">
              {{ editingTransaction ? '수정' : '추가' }}
            </el-button>
          </template>
        </el-dialog>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Plus, Refresh, Delete, ArrowLeft } from '@element-plus/icons-vue'
import { accountBookApi, type Transaction, type Category } from '@/api/accountBook'
import dayjs from 'dayjs'
import { formatDate as formatDateUtil } from '@/utils/date'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const transactions = ref<Transaction[]>([])
const categories = ref<Category[]>([])
const dialogVisible = ref(false)
const submitting = ref(false)
const editingTransaction = ref<Transaction | null>(null)
const selectedType = ref<string>('')
const selectedDate = ref<string>('')

const formRef = ref<FormInstance>()
const form = ref({
  type: 'EXPENSE' as 'INCOME' | 'EXPENSE',
  amount: 0,
  categoryId: undefined as number | undefined,
  transactionDate: dayjs().format('YYYY-MM-DD'),
  description: ''
})

const rules: FormRules = {
  type: [{ required: true, message: '유형을 선택하세요', trigger: 'change' }],
  amount: [{ required: true, message: '금액을 입력하세요', trigger: 'blur' }],
  transactionDate: [{ required: true, message: '날짜를 선택하세요', trigger: 'change' }]
}

const filteredCategories = computed(() => {
  return categories.value.filter(cat => cat.type === form.value.type)
})

const loadCategories = async () => {
  try {
    const response = await accountBookApi.getAllCategories()
    if (response.data.success && response.data.categories) {
      categories.value = response.data.categories
    }
  } catch (error: any) {
    console.error('카테고리 조회 실패:', error)
    ElMessage.error('카테고리 조회 중 오류가 발생했습니다.')
  }
}

const loadTransactions = async () => {
  loading.value = true
  try {
    let year: number | undefined
    let month: number | undefined

    if (selectedDate.value) {
      const [y, m] = selectedDate.value.split('-').map(Number)
      year = y
      month = m
    } else {
      const now = new Date()
      year = now.getFullYear()
      month = now.getMonth() + 1
    }

    const response = await accountBookApi.getAllTransactions(year, month)
    if (response.data.success && response.data.transactions) {
      let filtered = response.data.transactions
      
      if (selectedType.value) {
        filtered = filtered.filter(t => t.type === selectedType.value)
      }
      
      transactions.value = filtered
    }
  } catch (error: any) {
    console.error('거래 내역 조회 실패:', error)
    ElMessage.error('거래 내역 조회 중 오류가 발생했습니다.')
  } finally {
    loading.value = false
  }
}

const handleDateChange = () => {
  loadTransactions()
}

const openCreateDialog = () => {
  editingTransaction.value = null
  form.value = {
    type: 'EXPENSE',
    amount: 0,
    categoryId: undefined,
    transactionDate: dayjs().format('YYYY-MM-DD'),
    description: ''
  }
  dialogVisible.value = true
}

const openEditDialog = (transaction: Transaction) => {
  editingTransaction.value = transaction
  form.value = {
    type: transaction.type,
    amount: Number(transaction.amount),
    categoryId: transaction.categoryId,
    transactionDate: transaction.transactionDate,
    description: transaction.description || ''
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (editingTransaction.value) {
        // 수정
        await accountBookApi.updateTransaction(editingTransaction.value.id!, form.value)
        ElMessage.success('거래 내역이 수정되었습니다.')
      } else {
        // 추가
        await accountBookApi.createTransaction(form.value)
        ElMessage.success('거래 내역이 추가되었습니다.')
      }
      
      dialogVisible.value = false
      await loadTransactions()
    } catch (error: any) {
      console.error('거래 내역 저장 실패:', error)
      ElMessage.error(error.response?.data?.message || '거래 내역 저장 중 오류가 발생했습니다.')
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('이 거래 내역을 삭제하시겠습니까?', '삭제 확인', {
      confirmButtonText: '삭제',
      cancelButtonText: '취소',
      type: 'warning'
    })

    await accountBookApi.deleteTransaction(id)
    ElMessage.success('거래 내역이 삭제되었습니다.')
    await loadTransactions()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('거래 내역 삭제 실패:', error)
      ElMessage.error('거래 내역 삭제 중 오류가 발생했습니다.')
    }
  }
}

const formatCurrency = (amount: number | string): string => {
  const num = typeof amount === 'string' ? parseFloat(amount) : amount
  return new Intl.NumberFormat('ko-KR').format(num)
}

const formatDate = (date: string): string => {
  return dayjs(date).format('YYYY년 MM월 DD일')
}

const getIcon = (iconName?: string) => {
  return null
}

// URL 쿼리에서 edit 파라미터 확인
watch(() => route.query.edit, async (editId) => {
  if (editId && transactions.value.length > 0) {
    const transaction = transactions.value.find(t => t.id === Number(editId))
    if (transaction) {
      openEditDialog(transaction)
      // URL에서 쿼리 파라미터 제거
      router.replace({ query: {} })
    }
  }
}, { immediate: true })

onMounted(async () => {
  await loadCategories()
  await loadTransactions()
})
</script>

<style scoped>
.transactions-page {
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.transactions-main-card {
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

.filter-section {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
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
  background: white;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.2s;
}

.transaction-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
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
  flex-shrink: 0;
}

.transaction-info {
  flex: 1;
  min-width: 0;
}

.transaction-category {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.transaction-details {
  display: flex;
  gap: 12px;
  align-items: center;
  font-size: 14px;
  color: #909399;
}

.transaction-description {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.transaction-date {
  flex-shrink: 0;
}

.transaction-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.transaction-amount {
  font-size: 20px;
  font-weight: 700;
  min-width: 120px;
  text-align: right;
}

.transaction-amount.income {
  color: #67c23a;
}

.transaction-amount.expense {
  color: #f56c6c;
}

.category-color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
}

@media (max-width: 768px) {
  .transaction-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .transaction-right {
    width: 100%;
    justify-content: space-between;
  }

  .transaction-amount {
    font-size: 18px;
  }

  .transaction-details {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>

