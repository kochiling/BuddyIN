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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.List;
import java.util.Objects;

public class BuddyRecommendAdapter extends RecyclerView.Adapter<BuddyRecommendViewHolder> {

    private final Context context;
    private List<UserModel> userModelList;
    //private final List<KNNDataInfoModel> knnDataInfoModelList;

    public BuddyRecommendAdapter(Context context, List<UserModel> userModelList) {
        this.context = context;
        this.userModelList = userModelList;
    }

    @NonNull
    @Override
    public BuddyRecommendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommendation_buddy_item, parent, false);
        return new BuddyRecommendViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BuddyRecommendViewHolder holder, int position) {
        Glide.with(context).load(userModelList.get(position).getProfile_image()).into(holder.profileImage);
        holder.name.setText(userModelList.get(position).getUsername());

        holder.rec_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BuddyProfileActivity.class);
                intent.putExtra("userID",userModelList.get(holder.getAdapterPosition()).getUser_id());
                intent.putExtra("key",userModelList.get(holder.getAdapterPosition()).getKey());
                context.startActivity(intent);
            }
        });

        holder.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                String requestUserId = userModelList.get(holder.getAdapterPosition()).getKey();

                // References for the current user's requests
                DatabaseReference userReceivedRef = FirebaseDatabase.getInstance().getReference("Buddies").child("friend_requests")
                        .child(userId)
                        .child("sent")
                        .child(requestUserId);

                DatabaseReference targetUserSentRef = FirebaseDatabase.getInstance().getReference("Buddies").child("friend_requests")
                        .child(requestUserId)
                        .child("received")
                        .child(userId);

                // Set values for both users
                userReceivedRef.setValue(true);
                targetUserSentRef.setValue(true);

                // Reference to the MatchResults node for the current user
                DatabaseReference matchResultsRef = FirebaseDatabase.getInstance().getReference("Buddies").child("MatchResults").child( userId );

                matchResultsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean hasUpdates = false; // Flag to check if any update is done

                        // Loop through the match results
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String userId = snapshot.getKey(); // User ID in the match results
                            Boolean isMatched = snapshot.getValue(Boolean.class); // Match result

                            // Check if the value is true and the user ID matches the target user ID
                            if (Boolean.TRUE.equals(isMatched) && requestUserId.equals(userId)) {
                                // Update the value to false
                                matchResultsRef.child(userId).setValue(false);
                                hasUpdates = true;
                                int positionToRemove = holder.getAdapterPosition();
                                userModelList.remove(positionToRemove);
                                notifyItemRemoved(positionToRemove);
                            }
                        }

                        // Optionally update the target user's match results as well
                        if (hasUpdates) {
                            DatabaseReference targetUserMatchResultsRef = FirebaseDatabase.getInstance().getReference("Buddies").child("MatchResults").child(requestUserId);
                            targetUserMatchResultsRef.child(userId).setValue(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors here
                    }
                });
                // Optional: Add a Toast or Log to confirm the request was sent
                Toast.makeText(v.getContext(), "Friend request sent", Toast.LENGTH_SHORT ).show();
            }
        });

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                String requestUserId = userModelList.get(holder.getAdapterPosition()).getKey();

                // Reference to the MatchResults node for the current user
                DatabaseReference matchResultsRef = FirebaseDatabase.getInstance().getReference("Buddies").child("MatchResults").child( userId );

                matchResultsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Flag to check if any update is done

                        // Loop through the match results
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String userId = snapshot.getKey(); // User ID in the match results
                            Boolean isMatched = snapshot.getValue(Boolean.class); // Match result

                            // Check if the value is true and the user ID matches the target user ID
                            if (Boolean.TRUE.equals(isMatched) && requestUserId.equals(userId)) {
                                // Update the value to false
                                matchResultsRef.child(userId).setValue(false);
                                Toast.makeText(context, "Remove Recommend", Toast.LENGTH_SHORT).show();
                                int positionToRemove = holder.getAdapterPosition();
                                userModelList.remove(positionToRemove);
                                notifyItemRemoved(positionToRemove);

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {


                    }
                });

            }
        });

    }

    @Override
    public int getItemCount() {
        return (userModelList.size());
    }

    public void setRecommendList(List<UserModel> userModelList) {
        this.userModelList = userModelList;
        notifyDataSetChanged();
    }
}

class BuddyRecommendViewHolder extends RecyclerView.ViewHolder {
    TextView name;
    CardView rec_card;
    ImageView profileImage;
    Button send, remove;

    public BuddyRecommendViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.profile_name);
        send = itemView.findViewById(R.id.add_btn);
        remove = itemView.findViewById(R.id.remove_btn);
        rec_card = itemView.findViewById(R.id.rec_Card);
        profileImage = itemView.findViewById(R.id.profileImage);
    }
}
