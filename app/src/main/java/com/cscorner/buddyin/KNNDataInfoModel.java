package com.cscorner.buddyin;

public class KNNDataInfoModel {
    private String userid;
    private String name;
    private String course;
    private Integer seniority;
    private String hobbies;
    private String personalities;
    private String key;


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public KNNDataInfoModel() {
    }


    public String getUserid() {
        return userid;
    }

    public String getPersonalities() {
        return personalities;
    }

    public String getHobbies() {
        return hobbies;
    }

    public String getCourse() {
        return course;
    }

    public String getName() {
        return name;
    }

    public Integer getSeniority() {
        return seniority;
    }

    public KNNDataInfoModel(String userid, String name, String course, Integer seniority, String hobbies, String personalities) {
        this.userid = userid;
        this.name = name;
        this.course = course;
        this.seniority = seniority;
        this.hobbies = hobbies;
        this.personalities = personalities;
    }
}
