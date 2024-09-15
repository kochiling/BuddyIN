package com.cscorner.buddyin.admin;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

import com.cscorner.buddyin.NotesAdapter;
import com.cscorner.buddyin.NotesModel;
import com.cscorner.buddyin.NotesSubjectActivity;
import com.cscorner.buddyin.R;
import com.cscorner.buddyin.UploadNotesActivity;
import com.cscorner.buddyin.UploadPostActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AdminNotesFragment extends Fragment {

    RecyclerView recyclerView;
    FloatingActionButton addButton;
    List<NotesModel> notesModelList;
    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    NotesAdapter adapter;
    ItemTouchHelper.SimpleCallback simpleCallback;

    public AdminNotesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_notes, container, false);

        recyclerView = view.findViewById(R.id.nbs_rv);

        addButton = view.findViewById(R.id.addButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent intent = new Intent(v.getContext(), UploadNotesActivity.class);
                startActivity(intent);
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        notesModelList = new ArrayList<>();

        adapter = new NotesAdapter(getContext(), notesModelList);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Notes");
        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notesModelList.clear();
                for (DataSnapshot itemSnapshot: snapshot.getChildren()){
                    NotesModel dataClass = itemSnapshot.getValue(NotesModel.class);
                    notesModelList.add(dataClass);
                }

                //Swipe

                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
                itemTouchHelper.attachToRecyclerView(recyclerView);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
                builder.setTitle("Delete Notes");
                builder.setMessage("Are You Sure you want to delete this note?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int position = viewHolder.getAdapterPosition();
                        NotesModel taskToDelete = notesModelList.get(position);

                        // Get the storage reference from the notes model (assuming you store the file URL in the notes model)
                        String fileUrl = taskToDelete.getPdfURL(); // Replace 'getFileUrl' with your method to get the storage file URL or path

                        if (fileUrl != null && !fileUrl.isEmpty()) {
                            // Create a storage reference from the file URL or path
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference fileRef = storage.getReferenceFromUrl(fileUrl);

                            // Delete the file from Firebase Storage
                            fileRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // File successfully deleted from Firebase Storage

                                    // Now, remove the entry from the Realtime Database
                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notes");
                                    reference.child(taskToDelete.getNotes_id()).removeValue();

                                    // Show a success message
                                    Toast.makeText(getContext(), "Note and file deleted", Toast.LENGTH_SHORT).show();

                                    // Remove the item from the list and notify the adapter
                                    notesModelList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle errors if the file deletion fails
                                    Toast.makeText(getContext(), "Failed to delete file", Toast.LENGTH_SHORT).show();
                                    adapter.notifyItemChanged(viewHolder.getAdapterPosition()); // Revert the swipe action
                                }
                            });
                        } else {
                            // If there's no file to delete, just remove the database entry
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notes");
                            reference.child(taskToDelete.getNotes_id()).removeValue();

                            // Show a success message
                            Toast.makeText(getContext(), "Note deleted", Toast.LENGTH_SHORT).show();

                            // Remove the item from the list and notify the adapter
                            notesModelList.remove(position);
                            adapter.notifyItemRemoved(position);
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.notifyItemChanged(viewHolder.getAdapterPosition()); // Revert the swipe action
                    }
                });
                builder.show();
            }
        };



        return view;
    }
}