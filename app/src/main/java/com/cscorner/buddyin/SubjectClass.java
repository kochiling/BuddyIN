package com.cscorner.buddyin;

import java.io.Serializable;

public class SubjectClass implements Serializable {
    private String subject;
    private String key;

    public SubjectClass() {
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public SubjectClass(String subject) {
        this.subject = subject;

    }
    public String getSubject() {
        return subject;
    }



}