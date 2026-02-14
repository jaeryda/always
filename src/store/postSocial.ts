import { defineStore } from 'pinia'
import { ref } from 'vue'
import { socialApi, SocialComment } from '@/api/social'

export const usePostSocialStore = defineStore('postSocial', () => {
  const likeCount = ref<number>(0)
  const bookmarkCount = ref<number>(0)
  const liked = ref<boolean>(false)
  const bookmarked = ref<boolean>(false)
  const comments = ref<SocialComment[]>([])

  const load = async (postId: number) => {
    const response = await socialApi.getPostSocial(postId)
    likeCount.value = response.data.likeCount
    bookmarkCount.value = response.data.bookmarkCount
    liked.value = response.data.liked
    bookmarked.value = response.data.bookmarked
    comments.value = response.data.comments || []
  }

  const toggleLike = async (postId: number): Promise<boolean> => {
    const response = await socialApi.toggleLike(postId)
    liked.value = response.data.liked
    likeCount.value = response.data.likeCount
    return liked.value
  }

  const toggleBookmark = async (postId: number): Promise<boolean> => {
    const response = await socialApi.toggleBookmark(postId)
    bookmarked.value = response.data.bookmarked
    bookmarkCount.value = response.data.bookmarkCount
    return bookmarked.value
  }

  const addComment = async (postId: number, content: string): Promise<void> => {
    await socialApi.addComment(postId, content)
    await load(postId)
  }

  const removeComment = async (postId: number, commentId: number): Promise<boolean> => {
    const response = await socialApi.deleteComment(commentId)
    if (response.data.success) {
      await load(postId)
      return true
    }
    return false
  }

  return {
    likeCount,
    bookmarkCount,
    liked,
    bookmarked,
    comments,
    load,
    toggleLike,
    toggleBookmark,
    addComment,
    removeComment
  }
})

