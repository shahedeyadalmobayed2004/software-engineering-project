package com.example.recipebook;

public class CommentModel {
    private String commentId;
    private String userId;
    private String userName;
    private String text;
    private Long timestamp;

    public CommentModel() {}

    public CommentModel(String commentId, String userId, String userName, String text, Long timestamp) {
        this.commentId = commentId;
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}