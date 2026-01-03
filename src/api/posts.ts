import api from './index'

export interface Post {
  id?: number
  title: string
  content: string
  imagePath?: string | null
  createdAt?: string
  updatedAt?: string
}

export interface PostResponse {
  posts: Post[]
  count: number
  totalPages: number
  currentPage: number
  pageSize: number
  hasNext: boolean
  hasPrevious: boolean
  timestamp: string
}

export interface CreatePostData {
  title: string
  content: string
  imagePath?: string
}

export const postsApi = {
  // 모든 포스트 가져오기 (페이지네이션)
  getAllPosts(page = 0, size = 10, search?: string) {
    const params: any = { page, size }
    if (search) {
      params.search = search
    }
    return api.get<PostResponse>('/posts', { params })
  },

  // 작성자별 포스트 조회
  getPostsByAuthor(authorId: number, page = 0, size = 10) {
    const params = { page, size }
    return api.get<PostResponse>(`/posts/author/${authorId}`, { params })
  },

  // 특정 포스트 가져오기
  getPostById(id: number) {
    return api.get<{ post: Post; timestamp: string }>(`/posts/${id}`)
  },

  // 포스트 생성
  createPost(data: CreatePostData) {
    return api.post<{ message: string; post: Post; timestamp: string }>('/posts', data)
  },

  // 포스트 업데이트
  updatePost(id: number, data: CreatePostData) {
    return api.put<{ message: string; post: Post; timestamp: string }>(`/posts/${id}`, data)
  },

  // 포스트 삭제
  deletePost(id: number) {
    return api.delete<{ message: string; id: number; timestamp: string }>(`/posts/${id}`)
  },

  // 이미지 업로드
  uploadPostImage(id: number, file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post<{ message: string; post: Post; imageUrl: string; timestamp: string }>(`/posts/${id}/image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

