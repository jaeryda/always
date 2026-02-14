import { defineStore } from 'pinia'
import { ref } from 'vue'
import { accountBookApi, Budget } from '@/api/accountBook'

export const useBudgetStore = defineStore('budget', () => {
  const budgets = ref<Budget[]>([])

  const load = async (year: number, month: number) => {
    const response = await accountBookApi.getBudgets(year, month)
    budgets.value = response.data.budgets || []
  }

  const getBudget = (year: number, month: number, categoryId: number): number => {
    const value = budgets.value.find((item) => item.year === year && item.month === month && item.categoryId === categoryId)?.amount
    return value ? Number(value) : 0
  }

  const setBudget = async (year: number, month: number, categoryId: number, amount: number) => {
    await accountBookApi.upsertBudget({ year, month, categoryId, amount })
    await load(year, month)
  }

  const removeBudget = async (year: number, month: number, categoryId: number) => {
    await accountBookApi.deleteBudget(categoryId, year, month)
    await load(year, month)
  }

  return {
    budgets,
    load,
    getBudget,
    setBudget,
    removeBudget
  }
})
