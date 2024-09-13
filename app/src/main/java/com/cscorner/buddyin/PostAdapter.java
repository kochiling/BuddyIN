package com.cscorner.buddyin;

import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_POST = 0;
    private static final int VIEW_TYPE_QUESTION = 1;

    private final Context context;
    private List<PostModel> postModelList;
    private DatabaseReference userRef,likeRef;
    Boolean LikeChecker = false;
    String currentUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

    public PostAdapter(Context context, List<PostModel> postModelList) {
        this.context = context;
        this.postModelList = postModelList;
        userRef = FirebaseDatabase.getInstance().getReference().child("User Info");
        likeRef = FirebaseDatabase.getInstance().getReference().child("Post");
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
            Glide.with(context).load(item.getUser_profile()).into(postViewHolder.profileImage);

            postViewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LikeChecker = true;

                    likeRef.child(item.getPost_id()).child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (LikeChecker) {
                                if (snapshot.hasChild(currentUid)) {
                                    likeRef.child(item.getPost_id()).child("Likes").child(currentUid).removeValue();
                                    LikeChecker = false;
                                } else {
                                    likeRef.child(item.getPost_id()).child("Likes").child(currentUid).setValue(true);
                                    LikeChecker = false;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle error if necessary
                        }
                    });
                }
            });

            postViewHolder.setLikeButtonStatus(item);
            postViewHolder.setCommentNum(item);

            postViewHolder.postCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ViewPostDetailActivity.class);
                    intent.putExtra("userID",postModelList.get(holder.getAdapterPosition()).getUser_id());
                    intent.putExtra("username",postModelList.get(holder.getAdapterPosition()).getUsername());
                    intent.putExtra("postImage",postModelList.get(holder.getAdapterPosition()).getPost_image());
                    intent.putExtra("desc",postModelList.get(holder.getAdapterPosition()).getDescription());
                    intent.putExtra("timestamp",postModelList.get(holder.getAdapterPosition()).getTimestamp());
                    intent.putExtra("postid",postModelList.get(holder.getAdapterPosition()).getPost_id());
                    intent.putExtra("userImage",postModelList.get(holder.getAdapterPosition()).getUser_profile());
                    intent.putExtra("subject",postModelList.get(holder.getAdapterPosition()).getSubject());
                    intent.putExtra("key",postModelList.get(holder.getAdapterPosition()).getKey());
                    context.startActivity(intent);
                }
            });



        } else {
            QuestionViewHolder questionViewHolder = (QuestionViewHolder) holder;
            questionViewHolder.UsernameCard.setText(item.getUsername());
            questionViewHolder.contextCard.setText(item.getDescription());
            questionViewHolder.user_time.setText(timestamp);
            Glide.with(context).load(item.getUser_profile()).into(questionViewHolder.profileImage);

            questionViewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LikeChecker = true;

                    likeRef.child(item.getPost_id()).child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (LikeChecker) {
                                if (snapshot.hasChild(currentUid)) {
                                    likeRef.child(item.getPost_id()).child("Likes").child(currentUid).removeValue();
                                    LikeChecker = false;
                                } else {
                                    likeRef.child(item.getPost_id()).child("Likes").child(currentUid).setValue(true);
                                    LikeChecker = false;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle error if necessary
                        }
                    });
                }
            });

            questionViewHolder.setLikeButtonStatus(item);
            questionViewHolder.setCommentNum(item);

            questionViewHolder.postCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ViewPostDetailActivity.class);
                    intent.putExtra("userID",postModelList.get(holder.getAdapterPosition()).getUser_id());
                    intent.putExtra("username",postModelList.get(holder.getAdapterPosition()).getUsername());
                    intent.putExtra("postImage",postModelList.get(holder.getAdapterPosition()).getPost_image());
                    intent.putExtra("desc",postModelList.get(holder.getAdapterPosition()).getDescription());
                    intent.putExtra("timestamp",postModelList.get(holder.getAdapterPosition()).getTimestamp());
                    intent.putExtra("postid",postModelList.get(holder.getAdapterPosition()).getPost_id());
                    intent.putExtra("userImage",postModelList.get(holder.getAdapterPosition()).getUser_profile());
                    intent.putExtra("subject",postModelList.get(holder.getAdapterPosition()).getSubject());
                    intent.putExtra("key",postModelList.get(holder.getAdapterPosition()).getKey());
                    context.startActivity(intent);
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
        String currentUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference().child("Post");
        int likecounts;
        int commentCount;

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

        public void setCommentNum(PostModel item){
            likeRef.child(item.getPost_id()).child("Comments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    commentCount= (int) snapshot.getChildrenCount();
                    comment_textview.setText(Integer.toString(commentCount));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        public void setLikeButtonStatus(PostModel item) {
            likeRef.child(item.getPost_id()).child("Likes").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild(currentUid)) {
                        likecounts = (int) snapshot.getChildrenCount();
                        likeButton.setImageResource(R.drawable.round_favorite_24); // Change to the filled heart image
                        liked_textview.setText(Integer.toString(likecounts));
                    } else {
                        likecounts = (int) snapshot.getChildrenCount();
                        likeButton.setImageResource(R.drawable.rounded_favorite_24); // Change to the empty heart image
                        liked_textview.setText(Integer.toString(likecounts));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error if necessary
                }
            });
        }

    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView contextCard, UsernameCard,user_time,liked_textview,comment_textview;;
        CardView postCard;
        ImageView profileImage;
        ImageButton likeButton,commentButton;
        String currentUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference().child("Post");
        int likecounts;
        int commentCount;

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

        public void setLikeButtonStatus(PostModel item) {
            likeRef.child(item.getPost_id()).child("Likes").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild(currentUid)) {
                        likecounts = (int) snapshot.getChildrenCount();
                        likeButton.setImageResource(R.drawable.round_favorite_24); // Change to the filled heart image
                        liked_textview.setText(Integer.toString(likecounts));
                    } else {
                        likecounts = (int) snapshot.getChildrenCount();
                        likeButton.setImageResource(R.drawable.rounded_favorite_24); // Change to the empty heart image
                        liked_textview.setText(Integer.toString(likecounts));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error if necessary
                }
            });
        }

        public void setCommentNum(PostModel item){
            likeRef.child(item.getPost_id()).child("Comments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    commentCount= (int) snapshot.getChildrenCount();
                    comment_textview.setText(Integer.toString(commentCount));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }
}