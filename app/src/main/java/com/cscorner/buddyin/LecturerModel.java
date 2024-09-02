package com.cscorner.buddyin;

public class LecturerModel {
    private String name;
    private String id;
    private String email;
    private String faculty;
    private String profile_image;
    private String key;
    private String user_id;
    private boolean isLecturer;
    private int status;

    public LecturerModel() {
    }

    public LecturerModel(boolean isLecturer, int status, String profile_image, String user_id, String faculty, String email, String id, String name) {
        this.isLecturer = isLecturer;
        this.status = status;
        this.profile_image = profile_image;
        this.user_id = user_id;
        this.faculty = faculty;
        this.email = email;
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isLecturer() {
        return isLecturer;
    }

    public void setLecturer(boolean lecturer) {
        isLecturer = lecturer;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
