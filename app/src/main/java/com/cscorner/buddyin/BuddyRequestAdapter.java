package com.cscorner.buddyin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Objects;

public class BuddyRequestAdapter extends RecyclerView.Adapter<BuddyRequestViewHolder> {
    private final Context context;
    private final List<UserModel> userModelList;

    public BuddyRequestAdapter(Context context, List<UserModel> userModelList) {
        this.context = context;
        this.userModelList = userModelList;
    }

    @NonNull
    @Override
    public BuddyRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_buddy_item, parent, false);
        return new BuddyRequestViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BuddyRequestViewHolder holder, int position) {
        Glide.with(context).load(userModelList.get(position).getProfile_image()).into(holder.profileImage);
        holder.name.setText(userModelList.get(position).getUsername());

        holder.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                String requestUserId = userModelList.get(holder.getAdapterPosition()).getKey();

                // References for the current user's requests
                DatabaseReference userReceivedRef = FirebaseDatabase.getInstance().getReference("Buddies").child("friend_requests")
                        .child(currentUserId)
                        .child("received")
                        .child(requestUserId);

                DatabaseReference userSentRef = FirebaseDatabase.getInstance().getReference("Buddies").child("friend_requests")
                        .child(requestUserId)
                        .child("sent")
                        .child(currentUserId);

                // References for adding to the friend list
                DatabaseReference currentUserFriendListRef = FirebaseDatabase.getInstance().getReference("Buddies").child("friend")
                        .child(currentUserId)
                        .child("list")
                        .child(requestUserId);

                DatabaseReference requestUserFriendListRef = FirebaseDatabase.getInstance().getReference("Buddies").child("friend")
                        .child(requestUserId)
                        .child("list")
                        .child(currentUserId);

                // Update friend request status and add to friend list
                userReceivedRef.setValue(false).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userSentRef.setValue(false).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                // Successfully updated friend requests, now add to friend lists
                                currentUserFriendListRef.setValue(true);
                                requestUserFriendListRef.setValue(true);

                                // Show success message
                                Toast.makeText(v.getContext(), "Buddy request accepted", Toast.LENGTH_SHORT).show();
                            } else {
                                // Failed to update sent request
                                Toast.makeText(v.getContext(), "Failed to update request", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Failed to update received request
                        Toast.makeText(v.getContext(), "Failed to update request", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        holder.rec_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BuddyProfileActivity.class);
                intent.putExtra("userID",userModelList.get(holder.getAdapterPosition()).getUser_id());
                intent.putExtra("key",userModelList.get(holder.getAdapterPosition()).getKey());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (userModelList.size());
    }
}
class BuddyRequestViewHolder extends RecyclerView.ViewHolder {
    TextView name;
    CardView rec_card;
    ImageView profileImage;
    Button send, remove;

    public BuddyRequestViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.profile_name);
        send = itemView.findViewById(R.id.add_btn);
        remove = itemView.findViewById(R.id.remove_btn);
        rec_card = itemView.findViewById(R.id.rec_Card);
        profileImage = itemView.findViewById(R.id.profileImage);
    }
}
