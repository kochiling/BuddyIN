package com.cscorner.buddyin;

import java.io.Serializable;

public class SubjectClass implements Serializable {
    private String subject;


    public SubjectClass(String subject) {
        this.subject = subject;

    }
    public String getSubject() {
        return subject;
    }



}