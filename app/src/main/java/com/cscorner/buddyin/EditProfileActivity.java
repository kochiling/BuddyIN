package com.cscorner.buddyin;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {

    ImageView profileImage;
    TextView profile_name,courseText,seniorityText,hobbiesText,personalitiesText;
    String key = "";
    String userID = "";
    private DatabaseReference databaseReference;

    private static final int IMAGEPICK_GALLERY_REQUEST = 300;
    private static final int IMAGE_PICKCAMERA_REQUEST = 400;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    String[] cameraPermission;
    String[] storagePermission;
    Uri imageuri = null;

    boolean[] selectedhobbies;
    ArrayList<Integer> hobbiesList = new ArrayList<>();
    String[] hobbiesArray;

    boolean[] selectedpersonalites;
    ArrayList<Integer> personalityList = new ArrayList<>();
    String[] personalityArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
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

        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Reference to "User Info" under the current user
        databaseReference = FirebaseDatabase.getInstance().getReference("User Info").child(currentUserId);

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
                            Glide.with(EditProfileActivity.this)
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

        DatabaseReference knnReference = FirebaseDatabase.getInstance().getReference("KNN Data Information").child(Objects.requireNonNull(currentUserId));

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

        profile_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the layout for the dialog
                View view = LayoutInflater.from(EditProfileActivity.this).inflate(R.layout.dialog_editprofile_layout, null);

                // Build the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setTitle("Enter A New Username");

                // Get reference to the EditText from the dialog
                final EditText profile_name1 = view.findViewById(R.id.input);
                profile_name1.setHint("Enter a new Name");
                builder.setView(view);

                // Set Positive Button for "Change"
                builder.setPositiveButton("Change", (dialog, which) -> {
                    String newProfileName = profile_name1.getText().toString().trim();

                    if (!newProfileName.isEmpty()) {
                        // Call the method to update the profile name in Firebase
                        UpdateUserProfileName updateProfile = new UpdateUserProfileName(newProfileName);
                        Toast.makeText(EditProfileActivity.this, "Username Updated Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Please Enter a New Name to Change", Toast.LENGTH_SHORT).show();
                    }
                });

                // Set Negative Button for "Cancel"
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                // Create and show the dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicDialog();
            }
        });

        seniorityText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the layout for the dialog
                View view = LayoutInflater.from(EditProfileActivity.this).inflate(R.layout.dialog_editprofile_layout, null);

                // Build the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setTitle("Enter the year you want to change");

                // Get reference to the EditText from the dialog
                final EditText year = view.findViewById(R.id.input);
                year.setHint("Enter a number only");
                year.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(view);

                // Set Positive Button for "Add"
                builder.setPositiveButton("Change", (dialog, which) -> {
                    String year1 = year.getText().toString().trim();

                    if (!year1.isEmpty()) {
                        updateSeniority(year1);
                        Toast.makeText(EditProfileActivity.this, "Year Updated Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Please Enter a New Number to Change", Toast.LENGTH_SHORT).show();
                    }
                });

                // Set Negative Button for "Cancel"
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                // Create and show the dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        hobbiesText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the layout for the dialog
                View view = LayoutInflater.from(EditProfileActivity.this).inflate(R.layout.dialog_editprofile_layout, null);

                // Build the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setTitle("Click on the text box below to choose the hobbies");

                // Get reference to the EditText from the dialog
                final EditText hobbies_input = view.findViewById(R.id.input);
                hobbies_input.setHint("Choose Hobbies");
                hobbies_input.setFocusable(false);
                builder.setView(view);

                // Set Positive Button for "Add"
                builder.setPositiveButton("Change", (dialog, which) -> {
                    String hobbies1 = hobbies_input.getText().toString().trim();

                    if (!hobbies1.isEmpty()) {
                        updateHobbies(hobbies1);
                        Toast.makeText(EditProfileActivity.this, "Hobbies Updated Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Select Hobbies to Change", Toast.LENGTH_SHORT).show();
                    }
                });

                // Set Negative Button for "Cancel"
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                // Create and show the dialog
                AlertDialog dialog = builder.create();
                dialog.show();

                //HobbiesType
                hobbiesArray = getResources().getStringArray(R.array.hobbies);
                // initialize selected array
                selectedhobbies = new boolean[hobbiesArray.length];

                hobbies_input.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Initialize alert dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                        // set title
                        builder.setTitle("Select Hobbies");
                        // set dialog non cancelable
                        builder.setCancelable(false);

                        builder.setMultiChoiceItems(hobbiesArray, selectedhobbies, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                // check condition
                                if (b) {
                                    // when checkbox selected
                                    // Add position in list
                                    hobbiesList.add(i);
                                    // Sort array list
                                    Collections.sort(hobbiesList);
                                } else {
                                    // when checkbox unselected
                                    // Remove position from list
                                    hobbiesList.remove(Integer.valueOf(i));
                                }
                            }
                        });

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Initialize string builder
                                StringBuilder stringBuilder = new StringBuilder();
                                // use for loop
                                for (int j = 0; j < hobbiesList.size(); j++) {
                                    // concat array value
                                    stringBuilder.append(hobbiesArray[hobbiesList.get(j)]);
                                    // check condition
                                    if (j != hobbiesList.size() - 1) {
                                        // When j value  not equal
                                        // to list size - 1
                                        // add comma
                                        stringBuilder.append(", ");
                                    }
                                }
                                // set text on textView
                                hobbies_input.setText(stringBuilder.toString());
                            }
                        });

                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // dismiss dialog
                                dialogInterface.dismiss();
                            }
                        });
                        builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // use for loop
                                for (int j = 0; j < selectedhobbies.length; j++) {
                                    // remove all selection
                                    selectedhobbies[j] = false;
                                    // clear list
                                    hobbiesList.clear();
                                    // clear text view value
                                    hobbies_input.setText("");
                                }
                            }
                        });
                        // show dialog
                        builder.show();
                    }
                });
            }
        });

        personalitiesText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the layout for the dialog
                View view = LayoutInflater.from(EditProfileActivity.this).inflate(R.layout.dialog_editprofile_layout, null);

                // Build the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setTitle("Click on the text box below to choose the personalities");

                // Get reference to the EditText from the dialog
                final EditText personalites_input = view.findViewById(R.id.input);
                personalites_input.setHint("Choose Personalities");
                personalites_input.setFocusable(false);
                builder.setView(view);

                // Set Positive Button for "Add"
                builder.setPositiveButton("Change", (dialog, which) -> {
                    String personalites_input1 = personalites_input.getText().toString().trim();

                    if (!personalites_input1.isEmpty()) {
                        updatePersonalites(personalites_input1);
                        Toast.makeText(EditProfileActivity.this, "Personalites Updated Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Select Personalites to Change", Toast.LENGTH_SHORT).show();
                    }
                });

                // Set Negative Button for "Cancel"
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                // Create and show the dialog
                AlertDialog dialog = builder.create();
                dialog.show();

                //Personality Spinner
                personalityArray = getResources().getStringArray(R.array.personalities);
                // initialize selected array
                selectedpersonalites = new boolean[personalityArray.length];

                personalites_input.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Initialize alert dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                        // set title
                        builder.setTitle("Select Personalities");
                        // set dialog non cancelable
                        builder.setCancelable(false);

                        builder.setMultiChoiceItems(personalityArray, selectedpersonalites, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                // check condition
                                if (b) {
                                    // when checkbox selected
                                    // Check if the limit is reached
                                    if (personalityList.size() < 3) {
                                        // Add position in list
                                        personalityList.add(i);
                                        // Sort array list
                                        Collections.sort(personalityList);
                                    } else {
                                        // Show a message to the user
                                        Toast.makeText(EditProfileActivity.this, "You can select up to 3 personalities only", Toast.LENGTH_SHORT).show();
                                        // Uncheck the checkbox
                                        ((AlertDialog) dialogInterface).getListView().setItemChecked(i, false);
                                    }
                                } else {
                                    // when checkbox unselected
                                    // Remove position from list
                                    personalityList.remove(Integer.valueOf(i));
                                }
                            }
                        });

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Initialize string builder
                                StringBuilder stringBuilder = new StringBuilder();
                                // use for loop
                                for (int j = 0; j < personalityList.size(); j++) {
                                    // concat array value
                                    stringBuilder.append(personalityArray[personalityList.get(j)]);
                                    // check condition
                                    if (j != personalityList.size() - 1) {
                                        // When j value  not equal
                                        // to list size - 1
                                        // add comma
                                        stringBuilder.append(", ");
                                    }
                                }
                                // set text on textView
                                personalites_input.setText(stringBuilder.toString());
                            }
                        });

                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // dismiss dialog
                                dialogInterface.dismiss();
                            }
                        });

                        builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // use for loop
                                for (int j = 0; j < selectedpersonalites.length; j++) {
                                    // remove all selection
                                    selectedpersonalites[j] = false;
                                }
                                // clear list
                                personalityList.clear();
                                // clear text view value
                                personalites_input.setText("");
                            }
                        });
                        // show dialog
                        builder.show();
                    }
                });
            }
        });

        courseText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the layout for the dialog
                View view = LayoutInflater.from(EditProfileActivity.this).inflate(R.layout.dialog_course_layout, null);

                // Build the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setTitle("Select a new course");

                // Get reference to the EditText from the dialog
                final EditText course_input = view.findViewById(R.id.courseinput);
                course_input.setFocusable(false);
                builder.setView(view);

                // Set Positive Button for "Add"
                builder.setPositiveButton("Add", (dialog, which) -> {
                    String course_input1 = course_input.getText().toString().trim();

                    if (!course_input1.isEmpty()) {
                        // Call the method to update the profile name in Firebase
                        updatecourse(course_input1);
                        Toast.makeText(EditProfileActivity.this, "Course Updated Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Select Course to Change", Toast.LENGTH_SHORT).show();
                    }
                });

                // Set Negative Button for "Cancel"
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                // Create and show the dialog
                AlertDialog dialog = builder.create();
                dialog.show();

                // Course Array
                String[] courseArray = getResources().getStringArray(R.array.coursesinput);

                course_input.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Initialize alert dialog for course selection
                        AlertDialog.Builder courseBuilder = new AlertDialog.Builder(EditProfileActivity.this);
                        courseBuilder.setTitle("Select Course");

                        // Create custom adapter for the list with gaps between items
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(EditProfileActivity.this, R.layout.drop_down_item, courseArray) ;

                        // Set the custom adapter with items and spacing
                        courseBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Set the selected course in the input field
                                course_input.setText(courseArray[i]);
                            }
                        });

                        courseBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> {
                            // Dismiss dialog
                            dialogInterface.dismiss();
                        });

                        // Show dialog
                        courseBuilder.show();
                    }
                });
            }
        });
    }

    private void updatecourse(String courseInput1) {
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        DatabaseReference knnRef = FirebaseDatabase.getInstance().getReference("KNN Data Information").child(currentUserId);
        knnRef.child("course").setValue(courseInput1)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Course updated in KNN Data Information");
                    } else {
                        Log.e(TAG, "Failed to update Course in KNN Data Information", task.getException());
                    }
                });
    }

    private void updatePersonalites(String personalitesInput1) {
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        DatabaseReference knnRef = FirebaseDatabase.getInstance().getReference("KNN Data Information").child(currentUserId);
        knnRef.child("personalities").setValue(personalitesInput1)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Personalities updated in KNN Data Information");
                    } else {
                        Log.e(TAG, "Failed to update Personalities in KNN Data Information", task.getException());
                    }
                });

    }


    private void updateHobbies(String hobbies1) {

        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        DatabaseReference knnRef = FirebaseDatabase.getInstance().getReference("KNN Data Information").child(currentUserId);
        knnRef.child("hobbies").setValue(hobbies1)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Hobbies updated in KNN Data Information");
                    } else {
                        Log.e(TAG, "Failed to update hobbies in KNN Data Information", task.getException());
                    }
                });
    }

    private void updateSeniority(String year1) { // Corrected spelling
        Integer year2 = Integer.valueOf(year1);
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        DatabaseReference knnRef = FirebaseDatabase.getInstance().getReference("KNN Data Information").child(currentUserId);
        knnRef.child("seniority").setValue(year2)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Seniority updated in KNN Data Information");
                    } else {
                        Log.e(TAG, "Failed to update seniority in KNN Data Information", task.getException());
                    }
                });
    }

    private void showImagePicDialog() {
        String[] options = {"Camera", "Gallery"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(EditProfileActivity.this);
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
                imageuri = Objects.requireNonNull(data).getData(); // get image data to upload
                try {
                    updateImage(imageuri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (requestCode == IMAGE_PICKCAMERA_REQUEST) {
                try {
                    updateImage(imageuri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
        imageuri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent camerIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camerIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
        startActivityForResult(camerIntent, IMAGE_PICKCAMERA_REQUEST);
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGEPICK_GALLERY_REQUEST);
    }

    private Boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        requestPermissions(storagePermission, STORAGE_REQUEST);
    }

    private void updateImage(Uri imageUri) throws IOException {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Updating Image...");
        dialog.setCancelable(false); // Prevent canceling the dialog
        dialog.show();

        // Convert Uri to a String
        String imagePath = imageUri.getLastPathSegment(); // Get the last part of the URI for naming

        // Get Bitmap from Uri
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, arrayOutputStream); // Compressing the image
        final byte[] data = arrayOutputStream.toByteArray();

        // Create a reference in Firebase Storage
        StorageReference ref = FirebaseStorage.getInstance().getReference("User Profile Pics").child(imagePath);

        // Upload the image
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                dialog.dismiss();
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                uriTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            String downloadUri = task.getResult().toString(); // Getting the download URL
                            // Call the method to update the profile image in Firebase
                            UpdateUserProfileImage updateProfileImage = new UpdateUserProfileImage(downloadUri);
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private static class UpdateUserProfileImage {

        private final String downloadUri;
        private final DatabaseReference userRef, knnRef, notesRef, postsRef;

        public UpdateUserProfileImage(String downloadUri) {
            this.downloadUri = downloadUri;
            FirebaseDatabase database = FirebaseDatabase.getInstance();

            // Get the current user ID
            String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

            // Initialize references to the relevant database nodes
            this.userRef = database.getReference("User Info").child(currentUserId);
            this.knnRef = database.getReference("KNN Data Information").child(currentUserId);
            this.notesRef = database.getReference("Notes");
            this.postsRef = database.getReference("Post");

            // Start updating the profile image in different nodes
            updateProfileImageInUserInfo();
            updateProfileImageInPosts(currentUserId);
            updateProfileImageInComments(currentUserId);
        }

        private void updateProfileImageInUserInfo() {
            userRef.child("profile_image").setValue(downloadUri)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Profile image updated in User Info");
                        } else {
                            Log.e(TAG, "Failed to update profile image in User Info", task.getException());
                        }
                    });
        }

        private void updateProfileImageInComments(String currentUserId) {
            postsRef.orderByChild("user_id").equalTo(currentUserId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                snapshot.getRef().child("user_profile").setValue(downloadUri)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Profile image updated in Comments for user_id: " + currentUserId);
                                            } else {
                                                Log.e(TAG, "Failed to update profile image in Comments", task.getException());
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Failed to update profile image in Comments", databaseError.toException());
                        }
                    });
        }

        private void updateProfileImageInPosts(String currentUserId) {
            postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot postSnapshot) {
                    for (DataSnapshot post : postSnapshot.getChildren()) {
                        // Check if the post has a "Comments" path
                        if (post.hasChild("Comments")) {
                            DataSnapshot commentsSnapshot = post.child("Comments");

                            // Loop through the comments and update where user_id matches
                            for (DataSnapshot commentSnapshot : commentsSnapshot.getChildren()) {
                                String commentUserId = commentSnapshot.child("user_id").getValue(String.class);
                                if (commentUserId != null && commentUserId.equals(currentUserId)) {
                                    commentSnapshot.getRef().child("user_profile").setValue(downloadUri)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "Profile image updated in Comment for user_id: " + currentUserId);
                                                } else {
                                                    Log.e(TAG, "Failed to update profile image in Comment", task.getException());
                                                }
                                            });
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Failed to update profile image in Posts or Comments", databaseError.toException());
                }
            });
        }
    }


    private static class UpdateUserProfileName {
        private final String newProfileName;
        private final DatabaseReference userRef, knnRef, notesRef, postsRef;

        public UpdateUserProfileName(String newProfileName) {
            this.newProfileName = newProfileName;
            FirebaseDatabase database = FirebaseDatabase.getInstance();

            // Get the current user ID
            String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

            // Initialize references to the relevant database nodes
            this.userRef = database.getReference("User Info").child(currentUserId);
            this.knnRef = database.getReference("KNN Data Information").child(currentUserId);
            this.notesRef = database.getReference("Notes");
            this.postsRef = database.getReference("Post");
            // Start updating the profile name in different nodes
            updateProfileNameInUserInfo();
            updateProfileNameInKNNInfo();
            updateProfileNameInNotes(currentUserId);
            updateProfileNameInPosts(currentUserId);
            updateProfileNameInComments(currentUserId);
        }

        // Update the profile name in UserInfo node
        private void updateProfileNameInUserInfo() {
            userRef.child("username").setValue(newProfileName)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Username updated in UserInfo");
                        } else {
                            Log.e(TAG, "Failed to update username in UserInfo", task.getException());
                        }
                    });
        }

        // Update the profile name in KNN Data Information node
        private void updateProfileNameInKNNInfo() {
            knnRef.child("name").setValue(newProfileName)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Username updated in KNNDataInformation");
                        } else {
                            Log.e(TAG, "Failed to update username in KNNDataInformation", task.getException());
                        }
                    });
        }

        // Update the profile name in Notes where the user_id matches the current user
        private void updateProfileNameInNotes(String userId) {
            notesRef.orderByChild("user_id").equalTo(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                snapshot.getRef().child("user_name").setValue(newProfileName);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Failed to update username in Notes", databaseError.toException());
                        }
                    });
        }

        // Update the profile name in Posts where the user_id matches the current user
        private void updateProfileNameInPosts(String userId) {
            postsRef.orderByChild("user_id").equalTo(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                snapshot.getRef().child("username").setValue(newProfileName);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Failed to update username in Posts", databaseError.toException());
                        }
                    });
        }

        // Update the profile name in Comments where the user_id matches the current user
        private void updateProfileNameInComments(String userId) {
            postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot postSnapshot) {
                    for (DataSnapshot post : postSnapshot.getChildren()) {
                        // Check if the post has a "Comments" path
                        if (post.hasChild("Comments")) {
                            DataSnapshot commentsSnapshot = post.child("Comments");

                            // Loop through the comments and update where user_id matches
                            for (DataSnapshot commentSnapshot : commentsSnapshot.getChildren()) {
                                String commentUserId = commentSnapshot.child("user_id").getValue(String.class);
                                if (commentUserId != null && commentUserId.equals(userId)) {
                                    commentSnapshot.getRef().child("username").setValue(newProfileName)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "Username updated in Comment for user_id: " + userId);
                                                } else {
                                                    Log.e(TAG, "Failed to update username in Comment", task.getException());
                                                }
                                            });
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Failed to update username in Posts or Comments", databaseError.toException());
                }
            });
        }
    }
}