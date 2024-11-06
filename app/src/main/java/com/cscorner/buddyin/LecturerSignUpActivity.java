package com.cscorner.buddyin;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Objects;

public class LecturerSignUpActivity extends AppCompatActivity {

    TextInputEditText emailinput,passwordinput,confirminput,nameinput,idinput;
    AutoCompleteTextView facultyinput;
    ProgressBar progressBar;
    TextInputLayout emailinputlayout,passwordinputlayout, confirminputlayout,nameinputlayout,facultyinputlayout,idinputlayout;
    CheckBox checkBox;
    Button registerbtn;
    ImageView upload_pics;
    String imageURL;
    Uri uri;
    FirebaseAuth mAuth;

    private static final int IMAGEPICK_GALLERY_REQUEST = 300;
    private static final int IMAGE_PICKCAMERA_REQUEST = 400;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    String[] cameraPermission;
    String[] storagePermission;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lecturer_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        mAuth = FirebaseAuth.getInstance();
        emailinput = findViewById(R.id.emailinput);
        passwordinput = findViewById(R.id.passwordinput);
        confirminput = findViewById(R.id.confirminput);
        nameinput = findViewById(R.id.nameinput);
        facultyinput = findViewById(R.id.facultyinput);
        idinput = findViewById(R.id.idinput);
        registerbtn = findViewById(R.id.registerbtn);
        upload_pics = findViewById(R.id.upload_pics);
        checkBox = findViewById(R.id.terms_checkbox);
        progressBar = findViewById(R.id.progressBar);
        emailinputlayout = findViewById(R.id.emailinputlayout);
        passwordinputlayout = findViewById(R.id.passwordinputlayout);
        confirminputlayout = findViewById(R.id.confirminputlayout);
        nameinputlayout = findViewById(R.id.nameinputlayout);
        facultyinputlayout = findViewById(R.id.facultyinputlayout);
        idinputlayout = findViewById(R.id.idinputlayout);

        //GenderType
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.facultyinput, R.layout.drop_down_item);
        adapter.setDropDownViewResource(R.layout.drop_down_item);
        facultyinput.setAdapter(adapter);

        facultyinput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(LecturerSignUpActivity.this, facultyinput.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        upload_pics.setOnClickListener(view -> {
//            Intent photoPicker = new Intent(Intent.ACTION_PICK);
//            photoPicker.setType("image/*");
//            activityResultLauncher.launch(photoPicker);
            showImagePicDialog();
        });

        passwordinput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();

                // Initialize the error message
                StringBuilder errorMessage = new StringBuilder("Password should contain:");

                // Check password length
                if (password.length() < 6) {
                    errorMessage.append("\n- At least 6 characters");
                }

                // Check for lowercase letters
                boolean hasLowerCase = !password.equals(password.toUpperCase());
                if (!hasLowerCase) {
                    errorMessage.append("\n- At least one lowercase letter");
                }

                // Check for uppercase letters
                boolean hasUpperCase = !password.equals(password.toLowerCase());
                if (!hasUpperCase) {
                    errorMessage.append("\n- At least one uppercase letter");
                }

                // Check for numbers
                boolean hasNumber = password.matches(".*\\d.*");
                if (!hasNumber) {
                    errorMessage.append("\n- At least one number");
                }

                // Check for special characters
                boolean hasSpecialChar = password.matches(".*[^a-zA-Z0-9].*");
                if (!hasSpecialChar) {
                    errorMessage.append("\n- At least one special character");
                }

                // Set error message based on criteria
                if (password.length() >= 6 && hasLowerCase && hasUpperCase && hasNumber && hasSpecialChar) {
                    passwordinputlayout.setHelperText("");
                    passwordinputlayout.setError(null);  // Clear error if all criteria are met
                } else {
                    passwordinputlayout.setError(errorMessage.toString());
                    passwordinputlayout.setHelperText(null);  // Clear helper text
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    private void showImagePicDialog() {
        String[] options = {"Camera", "Gallery"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(LecturerSignUpActivity.this);
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
                upload_pics.setImageURI(uri);
            }
            if (requestCode == IMAGE_PICKCAMERA_REQUEST) {
                upload_pics.setImageURI(uri);
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
        boolean result = ContextCompat.checkSelfPermission(LecturerSignUpActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        requestPermissions(storagePermission, STORAGE_REQUEST);
    }


    @Override
    public void onBackPressed() {
        boolean isFormCompleted = false;
        if (!isFormCompleted) {
            showExitConfirmationDialog();
        } else {
            super.onBackPressed();
        }
    }

    // Show dialog when user tries to exit with incomplete form
    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setMessage("You have not completed the registration. Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        LecturerSignUpActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void saveData() {
        progressBar.setVisibility(View.VISIBLE);
        registerbtn.setEnabled(false);
        // Get input values
        String email = emailinput.getText().toString().trim();
        String password = passwordinput.getText().toString().trim();
        String confirmPassword = confirminput.getText().toString().trim();
        String name = nameinput.getText().toString().trim();
        String faculty = facultyinput.getText().toString().trim();
        String l_id = idinput.getText().toString().trim();

        // Validate all inputs
        if (!validateName(name) | !validateFaculty(faculty) | !validateID(l_id) | !validateEmail(email) | !validatePassword(password) | !validatePasswordConfirmation(password, confirmPassword)| !validateCheckBox())  {
            progressBar.setVisibility(View.GONE);
            registerbtn.setEnabled(true);
            return;
        }


        // Register user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // If registration is successful, handle image upload and save user data
                        if (uri != null) {
                            uploadImageAndSaveUserData();
                        } else {
                            // Set a default image URL or handle without an image
                            imageURL = "https://firebasestorage.googleapis.com/v0/b/buddyin-70.appspot.com/o/profile_image.jpg?alt=media&token=eca1a67d-9802-4a9d-a8bc-515947abf84b";
                            saveUserInfoToDatabase();
                        }
                    } else {
                        // Registration failed
                        progressBar.setVisibility(View.GONE);
                        registerbtn.setEnabled(true);
                        Toast.makeText(LecturerSignUpActivity.this, "Failed to register: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void uploadImageAndSaveUserData() {
        // Upload image to Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Lecturer Profile Pics")
                .child(Objects.requireNonNull(uri.getLastPathSegment()));

        AlertDialog.Builder builder = new AlertDialog.Builder(LecturerSignUpActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        storageReference.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isComplete());
                    Uri urlImage = uriTask.getResult();
                    imageURL = urlImage.toString();
                    saveUserInfoToDatabase();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    registerbtn.setEnabled(true);
                    Toast.makeText(LecturerSignUpActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserInfoToDatabase() {
        // Get current user's UID
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        boolean isLecturer = true;
        int status = 0;
        String email = emailinput.getText().toString().trim();
        String name = nameinput.getText().toString().trim();
        String faculty = facultyinput.getText().toString().trim();
        String l_id = idinput.getText().toString().trim();

        // Create a new Lecturer object
        LecturerModel lecturer = new LecturerModel (isLecturer, status, imageURL,userId,faculty,email,l_id,name);


        // Store lecturer data in the "Lecturers" node with userId as the key
        FirebaseDatabase.getInstance().getReference("Lecturers")
                .child(userId)
                .setValue(lecturer)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(LecturerSignUpActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                        // Redirect to another activity or perform another action
                        showRegistrationSuccessDialog();
                    } else {
                        Toast.makeText(LecturerSignUpActivity.this, "Failed to store user details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRegistrationSuccessDialog() {
        progressBar.setVisibility(View.GONE);
        // Build an AlertDialog
        new AlertDialog.Builder(this)
                .setTitle("Registration Successful")
                .setMessage("Your registration as a lecturer was successful.You will need to wait for an approve to your account. You will be logged out and redirected to the main page.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Log out the user
                    FirebaseAuth.getInstance().signOut();

                    // Redirect to MainActivity
                    Intent intent = new Intent(LecturerSignUpActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Close the current activity
                })
                .show();
    }

    private boolean validateName(String name) {
        if (name.isEmpty()) {
            nameinputlayout.setError("Name is required!");
            return false;
        } else {
            nameinputlayout.setError(null);
            return true;
        }
    }

    private boolean validateFaculty(String faculty) {
        if (faculty.isEmpty()) {
            facultyinputlayout.setError("Faculty is required!");
            return false;
        } else {
            facultyinputlayout.setError(null);
            return true;
        }
    }

    private boolean validateID(String l_id) {
        if (l_id.isEmpty()) {
            idinputlayout.setError("Lecturer ID is required!");
            return false;
        } else if (l_id.length() > 8) {
            passwordinputlayout.setError("Lecturer ID cannot exceed 8 characters");
            Toast.makeText(this, "Lecturer ID cannot exceed 8 characters!", Toast.LENGTH_SHORT).show();
            passwordinput.requestFocus();
            return false;
        }  else {
            idinputlayout.setError(null);
            return true;
        }
    }


    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            emailinputlayout.setError("Email address is required!");
            Toast.makeText(this, "Email address is required!", Toast.LENGTH_SHORT).show();
            emailinput.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailinputlayout.setError("Email address is invalid!");
            Toast.makeText(this, "Email address is invalid!", Toast.LENGTH_SHORT).show();
            emailinput.requestFocus();
            return false;
        } else {
            emailinputlayout.setError(null);
            return true;
        }
    }



    private boolean validatePassword(String password) {
        if (password.isEmpty()) {
            passwordinputlayout.setError("Password is required!");
            Toast.makeText(this, "Password is required!", Toast.LENGTH_SHORT).show();
            passwordinput.requestFocus();
            return false;
        } else if (password.length() < 6) {
            passwordinputlayout.setError("Password should be at least 6 characters!");
            Toast.makeText(this, "Password should be at least 6 characters!", Toast.LENGTH_SHORT).show();
            passwordinput.requestFocus();
            return false;
        } else {
            passwordinputlayout.setError(null);
            return true;
        }
    }

    private boolean validatePasswordConfirmation(String password, String passwordConfirm) {
        if (passwordConfirm.isEmpty()) {
            confirminputlayout.setError("Confirm password is required!");
            Toast.makeText(this, "Confirm password is required!", Toast.LENGTH_SHORT).show();
            confirminput.requestFocus();
            return false;
        } else if (!passwordConfirm.equals(password)) {
            confirminputlayout.setError("Both passwords must be the same");
            Toast.makeText(this, "Both passwords must be the same", Toast.LENGTH_SHORT).show();
            confirminput.requestFocus();
            return false;
        } else {
            confirminputlayout.setError(null);
            return true;
        }
    }

    private boolean validateCheckBox() {
        if (!checkBox.isChecked()) {
            // If the checkbox is not checked, show an error message
            Toast.makeText(this, "Please agree to the terms and conditions.", Toast.LENGTH_SHORT).show();
            return false; // Validation failed
        } else {
            checkBox.setFocusable(true);
            return true; // Validation passed
        }
    }

}

//        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == Activity.RESULT_OK) {
//                        Intent data = result.getData();
//                        assert data != null;
//                        uri = data.getData();
//                        upload_pics.setImageURI(uri);
//                    } else {
//                        Toast.makeText(LecturerSignUpActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
//                    }
//                }
//        );