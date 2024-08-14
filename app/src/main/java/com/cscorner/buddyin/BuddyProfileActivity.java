package com.cscorner.buddyin;

import static android.content.ContentValues.TAG;
import static android.widget.Toast.LENGTH_SHORT;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class BuddyProfileActivity extends AppCompatActivity {

    ImageView profileImage;
    TextView profile_name,courseText,seniorityText,hobbiesText,personalitiesText;
    Button add_btn;
    String key = "";
    String userID = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buddy_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        profileImage = findViewById(R.id.profileImage);
        profile_name = findViewById(R.id.profile_name);
        add_btn = findViewById(R.id.addBuddybtn);
        courseText = findViewById(R.id.courseText);
        seniorityText = findViewById(R.id.semesterText);
        hobbiesText = findViewById(R.id.hobbiesText);
        personalitiesText = findViewById(R.id.personalityText);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            key = bundle.getString("key");
            Log.d(TAG, "key is" + Objects.requireNonNull(key));
            userID = bundle.getString("userID");
        }


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User Info").child(key);
        // Add a ValueEventListener to retrieve and update data
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("StringFormatInvalid")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Use the UserModel class to map the data from the database
                    UserModel userProfile = dataSnapshot.getValue(UserModel.class);
                    if (userProfile != null) {
                        // Debugging log
                        Log.d(TAG, "User profile retrieved: " + userProfile.getUsername());
                        // Load the profile image using Glide
                        if (userProfile.getProfile_image() != null && !userProfile.getProfile_image().isEmpty()) {
                            Glide.with(BuddyProfileActivity.this)
                                    .load(userProfile.getProfile_image())
                                    .into(profileImage);

                            // Update the TextView with the retrieved data
                            profile_name.setText(userProfile.getUsername());
                        }
                    } else {
                        Log.d(TAG, "User profile is null");
                    }
                } else {
                    Log.d(TAG, "Data snapshot does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error: " + databaseError.getMessage());
            }
        });

        DatabaseReference knnReference = FirebaseDatabase.getInstance().getReference("KNN Data Information").child(Objects.requireNonNull(key));

        knnReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("StringFormatInvalid")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Use the UserModel class to map the data from the database
                    KNNDataInfoModel buddyinfo = dataSnapshot.getValue(KNNDataInfoModel.class);
                    if (buddyinfo != null) {
                        // Debugging log
                        Log.d(TAG, "User profile retrieved: " + buddyinfo.getName());

                        courseText.setText(buddyinfo.getCourse());
                        seniorityText.setText(String.valueOf(buddyinfo.getSeniority()));
                        hobbiesText.setText(buddyinfo.getHobbies());
                        personalitiesText.setText(buddyinfo.getPersonalities());

                    } else {
                        Log.d(TAG, "Buddy Info null");
                    }
                } else {
                    Log.d(TAG, "Data snapshot does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error: " + databaseError.getMessage());
            }
        });

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                String requestUserId = key;

                // References for the current user's requests
                DatabaseReference userReceivedRef = FirebaseDatabase.getInstance().getReference("Buddies").child("friend_requests")
                        .child(userId)
                        .child("sent")
                        .child(requestUserId);

                DatabaseReference targetUserSentRef = FirebaseDatabase.getInstance().getReference("Buddies").child("friend_requests")
                        .child(requestUserId)
                        .child("received")
                        .child(userId);

                // Set values for both users
                userReceivedRef.setValue(true);
                targetUserSentRef.setValue(true);

                // Reference to the MatchResults node for the current user
                DatabaseReference matchResultsRef = FirebaseDatabase.getInstance().getReference("MatchResults").child( userId );

                matchResultsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean hasUpdates = false; // Flag to check if any update is done

                        // Loop through the match results
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String userId = snapshot.getKey(); // User ID in the match results
                            Boolean isMatched = snapshot.getValue(Boolean.class); // Match result

                            // Check if the value is true and the user ID matches the target user ID
                            if (Boolean.TRUE.equals(isMatched) && requestUserId.equals(userId)) {
                                // Update the value to false
                                matchResultsRef.child(userId).setValue(false);
                                hasUpdates = true;
                            }
                        }

                        // Optionally update the target user's match results as well
                        if (hasUpdates) {
                            DatabaseReference targetUserMatchResultsRef = FirebaseDatabase.getInstance().getReference("MatchResults").child(requestUserId);
                            targetUserMatchResultsRef.child(userId).setValue(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors here
                    }
                });
                // Optional: Add a Toast or Log to confirm the request was sent
                Toast.makeText(BuddyProfileActivity.this, "Friend request sent", Toast.LENGTH_SHORT ).show();
                finish();
            }
        });
    }
}