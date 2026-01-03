package com.always.service;

import com.always.entity.Post;
import com.always.entity.User;
import com.always.mapper.PostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PostService {

    private final PostMapper postMapper;

    @Autowired
    public PostService(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    public List<Post> getAllPosts() {
        return postMapper.findAll();
    }

    public Page<Post> getAllPosts(Pageable pageable) {
        int offset = (int) pageable.getOffset();
        int limit = pageable.getPageSize();
        
        List<Post> content = postMapper.findAllWithPagination(offset, limit);
        int total = postMapper.count();
        
        return new PageImpl<>(content, pageable, total);
    }

    public Page<Post> searchPosts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPosts(pageable);
        }
        
        int offset = (int) pageable.getOffset();
        int limit = pageable.getPageSize();
        
        List<Post> content = postMapper.findByTitleContainingOrContentContaining(keyword, offset, limit);
        int total = postMapper.countByTitleContainingOrContentContaining(keyword);
        
        return new PageImpl<>(content, pageable, total);
    }

    public Optional<Post> getPostById(Long id) {
        Post post = postMapper.findById(id);
        return Optional.ofNullable(post);
    }

    public Post createPost(Post post) {
        // createdAt, updatedAt 설정
        if (post.getCreatedAt() == null) {
            post.setCreatedAt(LocalDateTime.now());
        }
        if (post.getUpdatedAt() == null) {
            post.setUpdatedAt(LocalDateTime.now());
        }
        
        postMapper.insert(post);
        return post;
    }

    public Post updatePost(Long id, Post postDetails) {
        Post post = postMapper.findById(id);
        if (post == null) {
            throw new RuntimeException("Post not found with id: " + id);
        }
        
        post.setTitle(postDetails.getTitle());
        post.setContent(postDetails.getContent());
        post.setUpdatedAt(LocalDateTime.now());
        
        // 이미지 경로도 업데이트
        if (postDetails.getImagePath() != null) {
            post.setImagePath(postDetails.getImagePath());
        }
        
        postMapper.update(post);
        return post;
    }

    public void deletePost(Long id) {
        if (!postMapper.existsById(id)) {
            throw new RuntimeException("Post not found with id: " + id);
        }
        postMapper.deleteById(id);
    }

    public Page<Post> getPostsByAuthor(User author, Pageable pageable) {
        int offset = (int) pageable.getOffset();
        int limit = pageable.getPageSize();
        
        List<Post> content = postMapper.findByAuthorId(author.getId(), offset, limit);
        int total = postMapper.countByAuthorId(author.getId());
        
        return new PageImpl<>(content, pageable, total);
    }

    public List<Post> getPostsByAuthor(User author) {
        return postMapper.findAllByAuthorId(author.getId());
    }
}
