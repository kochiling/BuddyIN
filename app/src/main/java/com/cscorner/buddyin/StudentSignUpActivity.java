package com.cscorner.buddyin;

import android.Manifest;
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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

public class StudentSignUpActivity extends AppCompatActivity {

    TextInputEditText emailinput,passwordinput,confirminput;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextInputLayout emailinputlayout,passwordinputlayout, confirminputlayout;

    TextInputEditText nameinput,ageinput,seniorinput,hobbiesinput,personalitiesinput;
    AutoCompleteTextView genderinput,courseinput;
    CountryCodePicker nationalityinput;

    CheckBox checkBox;
    Button registerbtn;
    ImageView upload_pics;
    String imageURL;
    Uri uri;
    TextInputLayout nameinputlayout,ageinputlayout,seniorinputlayout,hobbiesinputlayout,
            personalitiesinputlayout,genderinputlayout,courseinputlayout,nationalinputlayout;

    boolean[] selectedhobbies;
    ArrayList<Integer> hobbiesList = new ArrayList<>();
    String[] hobbiesArray;

    boolean[] selectedpersonalites;
    ArrayList<Integer> personalityList = new ArrayList<>();
    String[] personalityArray;

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
        setContentView(R.layout.activity_student_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        emailinput = findViewById(R.id.emailinput);
        passwordinput = findViewById(R.id.passwordinput);
        confirminput = findViewById(R.id.confirminput);

        emailinputlayout = findViewById(R.id.emailinputlayout);
        passwordinputlayout = findViewById(R.id.passwordinputlayout);
        confirminputlayout = findViewById(R.id.confirminputlayout);

        nameinput = findViewById(R.id.nameinput);
        ageinput = findViewById(R.id.ageinput);
        seniorinput = findViewById(R.id.seniorinput);
        genderinput = findViewById(R.id.genderinput);
        courseinput = findViewById(R.id.courseinput);
        hobbiesinput = findViewById(R.id.hobbiesinput);
        personalitiesinput = findViewById(R.id.personalitiesinput);
        registerbtn = findViewById(R.id.registerbtn);
        upload_pics = findViewById(R.id.upload_pics);
        checkBox = findViewById(R.id.terms_checkbox);
        nameinputlayout = findViewById(R.id.nameinputlayout);
        ageinputlayout = findViewById(R.id.ageinputlayout);
        seniorinputlayout = findViewById(R.id.seniorinputlayout);
        hobbiesinputlayout = findViewById(R.id.hobbiesinputlayout);
        personalitiesinputlayout = findViewById(R.id.personalitiesinputlayout);
        genderinputlayout = findViewById(R.id.genderinputlayout);
        courseinputlayout = findViewById(R.id.courseinputlayout);
        nationalinputlayout = findViewById(R.id.nationalinputlayout);
        progressBar = findViewById(R.id.progressBar);

        //GenderType
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.genderinput, R.layout.drop_down_item);
        adapter.setDropDownViewResource(R.layout.drop_down_item);
        genderinput.setAdapter(adapter);

        genderinput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(StudentSignUpActivity.this, genderinput.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        courseinput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Course Array
                String[] courseArray = getResources().getStringArray(R.array.coursesinput);

                // Initialize alert dialog for course selection
                AlertDialog.Builder courseBuilder = new AlertDialog.Builder(StudentSignUpActivity.this);
                courseBuilder.setTitle("Select Course");

                // Create custom adapter for the list with gaps between items
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(StudentSignUpActivity.this, R.layout.drop_down_item, courseArray);

                // Set the custom adapter with items and spacing
                courseBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Set the selected course in the input field
                        courseinput.setText(courseArray[i]);
                        Toast.makeText(StudentSignUpActivity.this, courseArray[i] + " selected", Toast.LENGTH_SHORT).show();
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

         nationalityinput = findViewById(R.id.nationalityinput);

        nationalityinput.setOnCountryChangeListener(() -> {
            // Get the selected country full name
            String nationality = nationalityinput.getSelectedCountryName();

            Toast.makeText(this, "Selected Country: " + nationality, Toast.LENGTH_SHORT).show();
        });

        //HobbiesType
        hobbiesArray = getResources().getStringArray(R.array.hobbies);
        // initialize selected array
        selectedhobbies = new boolean[hobbiesArray.length];

        hobbiesinput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Initialize alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(StudentSignUpActivity.this);
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
                        hobbiesinput.setText(stringBuilder.toString());
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
                            hobbiesinput.setText("");
                        }
                    }
                });
                // show dialog
                builder.show();
            }
        });

        //Personality Spinner
        personalityArray = getResources().getStringArray(R.array.personalities);
        // initialize selected array
        selectedpersonalites = new boolean[personalityArray.length];

        personalitiesinput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Initialize alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(StudentSignUpActivity.this);
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
                                Toast.makeText(StudentSignUpActivity.this, "You can select up to 3 personalities only", Toast.LENGTH_SHORT).show();
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
                        personalitiesinput.setText(stringBuilder.toString());
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
                        personalitiesinput.setText("");
                    }
                });
                // show dialog
                builder.show();
            }
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

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });


        upload_pics.setOnClickListener(view -> {
            showImagePicDialog();
        });
    }

    private void showImagePicDialog() {
        String[] options = {"Camera", "Gallery"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(StudentSignUpActivity.this);
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
        boolean result = ContextCompat.checkSelfPermission(StudentSignUpActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        requestPermissions(storagePermission, STORAGE_REQUEST);
    }

    private void saveData() {
        progressBar.setVisibility(View.VISIBLE);
        registerbtn.setEnabled(false);
        // Get input values
        String email = emailinput.getText().toString().trim();
        String password = passwordinput.getText().toString().trim();
        String confirmPassword = confirminput.getText().toString().trim();

        // Validate all inputs
        if (!validateName() | !validateAge() | !validateGender() | !validateSenior() | !validateCourse() | !validateNationality() | !validateHobbies() | !validatePersonality()| !validateEmail(email) | !validatePassword(password) | !validatePasswordConfirmation(password, confirmPassword) | !validateCheckBox()) {
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
                        Toast.makeText(StudentSignUpActivity.this, "Failed to register: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void uploadImageAndSaveUserData() {
        // Upload image to Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("User Profile Pics")
                .child(Objects.requireNonNull(uri.getLastPathSegment()));

        AlertDialog.Builder builder = new AlertDialog.Builder(StudentSignUpActivity.this);
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
                    Toast.makeText(StudentSignUpActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserInfoToDatabase() {
        //get the user_id
        FirebaseAuth dbAuth = FirebaseAuth.getInstance();
        String user_id = Objects.requireNonNull(dbAuth.getCurrentUser()).getUid();

        String name = nameinput.getText().toString().trim();
        String age = ageinput.getText().toString().trim();
        String gender =genderinput.getText().toString().trim();
        Integer senior = Integer.valueOf(seniorinput.getText().toString().trim());
        String courses = courseinput.getText().toString().trim();
        String hobbies = hobbiesinput.getText().toString().trim();
        String personality = personalitiesinput.getText().toString().trim();
        String nationality = nationalityinput.getSelectedCountryName();

        //refer back to above string name
        KNNDataInfoModel knnDataInfoModel = new KNNDataInfoModel(user_id,name,courses,senior,hobbies,personality);
        UserModel userModel = new UserModel(name,age,gender,nationality,imageURL,user_id);

        FirebaseDatabase.getInstance().getReference("KNN Data Information").child(user_id)
                .setValue(knnDataInfoModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(StudentSignUpActivity.this, "Saved for KNN", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(StudentSignUpActivity.this, Objects.requireNonNull(e.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                });

        FirebaseDatabase.getInstance().getReference("User Info").child(user_id)
                .setValue(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(StudentSignUpActivity.this, "Saved for Profile", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(StudentSignUpActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(StudentSignUpActivity.this, Objects.requireNonNull(e.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                });

        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference statusMyRef = FirebaseDatabase.getInstance().getReference("Buddies").child("status")
                .child(currentUserId);

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", "online");
        hashMap.put("typingTo", "no one");

        statusMyRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }

    private boolean validateName() {
        String name = nameinput.getText().toString().trim();
        if (name.isEmpty()) {
            nameinputlayout.setError("Name is required!");
            return false;
        } else {
            nameinputlayout.setError(null);
            return true;
        }
    }

    private boolean validateAge() {
        String age = ageinput.getText().toString().trim();
        if (age.isEmpty()) {
            ageinputlayout.setError("Age is required!");
            return false;
        } else {
            ageinputlayout.setError(null);
            return true;
        }
    }

    private boolean validateGender() {
        String gender = genderinput.getText().toString().trim();
        if (gender.isEmpty()) {
            genderinputlayout.setError("Gender is required!");
            return false;
        } else {
            genderinputlayout.setError(null);
            return true;
        }
    }

    private boolean validateSenior() {
        String senior = seniorinput.getText().toString().trim();
        if (senior.isEmpty()) {
            seniorinputlayout.setError("Year is required!");
            return false;
        } else if (senior.length() > 1) {
            passwordinputlayout.setError("The input character cannot be more than one character");
            Toast.makeText(this, "The input character cannot be more than one character", Toast.LENGTH_SHORT).show();
            passwordinput.requestFocus();
            return false;
        } else {
            seniorinputlayout.setError(null);
            return true;
        }
    }

    private boolean validateCourse() {
        String course = courseinput.getText().toString().trim();
        if (course.isEmpty()) {
            courseinputlayout.setError("Course is required!");
            return false;
        } else {
            courseinputlayout.setError(null);
            return true;
        }
    }

    private boolean validateNationality() {
        // Use getSelectedCountryName() to retrieve the selected country's name
        String nationality = nationalityinput.getSelectedCountryName(); // Get the selected country name
        if (nationality.isEmpty()) {
            nationalinputlayout.setError("Nationality is required!");
            return false;
        } else {
            nationalinputlayout.setError(null); // Clear the error if valid
            return true; // Return true if the validation passes
        }
    }

    private boolean validateHobbies() {
        String hobbies = hobbiesinput.getText().toString().trim();
        if (hobbies.isEmpty()) {
            hobbiesinputlayout.setError("Hobbies are required!");
            return false;
        } else {
            hobbiesinputlayout.setError(null);
            return true;
        }
    }

    private boolean validatePersonality() {
        String personality = personalitiesinput.getText().toString().trim();
        if (personality.isEmpty()) {
            personalitiesinputlayout.setError("Personality is required!");
            return false;
        } else {
            personalitiesinputlayout.setError(null);
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
                        StudentSignUpActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}