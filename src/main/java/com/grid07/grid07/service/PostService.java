package com.grid07.grid07.service;

import com.grid07.grid07.dto.CreateCommentRequest;
import com.grid07.grid07.dto.CreatePostRequest;
import com.grid07.grid07.entity.Comment;
import com.grid07.grid07.entity.Post;
import com.grid07.grid07.repository.CommentRepository;
import com.grid07.grid07.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private RedisService redisService;

    public Post createPost(CreatePostRequest request) {
        Post post = new Post();
        post.setContent(request.getContent());
        post.setAuthorType(request.getAuthorType());
        post.setAuthorId(request.getAuthorId());
        return postRepository.save(post);
    }

    public Comment addComment(Long postId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if ("BOT".equalsIgnoreCase(request.getAuthorType())) {

            if (request.getDepthLevel() > 20) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Vertical cap exceeded: max depth is 20");
            }

            boolean allowed = redisService.incrementBotCount(postId);
            if (!allowed) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Horizontal cap exceeded: max 100 bot replies per post");
            }

            if (redisService.isBotOnCooldown(request.getAuthorId(), post.getAuthorId())) {
                redisService.incrementBotCount(postId); // undo the increment
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Cooldown cap: bot must wait 10 minutes");
            }

            redisService.setBotCooldown(request.getAuthorId(), post.getAuthorId());
            redisService.incrementViralityScore(postId, 1);

            handleNotification(post.getAuthorId(), request.getAuthorId());
        } else {
            redisService.incrementViralityScore(postId, 50);
        }

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if ("USER".equalsIgnoreCase(userType)) {
            redisService.incrementViralityScore(postId, 20);
        }
    }

    private void handleNotification(Long postOwnerId, Long botId) {
        if (redisService.isNotificationOnCooldown(postOwnerId)) {
            redisService.pushPendingNotification(postOwnerId, "Bot " + botId + " replied to your post");
        } else {
            System.out.println("Push Notification Sent to User " + postOwnerId);
            redisService.setNotificationCooldown(postOwnerId);
        }
    }
}