package com.cscorner.buddyin;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import android.Manifest;

public class ChatActivity extends AppCompatActivity {

    ImageView profileImage;
    TextView profile_name, status;
    String key = "";
    String userID = "";
    EditText sendmsg_et;
    ImageButton attach_btn, send_btn;
    RecyclerView chatbot_rv;

    private static final int IMAGEPICK_GALLERY_REQUEST = 300;
    private static final int IMAGE_PICKCAMERA_REQUEST = 400;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    String[] cameraPermission;
    String[] storagePermission;
    Uri imageuri = null;
    boolean notify = false;
    String currentuid, image;

    List<ChatModel> chatModelList;
    ChatBubbleAdapter chatadapter;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        // Enable the Up button (back button)
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Get buddy id
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            key = bundle.getString("key");
            Log.d(TAG, "key is" + Objects.requireNonNull(key));
            userID = bundle.getString("userID");
        }

        profileImage = findViewById(R.id.profileImage);
        profile_name = findViewById(R.id.profile_name);
        sendmsg_et = findViewById(R.id.sendmsg_et);
        send_btn = findViewById(R.id.send_btn);
        attach_btn = findViewById(R.id.attach_btn);
        status = findViewById(R.id.status);

        chatbot_rv = findViewById(R.id.chatbot_rv);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        chatbot_rv.setLayoutManager(linearLayoutManager);

        chatModelList = new ArrayList<>();
        chatadapter = new ChatBubbleAdapter(this, chatModelList);
        chatbot_rv.setAdapter(chatadapter);

        currentuid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        Log.d("ChatActivity","current uid for chat"+currentuid);

        String senderRoom = currentuid + key;
        Log.d("ChatActivity","Send DB"+ senderRoom);

        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Buddies")
                .child("Chats").child(senderRoom).child("Messages");

        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatModelList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    ChatModel messages = snapshot1.getValue(ChatModel.class);
                    chatModelList.add(messages);

                }
                chatadapter.setChatModelList(chatModelList);
                chatadapter.notifyDataSetChanged();
                Log.d("ChatActivity", "Number of messages: " + chatModelList.size());
                chatbot_rv.scrollToPosition(chatModelList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error: " + error.getMessage());
            }
        });

        //readMessages();

        //Get Buddy Profile image and name
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User Info").child(key);
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
                            Glide.with(ChatActivity.this)
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



        //Fetch the status
        DatabaseReference statusReferences = FirebaseDatabase.getInstance().getReference("Buddies")
                .child("status").child(key);
        statusReferences.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Ensure data is not null and cast properly
                    String onlinestatus = (String) dataSnapshot.child("onlineStatus").getValue();
                    String typingto = (String) dataSnapshot.child("typingTo").getValue();

                    Log.d(TAG, "Fetched onlineStatus: " + onlinestatus);
                    Log.d(TAG, "Fetched typingTo: " + typingto);

                    // Handle cases when values are null or empty
                    if (typingto != null && typingto.equals(currentuid)) {
                        status.setText("Typing....");
                    } else {
                        if ("online".equals(onlinestatus)) {
                            status.setText(onlinestatus);
                        } else {
                            if (onlinestatus == null || onlinestatus.equals("null") || onlinestatus.isEmpty()) {
                                status.setText("Last Seen: Unknown");
                            } else {
                                try {
                                    long onlineTime = Long.parseLong(onlinestatus);
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTimeInMillis(onlineTime);
                                    String timedate = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                                    status.setText("Last Seen: " + timedate);
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Failed to parse online status timestamp", e);
                                    status.setText("Last Seen: Unknown");
                                }
                            }
                        }
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

        //Adjust the keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String message = sendmsg_et.getText().toString().trim();
                if (TextUtils.isEmpty(message)) {//if empty
                    Toast.makeText(ChatActivity.this, "Please Write Something Here", Toast.LENGTH_LONG).show();
                } else {
                    sendmessage(message);
                }
                sendmsg_et.setText("");
            }
        });

        // initialising permissions
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        attach_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicDialog();
            }
        });

        profile_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewBuddyProfileActivity.class);
                intent.putExtra("key", key);
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewBuddyProfileActivity.class);
                intent.putExtra("key", key);
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
        });

    }

    private void readMessages() {
        String senderRoom = currentuid + key;

        // Reference to the sender's messages
        DatabaseReference senderReference = FirebaseDatabase.getInstance().getReference("Buddies")
                .child("Chats").child(senderRoom).child("Messages");

        // Listen for changes in messages from the sender's room
        senderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot msgSnapshot : snapshot.getChildren()) {
                    ChatModel chatModel = msgSnapshot.getValue(ChatModel.class);

                    // Check if the current user is the sender
                    if (chatModel != null && chatModel.getSender().equals(currentuid)) {
                        // Do not mark as read for the sender
                        continue; // Skip to the next message
                    }

                    // Check if the message is unread and if the receiver is current user
                    if (chatModel != null && !chatModel.isReaded() && chatModel.getReceiver().equals(currentuid)) {
                        // Update the message to set it as read in the receiver's room
                        msgSnapshot.getRef().child("readed").setValue(true);
                        Log.e("ChatActivity","readed by sender room "+ chatModel.getReceiver()+ " sender "+ chatModel.getSender());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error reading sender messages: " + error.getMessage());
            }
        });
    }

    private void showImagePicDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
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
                    sendImageMessage(imageuri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (requestCode == IMAGE_PICKCAMERA_REQUEST) {
                try {
                    sendImageMessage(imageuri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
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
        boolean result = ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        requestPermissions(storagePermission, STORAGE_REQUEST);
    }

    private void sendImageMessage(Uri imageuri) throws IOException {
        notify = true;
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Sending Image");
        dialog.show();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String senderRoom = currentuid + key;
        String receiverRoom = key + currentuid;
        boolean delivered = true;
        boolean read = false;
        String type = "image";

        String filePathAndName = "ChatImages/" + "post" + timestamp; // filename
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageuri);
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, arrayOutputStream); // compressing the image using bitmap
        final byte[] data = arrayOutputStream.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference("Chat Images").child(filePathAndName);

        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                dialog.dismiss();
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                uriTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            String downloadUri = task.getResult().toString(); // Getting URL if task is successful
                            ChatModel message = new ChatModel(downloadUri, read, type, currentuid, key, timestamp, delivered);

                            FirebaseDatabase fdb = FirebaseDatabase.getInstance();

                            fdb.getReference("Buddies").child("Chats").child(senderRoom).child("Messages")
                                    .push().setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                fdb.getReference("Buddies").child("Chats").child(receiverRoom).child("Messages")
                                                        .push().setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                // You can add more functionality here if needed
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(ChatActivity.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    // Send message text
    private void sendmessage(String message) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String senderroom = currentuid + key;
        String receiverroom = key + currentuid;
        boolean delivered = true;
        boolean readed = false;
        String type = "text";

        ChatModel messages = new ChatModel( message, readed, type, currentuid, key, timestamp, delivered);

        FirebaseDatabase firebaseDatabase= FirebaseDatabase.getInstance();
        firebaseDatabase.getReference("Buddies").child("Chats")
                .child(senderroom)
                .child("Messages")
                .push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        firebaseDatabase.getReference("Buddies")
                                .child("Chats")
                                .child(receiverroom)
                                .child("Messages")
                                .push()
                                .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                    }
                                });
                    }
                });

    }

    // Check online status
    @Override
    protected void onPause() {
        super.onPause();
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

    private void checkOnlineStatus(String status) {
        // check online status
        DatabaseReference statusReferences = FirebaseDatabase.getInstance().getReference("Buddies")
                .child("status").child(currentuid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        statusReferences.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing) {
        DatabaseReference statusReferences = FirebaseDatabase.getInstance().getReference("Buddies")
                .child("status").child(currentuid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        statusReferences.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }

    private void checkUserStatus() {
        currentuid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }

    // Override onOptionsItemSelected to handle the Up button click
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {// Handle the Up button click (e.g., navigate back)
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}