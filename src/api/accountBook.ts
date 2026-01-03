import api from './index'

// 카테고리 인터페이스
export interface Category {
  id?: number
  userId?: number
  name: string
  type: 'INCOME' | 'EXPENSE'
  icon?: string
  color?: string
  displayOrder?: number
  createdAt?: string
  updatedAt?: string
}

// 거래 내역 인터페이스
export interface Transaction {
  id?: number
  userId?: number
  categoryId?: number
  category?: Category
  amount: number
  type: 'INCOME' | 'EXPENSE'
  description?: string
  transactionDate: string
  createdAt?: string
  updatedAt?: string
}

// 통계 응답 인터페이스
export interface StatisticsResponse {
  success: boolean
  year: number
  month: number
  income: number
  expense: number
  balance: number
}

// 카테고리 응답 인터페이스
export interface CategoryResponse {
  success: boolean
  categories?: Category[]
  category?: Category
  message?: string
}

// 거래 내역 응답 인터페이스
export interface TransactionResponse {
  success: boolean
  transactions?: Transaction[]
  transaction?: Transaction
  message?: string
}

export const accountBookApi = {
  // ========== 카테고리 관련 API ==========
  
  // 모든 카테고리 가져오기
  getAllCategories() {
    return api.get<CategoryResponse>('/account-book/categories')
  },

  // 타입별 카테고리 가져오기
  getCategoriesByType(type: 'INCOME' | 'EXPENSE') {
    return api.get<CategoryResponse>(`/account-book/categories/${type}`)
  },

  // 카테고리 생성
  createCategory(data: Omit<Category, 'id' | 'userId' | 'createdAt' | 'updatedAt'>) {
    return api.post<CategoryResponse>('/account-book/categories', data)
  },

  // 카테고리 수정
  updateCategory(id: number, data: Partial<Category>) {
    return api.put<CategoryResponse>(`/account-book/categories/${id}`, data)
  },

  // 카테고리 삭제
  deleteCategory(id: number) {
    return api.delete<CategoryResponse>(`/account-book/categories/${id}`)
  },

  // ========== 거래 내역 관련 API ==========

  // 모든 거래 내역 가져오기
  getAllTransactions(year?: number, month?: number) {
    const params: any = {}
    if (year) params.year = year
    if (month) params.month = month
    return api.get<TransactionResponse>('/account-book/transactions', { params })
  },

  // 특정 거래 내역 가져오기
  getTransactionById(id: number) {
    return api.get<TransactionResponse>(`/account-book/transactions/${id}`)
  },

  // 거래 내역 생성
  createTransaction(data: Omit<Transaction, 'id' | 'userId' | 'createdAt' | 'updatedAt'>) {
    return api.post<TransactionResponse>('/account-book/transactions', data)
  },

  // 거래 내역 수정
  updateTransaction(id: number, data: Partial<Transaction>) {
    return api.put<TransactionResponse>(`/account-book/transactions/${id}`, data)
  },

  // 거래 내역 삭제
  deleteTransaction(id: number) {
    return api.delete<TransactionResponse>(`/account-book/transactions/${id}`)
  },

  // ========== 통계 관련 API ==========

  // 통계 가져오기
  getStatistics(year?: number, month?: number) {
    const params: any = {}
    if (year) params.year = year
    if (month) params.month = month
    return api.get<StatisticsResponse>('/account-book/statistics', { params })
  }
}

