package com.cscorner.buddyin.lecturer;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cscorner.buddyin.EditProfileActivity;
import com.cscorner.buddyin.LecturerModel;
import com.cscorner.buddyin.MainActivity;
import com.cscorner.buddyin.ProfileViewPagerAdapter;
import com.cscorner.buddyin.R;
import com.cscorner.buddyin.UserModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LecturerProfileFragment extends Fragment {

    TabLayout tabLayout;
    ViewPager2 viewPager;
    ImageButton logout;
    ImageView profileimage;
    TextView profilename;
    FirebaseAuth mAuth;
    Button edit_pro_btn;

    private DatabaseReference databaseReference;

    public LecturerProfileFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lecturer_profile, container, false);

        logout = view.findViewById(R.id.logout);
        profileimage = view.findViewById(R.id.profileImage);
        profilename = view.findViewById(R.id.profile_name);
        mAuth = FirebaseAuth.getInstance();
        edit_pro_btn = view.findViewById(R.id.edit_pro_btn);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut(); // Sign out the user

                // Redirect to a login activity
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        // Get the current user ID
        String userId = mAuth.getCurrentUser().getUid();
        Log.d(TAG,"userId"+userId);

        // Reference to "User Info" under the current user
        databaseReference = FirebaseDatabase.getInstance().getReference("Lecturers").child(userId);

        // Add a ValueEventListener to retrieve and update data
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("StringFormatInvalid")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Use the UserModel class to map the data from the database
                    LecturerModel userProfile = dataSnapshot.getValue(LecturerModel.class);
                    if (userProfile != null) {
                        // Debugging log
                        Log.d(TAG, "User profile retrieved: " + userProfile.getName());
                        // Load the profile image using Glide
                        if (userProfile.getProfile_image() != null && !userProfile.getProfile_image().isEmpty()) {
                            Glide.with(getContext())
                                    .load(userProfile.getProfile_image())
                                    .into(profileimage);

                            // Update the TextView with the retrieved data
                            profilename.setText(userProfile.getName());
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

        edit_pro_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LecturerEditProfileActivity.class);
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

}