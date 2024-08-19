package com.cscorner.buddyin;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import com.google.gson.JsonObject;

public interface BuddyMatchApi {
    @POST("/user")
    Call<ResponseBody> senduser(@Body JsonObject userIdJson);

    @POST("/get_buddies")
    Call<ResponseBody> getBuddies(@Body JsonObject userIdJson);
}
