package com.cscorner.buddyin;

public class CommentModel {

    private String user_id;
    private String username;
    private String user_profile;
    private String timestamp;
    private String description;
    private String comment_id;
    private String key;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUser_profile() {
        return user_profile;
    }

    public void setUser_profile(String user_profile) {
        this.user_profile = user_profile;
    }

    public String getComment_id() {
        return comment_id;
    }

    public void setComment_id(String comment_id) {
        this.comment_id = comment_id;
    }

    public CommentModel(String user_id, String username, String user_profile, String timestamp, String description, String comment_id) {
        this.user_id = user_id;
        this.username = username;
        this.user_profile = user_profile;
        this.timestamp = timestamp;
        this.description = description;
        this.comment_id = comment_id;
    }

    public CommentModel() {
    }
}
