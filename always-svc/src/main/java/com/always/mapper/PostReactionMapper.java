package com.always.mapper;

import com.always.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostReactionMapper {
    int countByPostIdAndType(@Param("postId") Long postId, @Param("type") String type);
    boolean exists(@Param("postId") Long postId, @Param("userId") Long userId, @Param("type") String type);
    void insert(@Param("postId") Long postId, @Param("userId") Long userId, @Param("type") String type);
    int delete(@Param("postId") Long postId, @Param("userId") Long userId, @Param("type") String type);
    List<Post> findBookmarkedPostsByUserId(@Param("userId") Long userId);
}
