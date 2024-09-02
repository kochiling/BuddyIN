package com.cscorner.buddyin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        assert data != null;
                        uri = data.getData();
                        upload_pics.setImageURI(uri);
                    } else {
                        Toast.makeText(LecturerSignUpActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        upload_pics.setOnClickListener(view -> {
            Intent photoPicker = new Intent(Intent.ACTION_PICK);
            photoPicker.setType("image/*");
            activityResultLauncher.launch(photoPicker);
        });
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
        // Get input values
        progressBar.setVisibility(View.VISIBLE);
        String email = emailinput.getText().toString().trim();
        String password = passwordinput.getText().toString().trim();
        String confirmPassword = confirminput.getText().toString().trim();
        String name = nameinput.getText().toString().trim();
        String faculty = facultyinput.getText().toString().trim();
        String l_id = idinput.getText().toString().trim();

        // Validate all inputs
        if (!validateName(name) | !validateFaculty(faculty) | !validateID(l_id) | !validateEmail(email) | !validatePassword(password) | !validatePasswordConfirmation(password, confirmPassword)) {
            progressBar.setVisibility(View.GONE);
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
                        Toast.makeText(LecturerSignUpActivity.this, "Failed to register: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void uploadImageAndSaveUserData() {
        // Upload image to Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Lecturer Profile Pics")
                .child(Objects.requireNonNull(uri.getLastPathSegment()));

        storageReference.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isComplete());
                    Uri urlImage = uriTask.getResult();
                    imageURL = urlImage.toString();
                    saveUserInfoToDatabase();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
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
        } else {
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

}