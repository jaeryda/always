import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // state
  const name = ref<string>('사용자')
  const count = ref<number>(0)

  // getters
  const doubleCount = computed(() => count.value * 2)
  const greeting = computed(() => `안녕하세요, ${name.value}님!`)

  // actions
  function setName(newName: string): void {
    name.value = newName
  }

  function increment(): void {
    count.value++
  }

  function decrement(): void {
    count.value--
  }

  function reset(): void {
    count.value = 0
  }

  return {
    name,
    count,
    doubleCount,
    greeting,
    setName,
    increment,
    decrement,
    reset
  }
})

