package com.grid07.grid07.dto;

import lombok.Data;

@Data
public class CreateCommentRequest {
    private String content;
    private String authorType; // "USER" or "BOT"
    private Long authorId;
    private int depthLevel;
}