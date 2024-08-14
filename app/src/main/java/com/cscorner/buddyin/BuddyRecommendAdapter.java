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

public class BuddyRecommendAdapter extends RecyclerView.Adapter<BuddyRecommendViewHolder> {

    private final Context context;
    private final List<UserModel> userModelList;
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

    }

    @Override
    public int getItemCount() {
        return (userModelList.size());
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
