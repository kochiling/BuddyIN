package com.cscorner.buddyin;

public class PostModel {
    private String description;
    private String post_image;
    private String subject;
    private String user_id;
    private String username;
    private String key;

    // Default constructor required for calls to DataSnapshot.getValue(PostModel.class)
    public PostModel() {
    }

    // Getters and Setters
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPost_image() {
        return post_image;
    }

    public void setPost_image(String post_image) {
        this.post_image = post_image;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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

    public PostModel(String user_id, String username, String subject, String description, String post_image) {
        this.description = description;
        this.username = username;
        this.user_id = user_id;
        this.subject = subject;
        this.post_image = post_image;
    }
}