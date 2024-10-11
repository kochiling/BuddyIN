package com.cscorner.buddyin;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class NewBuddyProfileActivity extends AppCompatActivity {

    ImageView profileImage;
    TextView profile_name,courseText,seniorityText,hobbiesText,personalitiesText;
    Button add_btn;
    String key = "";
    String userID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_buddy_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        profileImage = findViewById(R.id.profileImage);
        profile_name = findViewById(R.id.profile_name);
        courseText = findViewById(R.id.courseText);
        seniorityText = findViewById(R.id.semesterText);
        hobbiesText = findViewById(R.id.hobbiesText);
        personalitiesText = findViewById(R.id.personalityText);

        Toolbar toolbar = findViewById(R.id.buddyP_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Buddy's Information");
        // Enable the Up button (back button)
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

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
                            Glide.with(NewBuddyProfileActivity.this)
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
    }

    @Override
    public boolean onOptionsItemSelected (@NonNull MenuItem item){
        if (item.getItemId() == android.R.id.home) {// Handle the Up button click (e.g., navigate back)
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}