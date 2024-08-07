package com.cscorner.buddyin;

import android.content.Context;
import android.content.Intent;
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

public class NotesAdapter extends RecyclerView.Adapter<NotesViewHolder>{

    private final Context context;
    private List<NotesModel> notesModelList;

    public NotesAdapter(Context context, List<NotesModel> notesModelList) {
        this.context = context;
        this.notesModelList = notesModelList;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.nbs_item, parent, false);
        return new NotesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        holder.nbs_name.setText(notesModelList.get(position).getSubject());
        holder.pdf_name.setText(notesModelList.get(position).getFile_title());
        holder.pdf_description.setText(notesModelList.get(position).getDescription());

//        holder.nbs_card.setOnClickListener(view -> {
//            Intent intent = new Intent(view.getContext(), AddTipsDetailActivity.class);
//            intent.putExtra("Image", notesModelList.get(holder.getAdapterPosition()).getDataImage());
//            intent.putExtra("Title", notesModelList.get(holder.getAdapterPosition()).getDataTitle());
//            intent.putExtra("URL Link", notesModelList.get(holder.getAdapterPosition()).getDataLink());
//            intent.putExtra("Description", dataList.get(holder.getAdapterPosition()).getDataDesc());
//            intent.putExtra("Key",dataList.get(holder.getAdapterPosition()).getKey());
//            view.getContext().startActivity(intent);
//        });

    }

    @Override
    public int getItemCount() {
        return notesModelList.size();
    }
}

class NotesViewHolder extends RecyclerView.ViewHolder {
    TextView nbs_name,pdf_name,pdf_description;
    CardView nbs_card;
    ImageView imageView;

    public NotesViewHolder(@NonNull View itemView) {
        super(itemView);
        nbs_name = itemView.findViewById(R.id.nbs_name);
        pdf_name= itemView.findViewById(R.id.pdf_name);
        pdf_description= itemView.findViewById(R.id.pdf_description);
        nbs_card = itemView.findViewById(R.id.nbs_card);
        imageView = itemView.findViewById(R.id.image_view);
    }
}



