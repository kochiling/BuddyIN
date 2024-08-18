package com.cscorner.buddyin;

public class ChatModel {
    String message;
    String receiver;
    String sender;
    String type;
    String timestamp;
    boolean delivered;
    boolean readed;

    public ChatModel() {
    }

    public ChatModel(String message, boolean readed, String type, String sender, String receiver, String timestamp, boolean delivered) {
        this.message = message;
        this.readed = readed;
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
        this.delivered = delivered;
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

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public boolean isReaded() {
        return readed;
    }

    public void setReaded(boolean readed) {
        this.readed = readed;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }
}
