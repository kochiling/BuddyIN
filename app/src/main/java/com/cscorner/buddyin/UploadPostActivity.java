package com.cscorner.buddyin;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
    String imageURL;
    Uri uri;



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
        //Get Profile Name
        // Get the current user ID
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
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
                            // Update the TextView with the retrieved data
                            profilename.setText(userProfile.getUsername());
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
            Intent photoPicker = new Intent(Intent.ACTION_PICK);
            photoPicker.setType("image/*");
            activityResultLauncher.launch(photoPicker);
        });
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
        String user_id = Objects.requireNonNull(dbAuth.getCurrentUser()).getUid();
        String subject = subjectCode_spinner.getText().toString().trim();
        String description = input_desc.getText().toString().trim();
        String username = profilename.getText().toString().trim();

        PostModel postModel = new PostModel(user_id,username,subject,description,imageURL);

        FirebaseDatabase.getInstance().getReference("Post").child(user_id).push()
                .setValue(postModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(UploadPostActivity.this, "Posted Successfully", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
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
}