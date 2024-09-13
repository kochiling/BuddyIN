package com.cscorner.buddyin;

public class NotesModel {
    private String description;
    private String pdfURL;
    private String subject;
    private String file_title;
    private String user_id;
    private String user_name;
    private String notes_id;
    private String key;

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    public String getFile_title() {
        return file_title;
    }

    public void setFile_title(String file_title) {
        this.file_title = file_title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPdfURL() {
        return pdfURL;
    }

    public void setPdfURL(String pdfURL) {
        this.pdfURL = pdfURL;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getNotes_id() {
        return notes_id;
    }

    public void setNotes_id(String notes_id) {
        this.notes_id = notes_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public NotesModel() {
    }

    public NotesModel(String notes_id, String user_id, String user_name, String subject, String file_title, String description, String pdfURL) {
        this.notes_id = notes_id;
        this.user_id = user_id;
        this.user_name = user_name;
        this.subject = subject;
        this.file_title = file_title;
        this.description = description;
        this.pdfURL = pdfURL;
    }

}
