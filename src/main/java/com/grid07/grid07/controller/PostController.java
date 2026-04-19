package com.grid07.grid07.controller;

import com.grid07.grid07.dto.CreateCommentRequest;
import com.grid07.grid07.dto.CreatePostRequest;
import com.grid07.grid07.entity.Comment;
import com.grid07.grid07.entity.Post;
import com.grid07.grid07.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody CreatePostRequest request) {
        return ResponseEntity.ok(postService.createPost(request));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> addComment(
            @PathVariable Long postId,
            @RequestBody CreateCommentRequest request) {
        return ResponseEntity.ok(postService.addComment(postId, request));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<String> likePost(
            @PathVariable Long postId,
            @RequestParam Long userId,
            @RequestParam String userType) {
        postService.likePost(postId, userId, userType);
        return ResponseEntity.ok("Liked successfully");
    }
}