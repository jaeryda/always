import api from './index'

export interface SocialComment {
  id: number
  postId: number
  userId: number
  authorUsername: string
  content: string
  createdAt: string
  updatedAt: string
}

export interface PostSocialSummary {
  likeCount: number
  bookmarkCount: number
  liked: boolean
  bookmarked: boolean
  comments: SocialComment[]
  commentsTotal: number
  commentsPage: number
  commentsSize: number
}

export const socialApi = {
  getPostSocial(postId: number, page = 0, size = 20) {
    return api.get<PostSocialSummary>(`/social/posts/${postId}`, { params: { page, size } })
  },
  toggleLike(postId: number) {
    return api.post<{ success: boolean; liked: boolean; likeCount: number }>(`/social/posts/${postId}/likes/toggle`)
  },
  toggleBookmark(postId: number) {
    return api.post<{ success: boolean; bookmarked: boolean; bookmarkCount: number }>(`/social/posts/${postId}/bookmarks/toggle`)
  },
  getComments(postId: number, page = 0, size = 20) {
    return api.get<{ success: boolean; comments: SocialComment[]; total: number; page: number; size: number }>(
      `/social/posts/${postId}/comments`,
      { params: { page, size } }
    )
  },
  addComment(postId: number, content: string) {
    return api.post<{ success: boolean; comment: SocialComment }>(`/social/posts/${postId}/comments`, { content })
  },
  deleteComment(commentId: number) {
    return api.delete<{ success: boolean }>(`/social/comments/${commentId}`)
  },
  getBookmarks(page = 0, size = 10) {
    return api.get<{ success: boolean; posts: any[]; total: number; page: number; size: number }>('/social/bookmarks', {
      params: { page, size }
    })
  }
}
