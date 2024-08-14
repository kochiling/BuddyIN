package com.cscorner.buddyin;

public class UserModel {
    private String username;
    private String gender;
    private String age;
    private String nationality;
    private String profile_image;
    private String key;
    private String user_id;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public UserModel() {
    }

    public String getProfile_image() {
        return profile_image;
    }

    public String getAge() {
        return age;
    }

    public String getUsername() {
        return username;
    }

    public String getGender() {
        return gender;
    }

    public String getNationality() {
        return nationality;
    }

//    public UserModel(String username, String age, String gender, String nationality, String profile_image) {
//        this.username = username;
//        this.age = age;
//        this.gender = gender;
//        this.profile_image = profile_image;
//        this.nationality = nationality;
//    }

    public UserModel(String username,String age, String gender, String nationality, String profile_image ,String user_id) {
        this.username = username;
        this.age = age;
        this.gender = gender;
        this.profile_image = profile_image;
        this.nationality = nationality;
        this.user_id = user_id;

    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
