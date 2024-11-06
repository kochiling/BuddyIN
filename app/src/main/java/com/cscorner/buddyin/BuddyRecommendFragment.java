package com.cscorner.buddyin;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class BuddyRecommendFragment extends Fragment {

    BuddyRecommendAdapter adapter;
    DatabaseReference matchResultsRef, usersRef, knnDataInfoRef;
    List<UserModel> UserList;
    TextView noDataText;
    private BuddyMatchApi api;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buddy_recommend, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noDataText = view.findViewById(R.id.no_data_text);
        noDataText.setVisibility(View.VISIBLE );

        RecyclerView recyclerView = view.findViewById(R.id.buddy_crv);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        UserList = new ArrayList<>();
        adapter = new BuddyRecommendAdapter(getContext(),UserList);
        recyclerView.setAdapter(adapter);
        fetchMatchedBuddies();

        // Initialize Retrofit
        // Home
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://192.168.0.115:5000") // Your Flask server URL
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();

        // Initialize Retrofit
//        // INTI
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://10.3.240.238:5000") // Your Flask server URL
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
        // Hotspot
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.20.48:5000") // Your Flask server URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        http://192.168.20.48:5000

        api = retrofit.create(BuddyMatchApi.class);

        // Call the method to make the request
        sendUserIdToServer();

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swiperefresh); // Bind SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            UserList.clear(); // Clear the current list
            fetchMatchedBuddies(); // Refresh data
        });

    }

    private void sendUserIdToServer() {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create a JSON object with the user ID
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_id", currentUid);

        // Make the API call using Retrofit
        Call<ResponseBody> call = api.getBuddies(jsonObject);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = Objects.requireNonNull(response.body()).string();
                        // Handle the response
                        Toast.makeText(getActivity(), responseBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Failed to read response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Request failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                //Toast.makeText(getActivity(), "API is not online", Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(), "Request failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMatchedBuddies() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference matchedBuddiesRef = FirebaseDatabase.getInstance().getReference("Buddies").child("MatchResults").child(currentUserId);

        matchedBuddiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (Boolean.TRUE.equals(snapshot.getValue(Boolean.class))) {
                        String buddyId = snapshot.getKey();
                        fetchBuddyInfo(buddyId);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }

    private void fetchBuddyInfo(String buddyId) {
        usersRef = FirebaseDatabase.getInstance().getReference("User Info").child(buddyId);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel userModel = snapshot.getValue(UserModel.class);
                if (userModel != null) {
                    userModel.setKey(snapshot.getKey()); // Assuming you have a setKey method to store the key
                    UserList.add(userModel);

                } else {
                    Log.d(TAG, "UserModel is null for buddyId: " + buddyId);
                }

                adapter.setRecommendList(UserList);
                adapter.notifyDataSetChanged();
                noDataText.setVisibility(UserList.isEmpty() ? View.VISIBLE : View.GONE);
                // Stop the refresh animation after data is loaded
                swipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load buddy info.", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}

//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.Response;
//        OkHttpClient okHttpClient = new OkHttpClient();
//
//        String currentuid= Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
//
//        RequestBody formbody = new FormBody.Builder().add("user_id",currentuid).build();
//
//        Request request = new Request.Builder().url("http://192.168.0.115:5000/get_buddies").post(formbody).build();
//        // Inside your OkHttp callback
//        okHttpClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                Log.e("API Error", "Request failed: " + e.getMessage());
//                getActivity().runOnUiThread(() -> {
//                    Toast.makeText(getActivity(), "Error from API", Toast.LENGTH_SHORT).show();
//                });
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                assert response.body() != null;
//                String responseBody = response.body().string();
//                getActivity().runOnUiThread(() -> {
//                    Log.e("API Error", responseBody);
//                    Toast.makeText(getActivity(), responseBody, Toast.LENGTH_SHORT).show();
//                });
//            }
//        });