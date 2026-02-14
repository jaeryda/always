import { defineStore } from 'pinia'
import { ref } from 'vue'
import { socialApi, SocialComment } from '@/api/social'

export const usePostSocialStore = defineStore('postSocial', () => {
  const likeCount = ref<number>(0)
  const bookmarkCount = ref<number>(0)
  const liked = ref<boolean>(false)
  const bookmarked = ref<boolean>(false)
  const comments = ref<SocialComment[]>([])
  const commentsTotal = ref<number>(0)
  const commentsPage = ref<number>(0)
  const commentsSize = ref<number>(20)

  const load = async (postId: number, page = commentsPage.value, size = commentsSize.value) => {
    const response = await socialApi.getPostSocial(postId, page, size)
    likeCount.value = response.data.likeCount
    bookmarkCount.value = response.data.bookmarkCount
    liked.value = response.data.liked
    bookmarked.value = response.data.bookmarked
    comments.value = response.data.comments || []
    commentsTotal.value = response.data.commentsTotal || 0
    commentsPage.value = response.data.commentsPage || page
    commentsSize.value = response.data.commentsSize || size
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
    await load(postId, 0, commentsSize.value)
  }

  const removeComment = async (postId: number, commentId: number): Promise<boolean> => {
    const response = await socialApi.deleteComment(commentId)
    if (response.data.success) {
      await load(postId, commentsPage.value, commentsSize.value)
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
    commentsTotal,
    commentsPage,
    commentsSize,
    load,
    toggleLike,
    toggleBookmark,
    addComment,
    removeComment
  }
})
