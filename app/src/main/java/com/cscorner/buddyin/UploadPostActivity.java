package com.cscorner.buddyin;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Objects;

public class UploadPostActivity extends AppCompatActivity {

    AutoCompleteTextView subjectCode_spinner;
    TextInputEditText input_desc;
    ImageView uploadPostPic;
    MaterialButton upload_btn;
    ArrayAdapter<String> adapter;
    ArrayList<String> subjectList;
    TextView profilename;
    TextInputLayout subjectlayout, desclayout;
    FirebaseAuth mAuth;
    DatabaseReference dbref, databaseReference;
    ImageView profileimage;
    String imageProfile;

    String imageURL;
    Uri uri;
    private static final int IMAGEPICK_GALLERY_REQUEST = 300;
    private static final int IMAGE_PICKCAMERA_REQUEST = 400;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    String[] cameraPermission;
    String[] storagePermission;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            return insets;
        });

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        subjectCode_spinner = findViewById(R.id.subjectCode_spinner);
        input_desc = findViewById(R.id.input_desc);
        uploadPostPic = findViewById(R.id.uploadPostPic);
        upload_btn = findViewById(R.id.upload_btn);
        profilename = findViewById(R.id.profile_name);
        subjectlayout = findViewById(R.id.subjectlayout);
        desclayout = findViewById(R.id.input_desclayout);
        profileimage = findViewById(R.id.profileImage);
        //Get Profile Name
        // Get the current user ID
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        checkUserRole(userId);

        subjectList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(UploadPostActivity.this, android.R.layout.simple_spinner_dropdown_item, subjectList);
        subjectCode_spinner.setAdapter(adapter);
        dbref = FirebaseDatabase.getInstance().getReference("Subject Data");

        subjectCode_spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedSubject = adapter.getItem(position);
                if (selectedSubject.equals("Others")) {
                    showAddSubjectDialog();
                }
            }
        });
        ShowData();

        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        //For image upload
        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        assert data != null;
                        uri = data.getData();
                        uploadPostPic.setImageURI(uri);
                    } else {
                        Toast.makeText(UploadPostActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        uploadPostPic.setOnClickListener(view -> {
            showImagePicDialog();
        });
    }

    private void showImagePicDialog() {
        String[] options = {"Camera", "Gallery"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(UploadPostActivity.this);
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == 0) {
                    if (!checkCameraPermission()) { // if permission is not given
                        requestCameraPermission(); // request for permission
                    } else {
                        pickFromCamera(); // if already access granted then click
                    }
                } else if (which == 1) {
                    if (!checkStoragePermission()) { // if permission is not given
                        requestStoragePermission(); // request for permission
                    } else {
                        pickFromGallery(); // if already access granted then pick
                    }
                }
            }
        });
        builder.create().show();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // request for permission if not given
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST: {
                if (grantResults.length > 0) {
                    boolean camera_accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageaccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (camera_accepted && writeStorageaccepted) {
                        pickFromCamera(); // if access granted then click
                    } else {
                        Toast.makeText(this, "Please Enable Camera and Storage Permissions", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST: {
                if (grantResults.length > 0) {
                    boolean writeStorageaccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageaccepted) {
                        pickFromGallery(); // if access granted then pick
                    } else {
                        Toast.makeText(this, "Please Enable Storage Permissions", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGEPICK_GALLERY_REQUEST) {
                uri = Objects.requireNonNull(data).getData();
                uploadPostPic.setImageURI(uri);
            }
            if (requestCode == IMAGE_PICKCAMERA_REQUEST) {
                uploadPostPic.setImageURI(uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        requestPermissions(cameraPermission, CAMERA_REQUEST);
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_pic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent camerIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camerIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(camerIntent, IMAGE_PICKCAMERA_REQUEST);
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGEPICK_GALLERY_REQUEST);
    }

    private Boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(UploadPostActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        requestPermissions(storagePermission, STORAGE_REQUEST);
    }


    public void saveData(){
        if (validateSubject() & validateDesc() ) {
            if (uri != null) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Post Pictures")
                        .child(Objects.requireNonNull(uri.getLastPathSegment()));
                AlertDialog.Builder builder = new AlertDialog.Builder(UploadPostActivity.this);
                builder.setCancelable(false);
                builder.setView(R.layout.progress_layout);
                AlertDialog dialog = builder.create();
                dialog.show();
                storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isComplete()) ;
                        Uri urlImage = uriTask.getResult();
                        imageURL = urlImage.toString();
                        saveInfo();
                        dialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                    }
                });
            } else {
                imageURL = "null";
                saveInfo();
            }
        } else {
            Toast.makeText(UploadPostActivity.this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveInfo(){
        //get the user_id
        FirebaseAuth dbAuth = FirebaseAuth.getInstance();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String user_id = Objects.requireNonNull(dbAuth.getCurrentUser()).getUid();
        String subject = subjectCode_spinner.getText().toString().trim();
        String description = Objects.requireNonNull(input_desc.getText()).toString().trim();
        String username = profilename.getText().toString().trim();

        // Reference to the "Post" node under the current user's ID
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Post");
        // Generate a unique key for the new post
        String post_id = postRef.push().getKey();

        //PostModel postModel = new PostModel(description,imageURL,subject,user_id,username,post_id,timestamp);
        PostModel postModel = new PostModel(imageProfile,description,imageURL,subject,user_id,username,post_id,timestamp);

        // Set the post value in the database under the generated post ID
        postRef.child(Objects.requireNonNull(post_id)).setValue(postModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Show a success message
                    Toast.makeText(UploadPostActivity.this, "Posted Successfully", Toast.LENGTH_LONG).show();
                    // Finish the activity and return to the previous screen
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Show an error message
                Toast.makeText(UploadPostActivity.this, Objects.requireNonNull(e.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean validateSubject() {
        String subject = subjectCode_spinner.getText().toString().trim();
        if (subject.isEmpty()) {
            subjectlayout.setError("Subject is required!");
            return false;
        } else if (subject.equals("Others")) {
            subjectlayout.setError("Please select another subject");
            return false;
        } else {
            subjectlayout.setError(null);
            return true;
        }
    }

    private boolean validateDesc() {
        String description = input_desc.getText().toString().trim();
        if (description.isEmpty()) {
            desclayout.setError("Description is required!");
            return false;
        } else {
            desclayout.setError(null);
            return true;
        }
    }

    //Add new subjectCode to spinner
    private void ShowData() {
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subjectList.clear(); // Clear the list here to avoid duplicates
                for (DataSnapshot item : snapshot.getChildren()) {
                    subjectList.add(Objects.requireNonNull(item.getValue()).toString());
                }
                subjectList.add("General Question");
                subjectList.add("Others");
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadPostActivity.this, "Failed to load subjects.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddSubjectDialog() {
        View view = LayoutInflater.from(UploadPostActivity.this).inflate(R.layout.dialog_addsubject_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Subject");

        final EditText subjectCode = view.findViewById(R.id.subjectcodeinput);
        final EditText subjectName = view.findViewById(R.id.subjectnameinput);
        builder.setView(view);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String newSubject = subjectCode.getText().toString().trim() + " - " + subjectName.getText().toString().trim();
            if (!newSubject.isEmpty()) {
                dbref.push().setValue(newSubject).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(UploadPostActivity.this, "Subject added successfully.", Toast.LENGTH_SHORT).show();
                        ShowData();
                    } else {
                        Toast.makeText(UploadPostActivity.this, "Failed to add subject.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void checkUserRole(String userId) {
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Admin").child(userId);

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
                    // Display the admin data in your UI
                    if (profileImage != null && !profileImage.isEmpty()) {
                        Glide.with(UploadPostActivity.this)
                                .load(profileImage)
                                .into(profileimage); // Assuming profileimage is an ImageView
                    }
                    imageProfile = dataSnapshot.child("profile_image").getValue(String.class);
                    // Update the TextView with the retrieved data

                    profilename.setText(name);
                } else {
                    // Check if the user is a lecturer
                    checkLecturerStatus(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UploadPostActivity.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkLecturerStatus(String userId) {
        DatabaseReference lecturerRef = FirebaseDatabase.getInstance().getReference("Lecturers").child(userId);

        lecturerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
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
                                        Glide.with(UploadPostActivity.this)
                                                .load(userProfile.getProfile_image())
                                                .into(profileimage);

                                        imageProfile = userProfile.getProfile_image();
                                        Log.d(TAG,imageProfile);

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
                } else {
                    // Reference to "User Info" under the current user
                    databaseReference = FirebaseDatabase.getInstance().getReference("User Info").child(userId);

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
                                        Glide.with(UploadPostActivity.this)
                                                .load(userProfile.getProfile_image())
                                                .into(profileimage);

                                        imageProfile = userProfile.getProfile_image();
                                        Log.d(TAG,imageProfile);

                                        // Update the TextView with the retrieved data
                                        profilename.setText(userProfile.getUsername());
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
                Toast.makeText(UploadPostActivity.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

//        // Reference to "User Info" under the current user
//        databaseReference = FirebaseDatabase.getInstance().getReference("User Info").child(userId);
//
//        // Add a ValueEventListener to retrieve and update data
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @SuppressLint("StringFormatInvalid")
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    // Use the UserModel class to map the data from the database
//                    UserModel userProfile = dataSnapshot.getValue(UserModel.class);
//                    if (userProfile != null) {
//                        // Debugging log
//                        Log.d(TAG, "User profile retrieved: " + userProfile.getUsername());
//                        // Load the profile image using Glide
//                        if (userProfile.getProfile_image() != null && !userProfile.getProfile_image().isEmpty()) {
//                            Glide.with(UploadPostActivity.this)
//                                    .load(userProfile.getProfile_image())
//                                    .into(profileimage);
//
//                            imageProfile = userProfile.getProfile_image();
//                            Log.d(TAG,imageProfile);
//
//                            // Update the TextView with the retrieved data
//                            profilename.setText(userProfile.getUsername());
//                        }
//                    } else {
//                        Log.d(TAG, "User profile is null");
//                    }
//                } else {
//                    Log.d(TAG, "Data snapshot does not exist");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.e(TAG, "Error: " + databaseError.getMessage());
//            }
//        });
