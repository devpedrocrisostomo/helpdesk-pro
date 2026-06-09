package com.helpdeskpro.dto.comment;

import com.helpdeskpro.dto.user.UserResponse;
import com.helpdeskpro.model.entity.Comment;

public class CommentResponse {
    private Long id;
    private Long ticketId;
    private UserResponse author;
    private String message;
    private String createdAt;

    public static CommentResponse from(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setTicketId(comment.getTicket().getId());
        response.setAuthor(UserResponse.from(comment.getAuthor()));
        response.setMessage(comment.getMessage());
        response.setCreatedAt(comment.getCreatedAt().toString());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public UserResponse getAuthor() {
        return author;
    }

    public void setAuthor(UserResponse author) {
        this.author = author;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
