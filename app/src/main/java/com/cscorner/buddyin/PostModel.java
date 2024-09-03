package com.cscorner.buddyin;

public class PostModel {
    private String description;
    private String post_image;
    private String subject;
    private String user_id;
    private String username;
    private String post_id;
    private String timestamp;
    private String key;

    // Default constructor required for calls to DataSnapshot.getValue(PostModel.class)
    public PostModel() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPost_image() {
        return post_image;
    }

    public void setPost_image(String post_image) {
        this.post_image = post_image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PostModel(String description, String post_image, String subject, String user_id, String username, String post_id, String timestamp) {
        this.description = description;
        this.post_image = post_image;
        this.subject = subject;
        this.user_id = user_id;
        this.username = username;
        this.post_id = post_id;
        this.timestamp = timestamp;
    }
}