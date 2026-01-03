package com.always.controller;

import com.always.entity.Post;
import com.always.entity.User;
import com.always.service.FileService;
import com.always.service.PostService;
import com.always.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:8088", "http://192.168.75.207:8088"}) // Vue 개발 서버 주소
public class ApiController {

    private final PostService postService;
    private final FileService fileService;
    private final UserService userService;

    @Autowired
    public ApiController(PostService postService, FileService fileService, UserService userService) {
        this.postService = postService;
        this.fileService = fileService;
        this.userService = userService;
    }

    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from Spring Boot!");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts")
    public ResponseEntity<Map<String, Object>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 페이지네이션 설정 (최신순 정렬)
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            
            Page<Post> postPage;
            if (search != null && !search.trim().isEmpty()) {
                postPage = postService.searchPosts(search.trim(), pageable);
            } else {
                postPage = postService.getAllPosts(pageable);
            }
            
            response.put("posts", postPage.getContent());
            response.put("count", postPage.getTotalElements());
            response.put("totalPages", postPage.getTotalPages());
            response.put("currentPage", postPage.getNumber());
            response.put("pageSize", postPage.getSize());
            response.put("hasNext", postPage.hasNext());
            response.put("hasPrevious", postPage.hasPrevious());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "포스트 조회 중 오류가 발생했습니다: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            e.printStackTrace(); // 로그 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<Map<String, Object>> getPostById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        return postService.getPostById(id)
                .map(post -> {
                    response.put("post", post);
                    if (post.getImagePath() != null && !post.getImagePath().isEmpty()) {
                        response.put("imageUrl", "http://192.168.75.207:8089/images/" + post.getImagePath());
                    }
                    response.put("timestamp", LocalDateTime.now());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Post not found with id: " + id,
                                "timestamp", LocalDateTime.now())));
    }

    @GetMapping("/posts/author/{authorId}")
    public ResponseEntity<Map<String, Object>> getPostsByAuthor(
            @PathVariable Long authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<User> userOpt = userService.findById(authorId);
            if (userOpt.isEmpty()) {
                response.put("error", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Post> postPage = postService.getPostsByAuthor(userOpt.get(), pageable);
            
            response.put("posts", postPage.getContent());
            response.put("count", postPage.getTotalElements());
            response.put("totalPages", postPage.getTotalPages());
            response.put("currentPage", postPage.getNumber());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "포스트 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/posts")
    public ResponseEntity<Map<String, Object>> createPost(
            @RequestBody Map<String, Object> postData,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // JWT에서 사용자 ID 가져오기
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                response.put("error", "인증 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 사용자 조회
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isEmpty()) {
                response.put("error", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Post post = new Post();
            post.setTitle((String) postData.get("title"));
            post.setContent((String) postData.get("content"));
            post.setAuthor(userOpt.get());
            
            Post createdPost = postService.createPost(post);
            
            response.put("message", "포스트가 생성되었습니다.");
            response.put("post", createdPost);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("error", "포스트 생성 중 오류가 발생했습니다: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<Map<String, Object>> updatePost(
            @PathVariable Long id,
            @RequestBody Map<String, Object> postData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Post post = new Post();
            post.setTitle((String) postData.get("title"));
            post.setContent((String) postData.get("content"));
            
            Post updatedPost = postService.updatePost(id, post);
            
            response.put("message", "포스트가 업데이트되었습니다.");
            response.put("post", updatedPost);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Map<String, Object>> deletePost(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 포스트에 연결된 이미지가 있으면 삭제
            postService.getPostById(id).ifPresent(post -> {
                if (post.getImagePath() != null && !post.getImagePath().isEmpty()) {
                    try {
                        fileService.deleteImage(post.getImagePath());
                    } catch (IOException e) {
                        // 이미지 삭제 실패해도 포스트는 삭제
                    }
                }
            });
            
            postService.deletePost(id);
            response.put("message", "포스트가 삭제되었습니다.");
            response.put("id", id);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // 이미지 업로드
    @PostMapping("/posts/{id}/image")
    public ResponseEntity<Map<String, Object>> uploadPostImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 이미지 파일 저장
            String savedFilename = fileService.saveImage(file);
            
            // 포스트에 이미지 경로 저장
            Post post = postService.getPostById(id)
                    .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
            
            // 기존 이미지가 있으면 삭제
            if (post.getImagePath() != null && !post.getImagePath().isEmpty()) {
                try {
                    fileService.deleteImage(post.getImagePath());
                } catch (IOException e) {
                    // 삭제 실패해도 계속 진행
                }
            }
            
            post.setImagePath(savedFilename);
            Post updatedPost = postService.updatePost(id, post);
            
            response.put("message", "이미지가 업로드되었습니다.");
            response.put("post", updatedPost);
            response.put("imageUrl", "/images/" + savedFilename);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("error", "이미지 업로드 실패: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // 이미지 다운로드 (정적 리소스로 제공하므로 이 엔드포인트는 선택사항)
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path filePath = fileService.getImagePath(filename);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                // Content-Type 결정
                String contentType = "application/octet-stream";
                try {
                    contentType = Files.probeContentType(filePath);
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }
                } catch (IOException e) {
                    // 기본값 사용
                }
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}




