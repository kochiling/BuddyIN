package com.cscorner.buddyin;

public interface ResponseCallback {
    void onResponse(String response);
    void onError(Throwable throwable);
}
