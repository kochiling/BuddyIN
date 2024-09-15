package com.cscorner.buddyin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class NotesProfileFragment extends Fragment {

    TextView noDataText;
    List<NotesModel> notesModelList;
    RecyclerView notes_crv;
    ProfileNotesAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notes_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noDataText = view.findViewById(R.id.no_data_text);
        noDataText.setVisibility(View.VISIBLE );

        notes_crv = view.findViewById(R.id.notes_crv);


        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        notes_crv .setLayoutManager(gridLayoutManager);

        notesModelList = new ArrayList<>();

        adapter = new ProfileNotesAdapter(getActivity(), notesModelList);
        notes_crv.setAdapter(adapter);

        // Get the current user's ID (assuming you're using Firebase Authentication)
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Reference to the "Notes" node in Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Notes");

        // Query to fetch posts where userId equals the current user's ID
        databaseReference.orderByChild("user_id").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notesModelList.clear();  // Clear the old list before adding new data
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            NotesModel notesModel = postSnapshot.getValue(NotesModel.class);
                            if (notesModel != null) {
                                notesModelList.add(notesModel);
                            }
                        }

                        // Reverse the list to show the latest posts first
                        Collections.reverse(notesModelList);

                        // Update the adapter with the fetched posts
                        adapter.notifyDataSetChanged();
                        noDataText.setVisibility(notesModelList.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load notes.", Toast.LENGTH_SHORT).show();
                    }
                });

    }
}