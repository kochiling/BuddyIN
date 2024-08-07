package com.cscorner.buddyin;

public class NotesModel {
    private String description;
    private String pdfURL;
    private String subject;
    private String file_title;

    public NotesModel() {
    }

    public NotesModel( String subject, String file_title,String description, String pdfURL) {
        this.description = description;
        this.file_title = file_title;
        this.pdfURL = pdfURL;
        this.subject = subject;
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
}
