package com.cscorner.buddyin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_POST = 0;
    private static final int VIEW_TYPE_QUESTION = 1;

    private final Context context;
    private List<PostModel> postModelList;

    public PostAdapter(Context context, List<PostModel> postModelList) {
        this.context = context;
        this.postModelList = postModelList;
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

        if (holder.getItemViewType() == VIEW_TYPE_POST) {
            PostViewHolder postViewHolder = (PostViewHolder) holder;
            postViewHolder.UsernameCard.setText(item.getUsername());
            postViewHolder.contextCard.setText(item.getDescription());
            Glide.with(context).load(item.getPost_image()).into(postViewHolder.postImage);
        } else {
            QuestionViewHolder questionViewHolder = (QuestionViewHolder) holder;
            questionViewHolder.UsernameCard.setText(item.getUsername());
            questionViewHolder.contextCard.setText(item.getDescription());
        }
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
        ImageView postImage;
        TextView contextCard, UsernameCard;
        CardView postCard;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.postImage);
            UsernameCard = itemView.findViewById(R.id.UsernameCard);
            contextCard = itemView.findViewById(R.id.contextCard);
            postCard = itemView.findViewById(R.id.postCard);
        }
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView contextCard, UsernameCard;
        CardView postCard;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            UsernameCard = itemView.findViewById(R.id.UsernameCard);
            contextCard = itemView.findViewById(R.id.contextCard);
            postCard = itemView.findViewById(R.id.postCard);
        }
    }
}