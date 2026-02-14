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
}

export const socialApi = {
  getPostSocial(postId: number) {
    return api.get<PostSocialSummary>(`/social/posts/${postId}`)
  },
  toggleLike(postId: number) {
    return api.post<{ success: boolean; liked: boolean; likeCount: number }>(`/social/posts/${postId}/likes/toggle`)
  },
  toggleBookmark(postId: number) {
    return api.post<{ success: boolean; bookmarked: boolean; bookmarkCount: number }>(`/social/posts/${postId}/bookmarks/toggle`)
  },
  getComments(postId: number) {
    return api.get<SocialComment[]>(`/social/posts/${postId}/comments`)
  },
  addComment(postId: number, content: string) {
    return api.post<{ success: boolean; comment: SocialComment }>(`/social/posts/${postId}/comments`, { content })
  },
  deleteComment(commentId: number) {
    return api.delete<{ success: boolean }>(`/social/comments/${commentId}`)
  },
  getBookmarks() {
    return api.get<{ success: boolean; posts: any[] }>('/social/bookmarks')
  }
}
