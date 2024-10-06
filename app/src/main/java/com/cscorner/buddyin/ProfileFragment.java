package com.cscorner.buddyin;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class ProfileFragment extends Fragment {

    TabLayout tabLayout;
    ViewPager2 viewPager;
    ImageButton logout;
    ImageView profileimage;
    TextView profilename;
    FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    Button edit_pro_btn;
    private ValueEventListener valueEventListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        logout = view.findViewById(R.id.logout);
        profileimage = view.findViewById(R.id.profileImage);
        profilename = view.findViewById(R.id.profile_name);
        mAuth = FirebaseAuth.getInstance();
        edit_pro_btn = view.findViewById(R.id.edit_pro_btn);
        // Get the current user ID
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out the user from FirebaseAuth
                FirebaseAuth.getInstance().signOut();

                // Redirect to the login activity
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                // Finish the current activity to prevent returning to it
                getActivity().finish();

                Log.e(TAG,"logout account"+ userId);
            }
        });




        // Reference to "User Info" under the current user
        databaseReference = FirebaseDatabase.getInstance().getReference("User Info").child(userId);

        // Add a ValueEventListener to retrieve and update data
        valueEventListener = new ValueEventListener() {
            @SuppressLint("StringFormatInvalid")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isAdded()) {
                    return; // Fragment is not attached, exit the method
                }

                if (dataSnapshot.exists()) {
                    // Use the UserModel class to map the data from the database
                    UserModel userProfile = dataSnapshot.getValue(UserModel.class);
                    if (userProfile != null) {
                        // Debugging log
                        Log.d(TAG, "User profile retrieved: " + userProfile.getUsername());
                        // Load the profile image using Glide
                        if (userProfile.getProfile_image() != null && !userProfile.getProfile_image().isEmpty()) {
                            Glide.with(requireContext())
                                    .load(userProfile.getProfile_image())
                                    .into(profileimage);
                        }

                        // Update the TextView with the retrieved data
                        profilename.setText(userProfile.getUsername());
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
        };

        databaseReference.addValueEventListener(valueEventListener);
        edit_pro_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.profiletab);
        viewPager = view.findViewById(R.id.profile_vpager);

        viewPager.setAdapter(new ProfileViewPagerAdapter(getActivity()));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Posts");
                    break;
                case 1:
                    tab.setText("Notes");
                    break;
            }
        }).attach();

    }

    @Override
    public void onStop() {
        super.onStop();
        if (databaseReference != null && valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener); // Remove the listener
        }
    }
}




