package com.cscorner.buddyin;

public class ChatbotModel {
    String message;
    String type;
    String timestamp;

    public ChatbotModel() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ChatbotModel(String message, String type, String timestamp) {
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
    }
}
