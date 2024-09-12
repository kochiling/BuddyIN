package com.cscorner.buddyin.admin;

import android.content.Context;
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
import com.cscorner.buddyin.LecturerModel;
import com.cscorner.buddyin.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class PendingAdapter extends RecyclerView.Adapter<PendingViewHolder> {

    private final Context context;
    private List<LecturerModel> lecturerModels;
    private DatabaseReference lecturerRef;

    public PendingAdapter(Context context, List<LecturerModel> lecturerModels) {
        this.context = context;
        this.lecturerModels = lecturerModels;

        // Initialize the Firebase reference
        lecturerRef = FirebaseDatabase.getInstance().getReference("Lecturers");
    }

    @NonNull
    @Override
    public PendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.pending_lecturer_item, parent, false);
        return new PendingViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingViewHolder holder, int position) {
        LecturerModel lecturerModel = lecturerModels.get(position);

        holder.profile_name.setText(lecturerModel.getName());
        holder.lid.setText(lecturerModel.getId());
        holder.email.setText(lecturerModel.getEmail());
        holder.faculty.setText(lecturerModel.getFaculty());

        // Load the profile image
        Glide.with(context).load(lecturerModel.getProfile_image()).into(holder.profileImage);

        // Approve button click - Change status to 1
        holder.approvebtn.setOnClickListener(v -> {
            lecturerRef.child(lecturerModel.getUser_id()).child("status").setValue(1)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Lecturer Approved", Toast.LENGTH_SHORT).show();

                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // Reject button click - Change status to 2
        holder.rejectbtn.setOnClickListener(v -> {
            lecturerRef.child(lecturerModel.getUser_id()).child("status").setValue(2)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Lecturer Rejected", Toast.LENGTH_SHORT).show();

                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return lecturerModels != null ? lecturerModels.size() : 0;
    }

    public void setLecturerListModel(List<LecturerModel> lecturerModels) {
        this.lecturerModels = lecturerModels;
        notifyDataSetChanged();
    }
}

class PendingViewHolder extends RecyclerView.ViewHolder {

    ImageView profileImage;
    TextView profile_name, lid, email, faculty;
    CardView rec_Card;
    Button approvebtn, rejectbtn;

    public PendingViewHolder(@NonNull View itemView) {
        super(itemView);
        rec_Card = itemView.findViewById(R.id.rec_Card);
        profile_name = itemView.findViewById(R.id.profile_name);
        lid = itemView.findViewById(R.id.lid);
        email = itemView.findViewById(R.id.email);
        faculty = itemView.findViewById(R.id.faculty);
        profileImage = itemView.findViewById(R.id.profileImage);
        approvebtn = itemView.findViewById(R.id.add_btn);
        rejectbtn = itemView.findViewById(R.id.remove_btn);
    }
}