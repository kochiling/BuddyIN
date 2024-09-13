package com.cscorner.buddyin;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BuddyChatFragment extends Fragment {


    BuddyChatAdapter adapter;
    DatabaseReference usersRef;
    List<UserModel> UserList;
    TextView noDataText;
    ImageButton chatbot;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buddy_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.chatbot_toolbar); //Ignore red line errors
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Chat");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        noDataText = view.findViewById(R.id.no_data_text);
        noDataText.setVisibility(View.VISIBLE );
        chatbot = view.findViewById(R.id.chatbot);

        chatbot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ChatbotActivity.class);
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.buddy_chat_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        UserList = new ArrayList<>();
        adapter = new BuddyChatAdapter(getContext(),UserList);
        recyclerView.setAdapter(adapter);
        fetchFriends();
    }

    private void fetchFriends() {
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference matchedBuddiesRef = FirebaseDatabase.getInstance().getReference("Buddies").child("friend").child(currentUserId).child("list");

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
                adapter.notifyDataSetChanged();
                noDataText.setVisibility(UserList.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load buddy info.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}