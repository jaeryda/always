package com.always.mapper;

import com.always.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {
    
    // 전체 조회
    List<Post> findAll();
    
    // ID로 조회
    Post findById(Long id);
    
    // 제목으로 검색
    List<Post> findByTitleContaining(String title);
    
    // 제목 또는 내용으로 검색 (페이지네이션 지원)
    List<Post> findByTitleContainingOrContentContaining(
        @Param("keyword") String keyword,
        @Param("offset") int offset,
        @Param("limit") int limit
    );
    
    // 제목 또는 내용으로 검색 개수
    int countByTitleContainingOrContentContaining(@Param("keyword") String keyword);
    
    // 전체 개수
    int count();
    
    // 페이지네이션 (offset, limit)
    List<Post> findAllWithPagination(
        @Param("offset") int offset,
        @Param("limit") int limit
    );
    
    // 작성자 ID로 조회 (페이지네이션 지원)
    List<Post> findByAuthorId(
        @Param("authorId") Long authorId,
        @Param("offset") int offset,
        @Param("limit") int limit
    );
    
    // 작성자 ID로 조회 개수
    int countByAuthorId(@Param("authorId") Long authorId);
    
    // 작성자 ID로 조회 (리스트)
    List<Post> findAllByAuthorId(@Param("authorId") Long authorId);
    
    // 저장
    void insert(Post post);
    
    // 업데이트
    void update(Post post);
    
    // 삭제
    void deleteById(Long id);
    
    // 존재 여부 확인
    boolean existsById(Long id);
}

