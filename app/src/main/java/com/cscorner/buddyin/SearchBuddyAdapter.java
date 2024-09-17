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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchBuddyAdapter extends RecyclerView.Adapter<SearchBuddyViewHolder> {

    private final Context context;
    private List<UserModel> userModelList;

    public SearchBuddyAdapter(Context context, List<UserModel> userModelList) {
        this.context = context;
        this.userModelList = userModelList;
    }

    @NonNull
    @Override
    public SearchBuddyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_buddy_item, parent, false);
        return new SearchBuddyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchBuddyViewHolder holder, int position) {
        // Load profile image and set the buddy's name
        Glide.with(context).load(userModelList.get(position).getProfile_image()).into(holder.profileImage);
        holder.name.setText(userModelList.get(position).getUsername());

        // Get the current user's ID and the buddy's ID
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        String buddyId = userModelList.get(holder.getAdapterPosition()).getKey();

        // Define the references for match, friend requests (sent and received), and friends
        DatabaseReference matchRef = FirebaseDatabase.getInstance().getReference("Buddies").child("MatchResults").child(currentUserId).child(buddyId);
        DatabaseReference requestSentRef = FirebaseDatabase.getInstance().getReference("Buddies").child("friend_requests").child(currentUserId).child("sent").child(buddyId);
        DatabaseReference requestReceivedRef = FirebaseDatabase.getInstance().getReference("Buddies").child("friend_requests").child(currentUserId).child("received").child(buddyId);
        DatabaseReference friendRef = FirebaseDatabase.getInstance().getReference("Buddies").child("friend").child(currentUserId).child("list").child(buddyId);

        // Handle card click event
        holder.rec_card.setOnClickListener(v -> {
            // First, check if the buddy is in match results
            matchRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot matchSnapshot) {
                    boolean isMatch = matchSnapshot.exists() && Boolean.TRUE.equals(matchSnapshot.getValue(Boolean.class));

                    // Check for friend requests (sent or received)
                    requestSentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot sentSnapshot) {
                            boolean isSentRequest = sentSnapshot.exists(); // Check if the request was sent

                            requestReceivedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot receivedSnapshot) {
                                    boolean isReceivedRequest = receivedSnapshot.exists(); // Check if there's a received request

                                    // Now, check if the buddy is a friend
                                    friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot friendSnapshot) {
                                            boolean isFriend = friendSnapshot.exists(); // Check if they're already friends

                                            // Logic to determine the correct page based on buddy status
                                            Intent intent;
                                            if (isFriend) {
                                                // Show in Buddy Chat Page
                                                intent = new Intent(context, NewBuddyProfileActivity.class);
                                            } else if (isReceivedRequest) {
                                                // Show in Request Page for received friend requests
                                                intent = new Intent(context, RequestBuddyProfileActivity.class);
                                            } else if (isSentRequest) {
                                                // Show in a page for sent friend requests (if you have one)
                                                intent = new Intent(context, NewBuddyProfileActivity.class);
                                            } else if (isMatch) {
                                                // Show in Recommend 2 Page if matched but not friends
                                                intent = new Intent(context,BuddyProfileActivity.class);
                                            } else {
                                                // Show in Recommend Page if neither matched nor friends
                                                intent = new Intent(context, BuddyProfileActivity.class);
                                            }

                                            // Pass the necessary buddy data to the intent
                                            intent.putExtra("userID", userModelList.get(holder.getAdapterPosition()).getUser_id());
                                            intent.putExtra("key", userModelList.get(holder.getAdapterPosition()).getKey());
                                            context.startActivity(intent);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e("SearchBuddyAdapter", "Error checking friend status: " + error.getMessage());
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("SearchBuddyAdapter", "Error checking received friend request: " + error.getMessage());
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("SearchBuddyAdapter", "Error checking sent friend request: " + error.getMessage());
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("SearchBuddyAdapter", "Error checking match status: " + error.getMessage());
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return (userModelList.size());
    }

    public void setSearchBuddyList(List<UserModel> userModelList) {
        this.userModelList = userModelList;
        notifyDataSetChanged();
    }

}

class SearchBuddyViewHolder extends RecyclerView.ViewHolder {
    TextView name;
    CardView rec_card;
    ImageView profileImage;

    public SearchBuddyViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.profile_name);
        rec_card = itemView.findViewById(R.id.rec_Card);
        profileImage = itemView.findViewById(R.id.profileImage);
    }
}
