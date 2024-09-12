package com.cscorner.buddyin;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {

    private final Context context;
    private List<CommentModel> commentModelList;  // Remove final here

    public CommentAdapter(Context context, List<CommentModel> commentModelList) {
        this.context = context;
        this.commentModelList = commentModelList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentModel comment = commentModelList.get(position);

        // Use formatted timestamp
        String formattedTimestamp = formatTimestamp(comment.getTimestamp());

        holder.profile_name.setText(comment.getUsername());
        holder.comment_word.setText(comment.getDescription());
        holder.comment_time.setText(formattedTimestamp);

        // Load the profile image
        Glide.with(context).load(comment.getUser_profile()).into(holder.profileImage);
    }

    private String formatTimestamp(String timeStamp) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        return DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
    }

    @Override
    public int getItemCount() {
        return commentModelList.size();
    }

    // Update the list and notify the adapter of changes
    public void setCommentModelList(List<CommentModel> commentModelList) {
        this.commentModelList = commentModelList;
        notifyDataSetChanged();  // Important to notify the adapter of changes
    }
}
class CommentViewHolder extends RecyclerView.ViewHolder {

    ImageView profileImage;
    TextView profile_name,comment_word,comment_time;
    CardView comment_card;

    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);
        comment_card = itemView.findViewById(R.id.comment_card);
        profile_name = itemView.findViewById(R.id.profile_name);
        comment_time = itemView.findViewById(R.id.comment_time);
        comment_word = itemView.findViewById(R.id.comment_word);
        profileImage = itemView.findViewById(R.id.profileImage);
    }
}
