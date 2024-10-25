package com.cscorner.buddyin;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class BuddyChatAdapter extends RecyclerView.Adapter<BuddyChatViewHolder>{

    private final Context context;
    private final List<UserModel> userModelList;



    public BuddyChatAdapter(Context context, List<UserModel> userModelList) {
        this.context = context;
        this.userModelList = userModelList;
    }

    @NonNull
    @Override
    public BuddyChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_buddy_item, parent, false);
        return new BuddyChatViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BuddyChatViewHolder holder, int position) {
        Glide.with(context).load(userModelList.get(position).getProfile_image()).into(holder.profileImage);
        holder.profile_name.setText(userModelList.get(position).getUsername());

        // Get the current user ID and buddy's user ID
        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String buddyUserID = userModelList.get(position).getUser_id();
        String senderRoom = currentUserID + buddyUserID;

        // Call the function to count unread messages
        countUnreadMessages(currentUserID, buddyUserID, holder);

        holder.my_buddyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("userID", userModelList.get(holder.getAdapterPosition()).getUser_id());
                intent.putExtra("key", userModelList.get(holder.getAdapterPosition()).getKey());

                // Mark messages as read when opening the chat
                DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("Buddies")
                        .child("Chats").child(senderRoom).child("Messages");

                messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                            String receiverID = messageSnapshot.child("receiver").getValue(String.class);
                            if (receiverID != null && receiverID.equals(currentUserID)) {
                                messageSnapshot.getRef().child("readed").setValue(true);  // Mark as read
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error marking messages as read: " + error.getMessage());
                    }
                });

                context.startActivity(intent);
            }
        });

        // Reference to the chat in Firebase (adjust the path according to your structure)
        DatabaseReference lastMessageRef = FirebaseDatabase.getInstance().getReference("Buddies")
                .child("Chats").child(senderRoom).child("Messages")
                .orderByKey()
                .limitToLast(1).getRef();  // Fetch the last message

        // Fetch the last message
        lastMessageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                        String messageType = messageSnapshot.child("type").getValue(String.class);
                        String messageText = messageSnapshot.child("message").getValue(String.class);
                        String senderID = messageSnapshot.child("sender").getValue(String.class);
                        String lastMessage;

                        // Check if the last message was an image
                        if ("image".equals(messageType)) {
                            lastMessage = "[image]";
                        } else {
                            lastMessage = messageText;
                        }

                        // If the sender is the current user, prepend "You: " to the message
                        if (senderID != null && senderID.equals(currentUserID)) {
                            holder.lastseen.setText("You: " + lastMessage);
                        } else {
                            holder.lastseen.setText(lastMessage);
                        }
                    }
                } else {
                    holder.lastseen.setText("No messages yet");  // Default text if no messages exist
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.lastseen.setText("Error loading last message");
            }
        });
    }

    private void countUnreadMessages(String currentUserID, String buddyUserID, BuddyChatViewHolder holder) {
        String senderRoom = currentUserID + buddyUserID;
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("Buddies")
                .child("Chats").child(senderRoom).child("Messages");

        // Listen for unread messages where the receiver is the current user
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unreadCount = 0;
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    String receiverID = messageSnapshot.child("receiver").getValue(String.class);
                    Boolean isRead = messageSnapshot.child("readed").getValue(Boolean.class);

                    // Count if the receiver is the current user and message is unread
                    if (receiverID != null && receiverID.equals(currentUserID) && Boolean.FALSE.equals(isRead)) {
                        unreadCount++;
                    }
                }

                // Update unread count in the UI
                if (unreadCount > 0) {
                    holder.num.setText(String.valueOf(unreadCount));
                    holder.num.setVisibility(View.VISIBLE);
                } else {
                    holder.num.setVisibility(View.GONE);  // Hide the count when no unread messages
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching unread messages: " + error.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (userModelList.size());
    }
}

class BuddyChatViewHolder extends RecyclerView.ViewHolder {
    TextView profile_name,lastseen,num;
    CardView my_buddyCard;
    ImageView profileImage;

    public BuddyChatViewHolder(@NonNull View itemView) {
        super(itemView);
        profile_name = itemView.findViewById(R.id.profile_name);
        lastseen = itemView.findViewById(R.id.last_chat);
        my_buddyCard = itemView.findViewById(R.id.mybuddy_card);
        profileImage = itemView.findViewById(R.id.profileImage);
        num = itemView.findViewById(R.id.num);
    }
}