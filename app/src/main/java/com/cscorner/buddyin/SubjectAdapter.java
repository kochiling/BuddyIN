package com.cscorner.buddyin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class SubjectAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private Context context;
    private List<SubjectClass> sublist;

    public SubjectAdapter(Context context, List<SubjectClass> sublist) {
        this.sublist= sublist;
    }

    // This method creates a new ViewHolder object for each item in the RecyclerView
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each item and return a new ViewHolder object
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_notes_item, parent, false);
        return new MyViewHolder(itemView);
    }

    // This method binds the data to the ViewHolder object
    // for each item in the RecyclerView
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.subject.setText(sublist.get(position).getSubject());
    }


    // This method returns the total
    // number of items in the data set
    @Override
    public int getItemCount() {
        return sublist.size();
    }

}

class MyViewHolder extends RecyclerView.ViewHolder {
    TextView subject;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        subject = itemView.findViewById(R.id.subjectTitle);
    }
}


