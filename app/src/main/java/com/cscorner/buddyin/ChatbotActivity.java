package com.cscorner.buddyin;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cscorner.buddyin.databinding.ActivityChatbotBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Dispatchers;


public class ChatbotActivity extends AppCompatActivity {


    TextView profile_name, no_data_text;
    EditText sendmsg_et;
    ImageButton send_btn;
    RecyclerView chatbot_rv;
    String currentuid;
    boolean notify = false;

    List<ChatbotModel> chatbotModelList;
    ChatbotAdapter chatbotadapter;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chatbot);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.chatbot_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        // Enable the Up button (back button)
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Adjust the keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        profile_name = findViewById(R.id.profile_name);
        sendmsg_et = findViewById(R.id.sendmsg_et);
        send_btn = findViewById(R.id.send_btn);
        no_data_text = findViewById(R.id.no_data_text);
        currentuid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        no_data_text.setVisibility(View.VISIBLE);

        chatbot_rv = findViewById(R.id.chatbot_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        chatbot_rv.setLayoutManager(linearLayoutManager);

        chatbotModelList = new ArrayList<>();
        chatbotadapter = new ChatbotAdapter(this, chatbotModelList);
        chatbot_rv.setAdapter(chatbotadapter);

        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().
                getReference("Chatbot").child("Chat").child(currentuid).child("Messages");


        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatbotModelList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    ChatbotModel messages = snapshot1.getValue(ChatbotModel.class);
                    chatbotModelList.add(messages);
                }

                chatbotadapter.setChatbotModelList(chatbotModelList);
                chatbotadapter.notifyDataSetChanged();

                // Check if there are messages
                if (chatbotModelList.isEmpty()) {
                    no_data_text.setVisibility(View.VISIBLE);  // Show 'no data' text if no messages
                } else {
                    no_data_text.setVisibility(View.GONE);  // Hide 'no data' text if there are messages
                    chatbot_rv.scrollToPosition(chatbotModelList.size() - 1);  // Scroll to the latest message
                }

                Log.d("ChatBotActivity", "Number of messages: " + chatbotModelList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error: " + error.getMessage());
            }
        });




        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String message = sendmsg_et.getText().toString().trim();
                if (TextUtils.isEmpty(message)) {//if empty
                    Toast.makeText(ChatbotActivity.this, "Please Write Something Here", Toast.LENGTH_LONG).show();
                } else {
                    sendmessage(message);
                }
                sendmsg_et.setText("");
            }
        });

    }

    // Send message text
    private void sendmessage(String message) {
        // Instantiate GeminiPro model
        GeminiPro model = new GeminiPro();

        // Get current timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());

        // User's message data (type is "user")
        String type = "user";
        ChatbotModel userMessage = new ChatbotModel(message, type, timestamp);

        // Disable the send button while waiting for the bot's response
        send_btn.setEnabled(false);
        sendmsg_et.setHint("AI Buddy Is Answering Your Question...");

        // Save the user's message to Firebase
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference chatRef = firebaseDatabase.getReference("Chatbot").child("Chat").child(currentuid).child("Messages");

        // Push the user's message
        chatRef.push().setValue(userMessage).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Send the message to the chatbot and get the response
                model.getResponse(message, new ResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        // Bot's message data (type is "bot")
                        String type = "bot";
                        ChatbotModel botMessage = new ChatbotModel(response, type, String.valueOf(System.currentTimeMillis()));

                        // Save the bot's response to Firebase
                        chatRef.push().setValue(botMessage).addOnCompleteListener(botTask -> {
                            if (botTask.isSuccessful()) {
                                send_btn.setEnabled(true);
                                sendmsg_et.setHint("Enter your question here");
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        // Handle the error and save an error message to Firebase
                        String type = "bot";
                        String errorMessage = "Sorry, I'm having trouble understanding that. Please try again.";
                        ChatbotModel errorResponse = new ChatbotModel(errorMessage, type, String.valueOf(System.currentTimeMillis()));

                        // Save error message to Firebase
                        chatRef.push().setValue(errorResponse).addOnCompleteListener(errorTask -> {
                            if (errorTask.isSuccessful()) {
                                send_btn.setEnabled(true);
                                sendmsg_et.setHint("Enter your question here");
                            }
                        });

                        // Show error toast
                        Toast.makeText(ChatbotActivity.this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // If saving the user message fails
                Toast.makeText(ChatbotActivity.this, "Failed to send message. Please try again.", Toast.LENGTH_SHORT).show();
                send_btn.setEnabled(true);
                sendmsg_et.setHint("Enter your question here");
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
