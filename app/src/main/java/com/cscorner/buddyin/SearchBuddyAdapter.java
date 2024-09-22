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
        holder.rec_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the buddyId is equal to the currentUserId (i.e., user is viewing their own profile)
                if (buddyId.equals(currentUserId)) {
                    // Navigate to NewBuddyProfileActivity since it's the user's own profile
                    Intent intent = new Intent(context, NewBuddyProfileActivity.class);
                    intent.putExtra("userID", userModelList.get(holder.getAdapterPosition()).getUser_id());
                    intent.putExtra("key", userModelList.get(holder.getAdapterPosition()).getKey());
                    context.startActivity(intent);
                    return;  // Exit the method as we already handled the case
                }

                // Proceed with checking the buddy status (friend, request, match)
                friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot friendSnapshot) {
                        if (friendSnapshot.exists() && Boolean.TRUE.equals(friendSnapshot.getValue(Boolean.class))) {
                            // Buddy is a friend, navigate to NewBuddyProfileActivity
                            Intent intent = new Intent(context, NewBuddyProfileActivity.class);
                            intent.putExtra("userID", userModelList.get(holder.getAdapterPosition()).getUser_id());
                            intent.putExtra("key", userModelList.get(holder.getAdapterPosition()).getKey());
                            context.startActivity(intent);
                        } else {
                            // Check if the buddy has a sent friend request (requestSentRef)
                            requestSentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot sentSnapshot) {
                                    if (sentSnapshot.exists() && Boolean.TRUE.equals(sentSnapshot.getValue(Boolean.class))) {
                                        // Buddy is in sent friend requests, navigate to NewBuddyProfileActivity
                                        Intent intent = new Intent(context, NewBuddyProfileActivity.class);
                                        intent.putExtra("userID", userModelList.get(holder.getAdapterPosition()).getUser_id());
                                        intent.putExtra("key", userModelList.get(holder.getAdapterPosition()).getKey());
                                        context.startActivity(intent);
                                    } else {
                                        // Check if the buddy has received a friend request (requestReceivedRef)
                                        requestReceivedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot receivedSnapshot) {
                                                if (receivedSnapshot.exists() && Boolean.TRUE.equals(receivedSnapshot.getValue(Boolean.class))) {
                                                    // Buddy has received a friend request, navigate to RequestBuddyProfileActivity
                                                    Intent intent = new Intent(context, RequestBuddyProfileActivity.class);
                                                    intent.putExtra("userID", userModelList.get(holder.getAdapterPosition()).getUser_id());
                                                    intent.putExtra("key", userModelList.get(holder.getAdapterPosition()).getKey());
                                                    context.startActivity(intent);
                                                } else {
                                                    // Check the match status (matchRef)
                                                    matchRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot matchSnapshot) {
                                                            if (matchSnapshot.exists()) {
                                                                // Match found (either true or false), navigate to BuddyProfileActivity
                                                                Intent intent = new Intent(context, BuddyProfileActivity.class);
                                                                intent.putExtra("userID", userModelList.get(holder.getAdapterPosition()).getUser_id());
                                                                intent.putExtra("key", userModelList.get(holder.getAdapterPosition()).getKey());
                                                                context.startActivity(intent);
                                                            } else {
                                                                // No match, no requests, no friends, default to BuddyProfileActivity
                                                                Intent intent = new Intent(context, BuddyProfileActivity.class);
                                                                intent.putExtra("userID", userModelList.get(holder.getAdapterPosition()).getUser_id());
                                                                intent.putExtra("key", userModelList.get(holder.getAdapterPosition()).getKey());
                                                                context.startActivity(intent);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                                            Log.e("SearchBuddyAdapter", "Error checking match status: " + databaseError.getMessage());
                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Log.e("SearchBuddyAdapter", "Error checking received request: " + databaseError.getMessage());
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e("SearchBuddyAdapter", "Error checking sent request: " + databaseError.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("SearchBuddyAdapter", "Error checking friend status: " + databaseError.getMessage());
                    }
                });
            }
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
