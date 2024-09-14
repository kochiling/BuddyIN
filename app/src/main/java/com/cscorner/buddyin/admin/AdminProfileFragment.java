package com.cscorner.buddyin.admin;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cscorner.buddyin.MainActivity;
import com.cscorner.buddyin.R;
import com.cscorner.buddyin.UserModel;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class AdminProfileFragment extends Fragment {

    TabLayout tabLayout;
    ViewPager2 viewPager;
    ImageView profileimage;
    TextView profilename;
    FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_profile, container, false);

        profileimage = view.findViewById(R.id.profileImage);
        profilename = view.findViewById(R.id.profile_name);
        mAuth = FirebaseAuth.getInstance();

        // Get the current user ID
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        // Reference to "Admin" node under the current user
        DatabaseReference adminReference = FirebaseDatabase.getInstance().getReference("Admin").child(userId);

// Add a ValueEventListener to retrieve and update data
        adminReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the admin details directly without using a model class
                    String name = dataSnapshot.child("username").getValue(String.class);
                    String profileImage = dataSnapshot.child("profile_image").getValue(String.class);

                    // Logging the data for debugging
                    Log.d(TAG, "Admin Name: " + name);
                    Log.d(TAG, "Admin Profile Image: " + profileImage);

                    // Display the admin data in your UI
                    if (profileImage != null && !profileImage.isEmpty()) {
                        Glide.with(getContext())
                                .load(profileImage)
                                .into(profileimage); // Assuming profileimage is an ImageView
                    }

                    // Update the TextView with the retrieved data
                    profilename.setText(name);
                } else {
                    Log.d(TAG, "Admin data does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error: " + databaseError.getMessage());
            }
        });


        return view;
    }
}