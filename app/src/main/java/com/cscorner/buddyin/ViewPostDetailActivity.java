package com.cscorner.buddyin;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ViewPostDetailActivity extends AppCompatActivity {

    ImageView post_image, profileImage;
    ImageButton send_btn, liked_btn;
    TextView profile_name, context, time, liked_textview, subject, comment_text;
    EditText comment_et;

    String userID = "";
    String username = "";
    String postImage = "";
    String desc = "";
    String timestamp = "";
    String postid = "";
    String user_profile = "";
    String subject_code = "";
    String currimageProfile;
    String current_name;
    Integer commentCount;

    DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference("Post");
    Boolean LikeChecker = false;
    String currentUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

    boolean notify = false;

    List<CommentModel> commentModelList;
    CommentAdapter commentAdapter;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_post_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Adjust the keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        post_image = findViewById(R.id.postImage);
        profileImage = findViewById(R.id.profileImage);
        send_btn = findViewById(R.id.send_btn);
        liked_btn = findViewById(R.id.like_btn);
        profile_name = findViewById(R.id.profile_name);
        context = findViewById(R.id.context);
        time = findViewById(R.id.time);
        liked_textview = findViewById(R.id.liked_textview);
        comment_et = findViewById(R.id.comment_et);
        comment_text = findViewById(R.id.comment);
        subject = findViewById(R.id.subject);

        //Get buddy id
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            userID = bundle.getString("userID");
            username = bundle.getString("username");
            postImage = bundle.getString("postImage");
            desc = bundle.getString("desc");
            timestamp = bundle.getString("timestamp");
            postid = bundle.getString("postid");
            user_profile = bundle.getString("userImage");
            subject_code = bundle.getString("subject");
        };

        checkUserRole(currentUid);

//        imageUrl = bundle.getString("Image");
//        Glide.with(this).load(bundle.getString("Image")).into(detailImage);

        profile_name.setText(username);
        context.setText(desc);
        String timestamp1 = formatTimestamp(timestamp);
        time.setText(timestamp1);
        Glide.with(context).load(user_profile).into(profileImage);

//        subject.setText("Subject Code: " + subject_code);

        if ("General Question".equals(subject_code)) {
            subject.setText(subject_code);
        } else {
            subject.setText("Subject Code: " + subject_code);
        }

        if ("null".equals(postImage)) {
            post_image.setVisibility(View.GONE);
        } else {
            Glide.with(context).load(postImage).into(post_image);
            post_image.setVisibility(View.VISIBLE);
        }

        post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewChatImageActivity.class);
                intent.putExtra("imageUrl", postImage);
                startActivity(intent);
            }
        });

        // Like button functionality
        liked_btn.setOnClickListener(v -> {
            LikeChecker = true;
            likeRef.child(postid).child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (LikeChecker) {
                        if (snapshot.hasChild(currentUid)) {
                            likeRef.child(postid).child("Likes").child(currentUid).removeValue();
                            LikeChecker = false;
                        } else {
                            likeRef.child(postid).child("Likes").child(currentUid).setValue(true);
                            LikeChecker = false;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error if necessary
                }
            });
        });

        // Set like button status
        setLikeButtonStatus();

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String comment = comment_et.getText().toString().trim();
                if (TextUtils.isEmpty(comment)) {//if empty
                    Toast.makeText(ViewPostDetailActivity.this, "Please Write Something Here", Toast.LENGTH_LONG).show();
                } else {
                    sendComment(comment);
                }
                comment_et.setText("");
            }
        });

        RecyclerView comment_rv = findViewById(R.id.comment_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        comment_rv.setLayoutManager(linearLayoutManager);

        commentModelList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentModelList);
        comment_rv.setAdapter(commentAdapter);

        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Post").child(postid).child("Comments");

        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentModelList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    CommentModel comments = snapshot1.getValue(CommentModel.class);
                    commentModelList.add(comments);
                }

                commentAdapter.setCommentModelList(commentModelList);
                commentAdapter.notifyDataSetChanged();

                // Scroll to the latest comment
                comment_rv.scrollToPosition(commentModelList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error: " + error.getMessage());
            }
        });

        setCommentNumber();


    }

    private void setCommentNumber() {
        likeRef.child(postid).child("Comments").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentCount = (int) snapshot.getChildrenCount();
                comment_text.setText("Comments " + commentCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendComment(String comment) {
        DatabaseReference comRef = FirebaseDatabase.getInstance().getReference("Post").child(postid).child("Comments");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String comment_id = comRef.push().getKey();

        CommentModel commentModel = new CommentModel(currentUid, current_name, currimageProfile, timestamp, comment, comment_id);

        comRef.child(comment_id).setValue(commentModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Show a success message
                    Toast.makeText(ViewPostDetailActivity.this, "Comment Send", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Show an error message
                Toast.makeText(ViewPostDetailActivity.this, Objects.requireNonNull(e.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLikeButtonStatus() {
        likeRef.child(postid).child("Likes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int likeCount = (int) snapshot.getChildrenCount();
                liked_textview.setText(String.valueOf(likeCount));
                if (snapshot.hasChild(currentUid)) {
                    liked_btn.setImageResource(R.drawable.round_favorite_24); // Change to filled heart
                } else {
                    liked_btn.setImageResource(R.drawable.rounded_favorite_24); // Change to empty heart
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if necessary
            }
        });
    }

    private String formatTimestamp(String timeStamp) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        return DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
    }

    private void checkUserRole(String currentUid){
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Admin").child(currentUid);

        adminRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && Boolean.TRUE.equals(dataSnapshot.child("isAdmin").getValue(Boolean.class))) {

                    // Retrieve the admin details directly without using a model class
                    String name = dataSnapshot.child("username").getValue(String.class);
                    String profileImage = dataSnapshot.child("profile_image").getValue(String.class);

                    // Logging the data for debugging
                    Log.d(TAG, "Admin Name: " + name);
                    Log.d(TAG, "Admin Profile Image: " + profileImage);

                    currimageProfile = dataSnapshot.child("profile_image").getValue(String.class);
                    // Update the TextView with the retrieved data

                    current_name = dataSnapshot.child("username").getValue(String.class);
                } else {
                    // Check if the user is a lecturer
                    checkLecturerStatus(currentUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewPostDetailActivity.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkLecturerStatus(String currentUid) {
        DatabaseReference lecturerRef = FirebaseDatabase.getInstance().getReference("Lecturers").child(currentUid);

        lecturerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DatabaseReference databaseReference;
                if (dataSnapshot.exists()) {
                    // Reference to "User Info" under the current user
                    databaseReference = FirebaseDatabase.getInstance().getReference("Lecturers").child(currentUid);

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
                                        currimageProfile = userProfile.getProfile_image();
                                        current_name = userProfile.getName();
                                        Log.d(TAG, current_name + currimageProfile);

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
                } else {
                    //Get Current Profile image and name
                    databaseReference = FirebaseDatabase.getInstance().getReference("User Info").child(currentUid);
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
                                        currimageProfile = userProfile.getProfile_image();
                                        current_name = userProfile.getUsername();
                                        Log.d(TAG, current_name + currimageProfile);

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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewPostDetailActivity.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
