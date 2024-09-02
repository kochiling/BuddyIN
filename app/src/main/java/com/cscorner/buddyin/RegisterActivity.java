package com.cscorner.buddyin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import android.content.DialogInterface;
import java.util.Collections;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText nameinput,ageinput,seniorinput,hobbiesinput,personalitiesinput;
    AutoCompleteTextView genderinput,courseinput,nationalityinput;
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


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nameinput = findViewById(R.id.nameinput);
        ageinput = findViewById(R.id.ageinput);
        seniorinput = findViewById(R.id.seniorinput);
        genderinput = findViewById(R.id.genderinput);
        courseinput = findViewById(R.id.courseinput);
        nationalityinput = findViewById(R.id.nationalityinput);
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

        //GenderType
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.genderinput, R.layout.drop_down_item);
        adapter.setDropDownViewResource(R.layout.drop_down_item);
        genderinput.setAdapter(adapter);

        genderinput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(RegisterActivity.this, genderinput.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        //CourseType
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.coursesinput, R.layout.drop_down_item);
        adapter1.setDropDownViewResource(R.layout.drop_down_item);
        courseinput.setAdapter(adapter1);

        courseinput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(RegisterActivity.this, courseinput.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        //NationalType
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.nationality, R.layout.drop_down_item);
        adapter2.setDropDownViewResource(R.layout.drop_down_item);
        nationalityinput.setAdapter(adapter2);

        nationalityinput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(RegisterActivity.this, nationalityinput.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        //HobbiesType
        hobbiesArray = getResources().getStringArray(R.array.hobbies);
        // initialize selected array
        selectedhobbies = new boolean[hobbiesArray.length];

        hobbiesinput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Initialize alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
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
                                Toast.makeText(RegisterActivity.this, "You can select up to 3 personalities only", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(RegisterActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
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
                        RegisterActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void saveData() {
        if (validateName() & validateAge() & validateGender() & validateSenior() & validateCourse() & validateNationality() & validateHobbies() & validatePersonality() ) {
            if (uri != null) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("User Profile Pics")
                        .child(Objects.requireNonNull(uri.getLastPathSegment()));
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
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
                Uri urlImage = Uri.parse("https://firebasestorage.googleapis.com/v0/b/buddyin-70.appspot.com/o/profile_image.jpg?alt=media&token=eca1a67d-9802-4a9d-a8bc-515947abf84b");
                imageURL = urlImage.toString();
                saveInfo();
            }
        } else {
            Toast.makeText(RegisterActivity.this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveInfo() {
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
        String nationality = nationalityinput.getText().toString().trim();

        //refer back to above string name
        KNNDataInfoModel knnDataInfoModel = new KNNDataInfoModel(user_id,name,courses,senior,hobbies,personality);
        UserModel userModel = new UserModel(name,age,gender,nationality,imageURL,user_id);

        FirebaseDatabase.getInstance().getReference("KNN Data Information").child(user_id)
                .setValue(knnDataInfoModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Saved for KNN", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, Objects.requireNonNull(e.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                });

        FirebaseDatabase.getInstance().getReference("User Info").child(user_id)
                .setValue(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Saved for Profile", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, Objects.requireNonNull(e.getMessage()), Toast.LENGTH_SHORT).show();
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
            seniorinputlayout.setError("Senior input is required!");
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
        String nationality = nationalityinput.getText().toString().trim();
        if (nationality.isEmpty()) {
            nationalinputlayout.setError("Nationality is required!");
            return false;
        } else {
            nationalinputlayout.setError(null);
            return true;
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


}