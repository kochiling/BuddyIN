package com.cscorner.buddyin.admin;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cscorner.buddyin.LecturerModel;
import com.cscorner.buddyin.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class AdminRejectedFragment extends Fragment {

    RecyclerView rejected_rv;
    List<LecturerModel> lecturerModels;
    ApprovedAdapter adapter;
    DatabaseReference lecturerRef;
    TextView noDataText;
    ItemTouchHelper.SimpleCallback simpleCallback;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_rejected, container, false);

        noDataText = view.findViewById(R.id.no_data_text);
        noDataText.setVisibility(View.VISIBLE );

        rejected_rv= view.findViewById(R.id.rejected_crv);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        rejected_rv.setLayoutManager(gridLayoutManager);

        lecturerModels = new ArrayList<>();
        adapter = new ApprovedAdapter(getContext(), lecturerModels);
        rejected_rv.setAdapter(adapter);

        // Firebase reference to 'Lecturers' node
        lecturerRef = FirebaseDatabase.getInstance().getReference("Lecturers");


        Query query = lecturerRef.orderByChild("status").equalTo(2);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lecturerModels.clear(); // Clear the list before adding new data

                // Loop through all the results
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LecturerModel lecturer = snapshot.getValue(LecturerModel.class);
                    lecturerModels.add(lecturer); // Add the lecturer to the list
                }

                //Swipe

                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
                itemTouchHelper.attachToRecyclerView(rejected_rv);

                // Notify the adapter that the data has changed
                adapter.setLecturerListModel(lecturerModels);
                adapter.notifyDataSetChanged();
                noDataText.setVisibility(lecturerModels.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });

        simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Approve This Lecturer Again");
                builder.setMessage("Are You Sure ??");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int position = viewHolder.getAdapterPosition();
                        LecturerModel taskApprove = lecturerModels.get(position);

                        lecturerRef.child(taskApprove.getUser_id()).child("status").setValue(1)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Lecturer Approved", Toast.LENGTH_SHORT).show();

                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());


                        lecturerModels.remove(position);
                        adapter.notifyItemRemoved(position);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    }
                });
                builder.show();
            }
        };








        return view;
    }
}