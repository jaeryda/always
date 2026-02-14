package com.always.mapper;

import com.always.entity.PostComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostCommentMapper {
    List<PostComment> findByPostId(@Param("postId") Long postId);
    void insert(PostComment comment);
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}

