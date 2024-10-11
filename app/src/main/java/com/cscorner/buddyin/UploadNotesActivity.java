package com.cscorner.buddyin;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Objects;

public class UploadNotesActivity extends AppCompatActivity {

    AutoCompleteTextView subjectCode_spinner;
    TextInputEditText input_desc,input_title;
    ImageView upload_pdf;
    MaterialButton upload_btn;
    ArrayAdapter<String> adapter;
    ArrayList<String> subjectList;
    TextView profilename;
    TextInputLayout subjectlayout, desclayout, titlelayout;
    FirebaseAuth mAuth;
    DatabaseReference dbref, databaseReference;
    StorageReference storageReference;
    String PDF_url;
    Uri uri;
    String profilenameuser;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_notes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        subjectCode_spinner = findViewById(R.id.subjectCode_spinner);
        input_desc = findViewById(R.id.input_desc);
        upload_pdf = findViewById(R.id.upload_pdf);
        upload_btn = findViewById(R.id.upload_btn);
        profilename = findViewById(R.id.profile_name);
        subjectlayout = findViewById(R.id.subjectlayout);
        desclayout = findViewById(R.id.input_desclayout);
        input_title = findViewById(R.id.input_title);
        titlelayout = findViewById(R.id.input_titlelayout);

        Toolbar toolbar = findViewById(R.id.uploadN_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Upload Notes");
        // Enable the Up button (back button)
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        storageReference = FirebaseStorage.getInstance().getReference("Notes File");

        // Get Profile Name
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        checkUserRole(userId);

        subjectList = new ArrayList<>();
        adapter = new ArrayAdapter<>(UploadNotesActivity.this, android.R.layout.simple_spinner_dropdown_item, subjectList);
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

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            uri = data.getData();
                            input_title.setText(uri.getLastPathSegment());
                        }
                    } else {
                        Toast.makeText(UploadNotesActivity.this, "No PDF Selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        upload_pdf.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("application/pdf");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            activityResultLauncher.launch(Intent.createChooser(intent, "PDF FILE SELECT"));
        });
    }

    private void saveData() {
        if (validateSubject() & validateDesc()) {
            if (uri != null) {
                final String timestamp = "" + System.currentTimeMillis();
                final String messagePushID = timestamp;
                StorageReference filepath = storageReference.child(messagePushID + "." + "pdf");
                //StorageReference ref = storageReference.child(uri.getLastPathSegment()+ "." + "pdf");
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("File is Loading");
                progressDialog.show();

                filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isComplete());
                        Uri pdf_url = uriTask.getResult();
                        PDF_url = pdf_url.toString();
                        saveInfo();  // Pass the URL to saveInfo
                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadNotesActivity.this, "Error uploading file", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressDialog.setMessage("File Uploaded " + (int) progress + "%");
                    }
                });
            } else {
                Toast.makeText(this, "Please select a PDF file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveInfo() {
        String subject = subjectCode_spinner.getText().toString().trim();
        String description = input_desc.getText().toString().trim();
        String title = (input_title.getText().toString().trim()+".pdf");
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        // Reference to the "Post" node under the current user's ID
        DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference("Notes");
        // Generate a unique key for the new post
        String notes_id = notesRef.push().getKey();

//        NotesModel notesModel = new NotesModel(subject,title, description, PDF_url);
        NotesModel notesModel1 = new NotesModel(notes_id,userId,profilenameuser,subject,title,description,PDF_url);

        notesRef.child(notes_id).setValue(notesModel1).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(UploadNotesActivity.this, "Notes Uploaded Successfully", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UploadNotesActivity.this, Objects.requireNonNull(e.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });

//        //databaseReference = FirebaseDatabase.getInstance().getReference("Notes").child(subject);
//        databaseReference = FirebaseDatabase.getInstance().getReference("Notes");
//        databaseReference.push().setValue(notesModel).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()) {
//                    Toast.makeText(UploadNotesActivity.this, "Notes Uploaded Successfully", Toast.LENGTH_LONG).show();
//                    finish();
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(UploadNotesActivity.this, Objects.requireNonNull(e.getMessage()), Toast.LENGTH_SHORT).show();
//            }
//        });

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

    private void ShowData() {
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subjectList.clear(); // Clear the list here to avoid duplicates
                for (DataSnapshot item : snapshot.getChildren()) {
                    subjectList.add(Objects.requireNonNull(item.getValue()).toString());
                }
                subjectList.add("Others");
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadNotesActivity.this, "Failed to load subjects.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddSubjectDialog() {
        View view = LayoutInflater.from(UploadNotesActivity.this).inflate(R.layout.dialog_addsubject_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Subject");

        final EditText subjectCode = view.findViewById(R.id.subjectcodeinput);
        final EditText subjectName = view.findViewById(R.id.subjectnameinput);
        builder.setView(view);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String subjectCodeText = subjectCode.getText().toString().trim();
            String subjectNameText = subjectName.getText().toString().trim();
            String formattedSubject = subjectCodeText + " - " + subjectNameText;

            if (!subjectCodeText.isEmpty() && !subjectNameText.isEmpty()) {
                DatabaseReference reference = dbref.push();
                reference.setValue(formattedSubject);
            } else {
                Toast.makeText(UploadNotesActivity.this, "Both fields are required", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void checkUserRole(String userId) {
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Admin").child(userId);

        adminRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && Boolean.TRUE.equals(dataSnapshot.child("isAdmin").getValue(Boolean.class))) {

                    // Retrieve the admin details directly without using a model class
                    String name = dataSnapshot.child("username").getValue(String.class);

                    // Logging the data for debugging
                    Log.d(TAG, "Admin Name: " + name);

                    // Update the TextView with the retrieved data
                    profilename.setText(name);
                    profilenameuser = name;
                } else {
                    // Check if the user is a lecturer
                    checkLecturerStatus(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UploadNotesActivity.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkLecturerStatus(String userId) {
        DatabaseReference lecturerRef = FirebaseDatabase.getInstance().getReference("Lecturers").child(userId);

        lecturerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Lecturers").child(userId);

                    userRef.addValueEventListener(new ValueEventListener() {
                        @SuppressLint("StringFormatInvalid")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                LecturerModel userProfile = dataSnapshot.getValue(LecturerModel.class);
                                if (userProfile != null) {
                                    Log.d(TAG, "User profile retrieved: " + userProfile.getName());
                                    profilename.setText(userProfile.getName());
                                    profilenameuser = userProfile.getName();
                                }
                            } else {
                                Log.d(TAG, "User profile is null");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d(TAG, "User profile is null");
                        }
                    });
                } else {
                    // Reference to "User Info" under the current user
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("User Info").child(userId);

                    userRef.addValueEventListener(new ValueEventListener() {
                        @SuppressLint("StringFormatInvalid")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                UserModel userProfile = dataSnapshot.getValue(UserModel.class);
                                if (userProfile != null) {
                                    Log.d(TAG, "User profile retrieved: " + userProfile.getUsername());
                                    profilename.setText(userProfile.getUsername());
                                    profilenameuser = userProfile.getUsername();
                                }
                            } else {
                                Log.d(TAG, "User profile is null");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d(TAG, "User profile is null");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UploadNotesActivity.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected (@NonNull MenuItem item){
        if (item.getItemId() == android.R.id.home) {// Handle the Up button click (e.g., navigate back)
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
