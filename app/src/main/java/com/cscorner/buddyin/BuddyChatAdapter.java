package com.cscorner.buddyin;

import android.content.Context;
import android.content.Intent;
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

        holder.my_buddyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
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

class BuddyChatViewHolder extends RecyclerView.ViewHolder {
    TextView profile_name,lastseen;
    CardView my_buddyCard;
    ImageView profileImage;

    public BuddyChatViewHolder(@NonNull View itemView) {
        super(itemView);
        profile_name = itemView.findViewById(R.id.profile_name);
        lastseen = itemView.findViewById(R.id.last_chat);
        my_buddyCard = itemView.findViewById(R.id.mybuddy_card);
        profileImage = itemView.findViewById(R.id.profileImage);
    }
}