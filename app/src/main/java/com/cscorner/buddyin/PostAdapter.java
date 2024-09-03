package com.cscorner.buddyin;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_POST = 0;
    private static final int VIEW_TYPE_QUESTION = 1;

    private final Context context;
    private List<PostModel> postModelList;
    private DatabaseReference userRef;

    public PostAdapter(Context context, List<PostModel> postModelList) {
        this.context = context;
        this.postModelList = postModelList;
        userRef = FirebaseDatabase.getInstance().getReference().child("User Info");
    }

    @Override
    public int getItemViewType(int position) {
        PostModel item = postModelList.get(position);
        if (item.getPost_image() != null && !item.getPost_image().equals("null")) {
            return VIEW_TYPE_POST;
        } else {
            return VIEW_TYPE_QUESTION;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_POST) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.vertical_post_item, parent, false);
            return new PostViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.vertical_question_item, parent, false);
            return new QuestionViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PostModel item = postModelList.get(position);
        String timestamp = formatTimestamp(item.getTimestamp());

        if (holder.getItemViewType() == VIEW_TYPE_POST) {
            PostViewHolder postViewHolder = (PostViewHolder) holder;
            postViewHolder.UsernameCard.setText(item.getUsername());
            postViewHolder.contextCard.setText(item.getDescription());
            Glide.with(context).load(item.getPost_image()).into(postViewHolder.postImage);
            postViewHolder.user_time.setText(timestamp);

            // Fetch user profile image
            userRef.child(item.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String profileImageUrl = snapshot.child("profile_image").getValue(String.class);
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(context).load(profileImageUrl).into(((PostViewHolder) holder).profileImage);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Failed to load user profile image.", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            QuestionViewHolder questionViewHolder = (QuestionViewHolder) holder;
            questionViewHolder.UsernameCard.setText(item.getUsername());
            questionViewHolder.contextCard.setText(item.getDescription());
            questionViewHolder.user_time.setText(timestamp);

            // Fetch user profile image
            userRef.child(item.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String profileImageUrl = snapshot.child("profile_image").getValue(String.class);
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(context).load(profileImageUrl).into(((QuestionViewHolder) holder).profileImage);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Failed to load user profile image.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String formatTimestamp(String timeStamp) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        return DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
    }

    public void setPostModelList(List<PostModel> postModelList) {
        this.postModelList = postModelList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return postModelList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImage,profileImage;
        TextView contextCard, UsernameCard,user_time,liked_textview,comment_textview;
        CardView postCard;
        ImageButton likeButton,commentButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.postImage);
            UsernameCard = itemView.findViewById(R.id.UsernameCard);
            contextCard = itemView.findViewById(R.id.contextCard);
            postCard = itemView.findViewById(R.id.postCard);
            profileImage = itemView.findViewById(R.id.profileImage);
            user_time = itemView.findViewById(R.id.user_time);
            likeButton = itemView.findViewById(R.id.likeButton);
            commentButton = itemView.findViewById(R.id.commentButton);
            liked_textview = itemView.findViewById(R.id.liked_textview);
            comment_textview = itemView.findViewById(R.id.comment_textview);

        }
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView contextCard, UsernameCard,user_time,liked_textview,comment_textview;;
        CardView postCard;
        ImageView profileImage;
        ImageButton likeButton,commentButton;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            UsernameCard = itemView.findViewById(R.id.UsernameCard);
            contextCard = itemView.findViewById(R.id.contextCard);
            postCard = itemView.findViewById(R.id.postCard);
            profileImage = itemView.findViewById(R.id.profileImage);
            user_time = itemView.findViewById(R.id.user_time);
            likeButton = itemView.findViewById(R.id.likeButton);
            commentButton = itemView.findViewById(R.id.commentButton);
            liked_textview = itemView.findViewById(R.id.liked_textview);
            comment_textview = itemView.findViewById(R.id.comment_textview);
        }
    }
}