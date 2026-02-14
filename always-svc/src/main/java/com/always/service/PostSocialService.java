package com.always.service;

import com.always.entity.PostComment;
import com.always.entity.Post;
import com.always.mapper.PostCommentMapper;
import com.always.mapper.PostReactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PostSocialService {

    private final PostCommentMapper postCommentMapper;
    private final PostReactionMapper postReactionMapper;

    @Autowired
    public PostSocialService(PostCommentMapper postCommentMapper, PostReactionMapper postReactionMapper) {
        this.postCommentMapper = postCommentMapper;
        this.postReactionMapper = postReactionMapper;
    }

    public List<PostComment> getComments(Long postId) {
        return postCommentMapper.findByPostId(postId);
    }

    public PostComment addComment(Long postId, Long userId, String content) {
        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        postCommentMapper.insert(comment);
        return comment;
    }

    public boolean deleteComment(Long commentId, Long userId) {
        return postCommentMapper.deleteByIdAndUserId(commentId, userId) > 0;
    }

    public int getLikeCount(Long postId) {
        return postReactionMapper.countByPostIdAndType(postId, "LIKE");
    }

    public int getBookmarkCount(Long postId) {
        return postReactionMapper.countByPostIdAndType(postId, "BOOKMARK");
    }

    public boolean isLiked(Long postId, Long userId) {
        return postReactionMapper.exists(postId, userId, "LIKE");
    }

    public boolean isBookmarked(Long postId, Long userId) {
        return postReactionMapper.exists(postId, userId, "BOOKMARK");
    }

    public boolean toggleLike(Long postId, Long userId) {
        if (isLiked(postId, userId)) {
            postReactionMapper.delete(postId, userId, "LIKE");
            return false;
        }
        postReactionMapper.insert(postId, userId, "LIKE");
        return true;
    }

    public boolean toggleBookmark(Long postId, Long userId) {
        if (isBookmarked(postId, userId)) {
            postReactionMapper.delete(postId, userId, "BOOKMARK");
            return false;
        }
        postReactionMapper.insert(postId, userId, "BOOKMARK");
        return true;
    }

    public List<Post> getBookmarkedPosts(Long userId) {
        return postReactionMapper.findBookmarkedPostsByUserId(userId);
    }
}
