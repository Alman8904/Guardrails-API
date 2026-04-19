package com.grid07.grid07.service;

import com.grid07.grid07.dto.CreateCommentRequest;
import com.grid07.grid07.dto.CreatePostRequest;
import com.grid07.grid07.entity.Comment;
import com.grid07.grid07.entity.Post;
import com.grid07.grid07.repository.CommentRepository;
import com.grid07.grid07.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    public Post createPost(CreatePostRequest request) {
        Post post = new Post();
        post.setContent(request.getContent());
        post.setAuthorType(request.getAuthorType());
        post.setAuthorId(request.getAuthorId());
        return postRepository.save(post);
    }

    public Comment addComment(Long postId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setContent(request.getContent());
        comment.setAuthorType(request.getAuthorType());
        comment.setAuthorId(request.getAuthorId());
        comment.setDepthLevel(request.getDepthLevel());
        return commentRepository.save(comment);
    }

    public void likePost(Long postId, Long userId, String userType) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        // virality score update will be added in Phase 2
    }
}