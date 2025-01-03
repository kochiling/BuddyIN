package com.cscorner.buddyin.admin;

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
import com.cscorner.buddyin.LecturerModel;
import com.cscorner.buddyin.R;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class ApprovedAdapter extends RecyclerView.Adapter<ApprovedViewHolder>{

    private final Context context;
    private List<LecturerModel> lecturerModels;
    private DatabaseReference lecturerRef;

    public ApprovedAdapter(Context context, List<LecturerModel> lecturerModels) {
        this.context = context;
        this.lecturerModels = lecturerModels;
    }

    @NonNull
    @Override
    public ApprovedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.approved_lecturer_item, parent, false);
        return new ApprovedViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ApprovedViewHolder holder, int position) {
        LecturerModel lecturerModel = lecturerModels.get(position);

        holder.profile_name.setText(lecturerModel.getName());
        holder.lid.setText(lecturerModel.getId());
        holder.email.setText(lecturerModel.getEmail());
        holder.faculty.setText(lecturerModel.getFaculty());

        // Load the profile image
        Glide.with(context).load(lecturerModel.getProfile_image()).into(holder.profileImage);
    }

    @Override
    public int getItemCount() {
        return lecturerModels.size();
    }

    public void setLecturerListModel(List<LecturerModel> lecturerModels) {
        this.lecturerModels = lecturerModels;
        notifyDataSetChanged();
    }
}

class ApprovedViewHolder extends RecyclerView.ViewHolder {

    ImageView profileImage;
    TextView profile_name, lid, email, faculty;
    CardView rec_Card;

    public ApprovedViewHolder(@NonNull View itemView) {
        super(itemView);
        rec_Card = itemView.findViewById(R.id.rec_Card);
        profile_name = itemView.findViewById(R.id.profile_name);
        lid = itemView.findViewById(R.id.lid);
        email = itemView.findViewById(R.id.email);
        faculty = itemView.findViewById(R.id.faculty);
        profileImage = itemView.findViewById(R.id.profileImage);
    }
}
