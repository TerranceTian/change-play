package com.h5game.threebody.model;

public class ChatMessage {
    private String username;
    private String content;
    private boolean isSystem;
    private String timestamp;

    public ChatMessage() {
        this.timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public ChatMessage(String username, String content, boolean isSystem) {
        this();
        this.username = username;
        this.content = content;
        this.isSystem = isSystem;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
