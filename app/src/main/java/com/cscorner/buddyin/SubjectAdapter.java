package com.cscorner.buddyin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectViewHolder> {

    private final Context context;
    private  List<String> sublist;

    public SubjectAdapter(Context context, List<String> sublist) {
        this.context = context;
        this.sublist = sublist;
    }

    // This method creates a new ViewHolder object for each item in the RecyclerView
    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each item and return a new ViewHolder object
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_notes_item, parent, false);
        return new SubjectViewHolder(itemView);
    }

    // This method binds the data to the ViewHolder object
    // for each item in the RecyclerView
    @Override
    public void onBindViewHolder(SubjectViewHolder holder, int position) {
        String subject = sublist.get(position);
        holder.subject.setText(subject);

        holder.subjectCard.setOnClickListener(v -> {
            Intent intent = new Intent(context, SubjectFilterActivity.class);
            intent.putExtra("subject", subject);
            context.startActivity(intent);
        });
    }

    // This method returns the total
    // number of items in the data set
    @Override
    public int getItemCount() {
        return sublist.size();
    }

    public void searchDataList(List<String> filteredList) {
        this.sublist = filteredList;  // Update the list with the filtered subjects
        notifyDataSetChanged();  // Notify the adapter about the data change
    }
}

class SubjectViewHolder extends RecyclerView.ViewHolder {
    TextView subject;
    CardView subjectCard;

    public SubjectViewHolder(@NonNull View itemView) {
        super(itemView);
        subject = itemView.findViewById(R.id.subjectTitle);
        subjectCard = itemView.findViewById(R.id.subjectCard);
    }
}
