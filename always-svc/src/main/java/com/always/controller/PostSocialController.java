package com.always.controller;

import com.always.entity.PostComment;
import com.always.entity.Post;
import com.always.service.PostSocialService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/social")
@CrossOrigin(origins = {"http://localhost:8088", "http://192.168.0.2:8088"})
public class PostSocialController {

    private final PostSocialService postSocialService;

    @Autowired
    public PostSocialController(PostSocialService postSocialService) {
        this.postSocialService = postSocialService;
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<?> getPostSocial(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> response = new HashMap<>();
        response.put("likeCount", postSocialService.getLikeCount(postId));
        response.put("bookmarkCount", postSocialService.getBookmarkCount(postId));
        response.put("liked", userId != null && postSocialService.isLiked(postId, userId));
        response.put("bookmarked", userId != null && postSocialService.isBookmarked(postId, userId));
        response.put("comments", postSocialService.getComments(postId, page, size));
        response.put("commentsTotal", postSocialService.countComments(postId));
        response.put("commentsPage", page);
        response.put("commentsSize", size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/posts/{postId}/likes/toggle")
    public ResponseEntity<?> toggleLike(@PathVariable Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false));
        boolean liked = postSocialService.toggleLike(postId, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "liked", liked,
                "likeCount", postSocialService.getLikeCount(postId)
        ));
    }

    @PostMapping("/posts/{postId}/bookmarks/toggle")
    public ResponseEntity<?> toggleBookmark(@PathVariable Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false));
        boolean bookmarked = postSocialService.toggleBookmark(postId, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "bookmarked", bookmarked,
                "bookmarkCount", postSocialService.getBookmarkCount(postId)
        ));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<?> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "comments", postSocialService.getComments(postId, page, size),
                "total", postSocialService.countComments(postId),
                "page", page,
                "size", size
        ));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long postId, @RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false));
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "content is required"));
        }
        PostComment comment = postSocialService.addComment(postId, userId, content.trim());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "comment", comment));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false));
        boolean deleted = postSocialService.deleteComment(commentId, userId);
        return ResponseEntity.ok(Map.of("success", deleted));
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<?> getBookmarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false));
        List<Post> posts = postSocialService.getBookmarkedPosts(userId, page, size);
        int total = postSocialService.countBookmarkedPosts(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "posts", posts,
                "total", total,
                "page", page,
                "size", size
        ));
    }
}
