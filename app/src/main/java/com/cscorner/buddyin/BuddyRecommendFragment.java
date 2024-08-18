package com.cscorner.buddyin;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BuddyRecommendFragment extends Fragment {

     RecyclerView recyclerView;
     BuddyRecommendAdapter adapter;
     DatabaseReference matchResultsRef, usersRef, knnDataInfoRef;
     List<UserModel> UserList;
     TextView noDataText;

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
                    adapter.notifyDataSetChanged();
                    noDataText.setVisibility(UserList.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Log.d(TAG, "UserModel is null for buddyId: " + buddyId);
                }


            }


//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                UserModel userModel = snapshot.getValue(UserModel.class);
//                if (userModel != null) {
//                    UserList.add(userModel);
//                    adapter.notifyDataSetChanged();
//                    noDataText.setVisibility(UserList.isEmpty() ? View.VISIBLE : View.GONE);
//                }
//            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load buddy info.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}