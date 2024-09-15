package com.cscorner.buddyin;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.widget.Button;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class HomeFragment extends Fragment {

    RecyclerView recyclerViewH,recyclerViewV;
    List<String> subList;
    List<PostModel> postModelList;
    SubjectAdapter adapter;
    PostAdapter adapter1;
    FloatingActionButton addButton;
    Button viewallbtn;
    DatabaseReference databaseReference;
    ValueEventListener eventListener;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize the RecyclerView
        recyclerViewH = view.findViewById(R.id.HorizontalNotesRV);
        // Initialize the data list and adapter
        subList = new ArrayList<>();
        // adding horizontal layout manager for our recycler view.
        recyclerViewH.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        // adding our array list to our recycler view adapter class.
        adapter = new SubjectAdapter(getContext(), subList);
        // setting adapter to our recycler view.
        recyclerViewH.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Subject Data");
        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    String subject = itemSnapshot.getValue(String.class);
                    subList.add(subject);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load subjects.", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerViewV = view.findViewById(R.id.VerticalPostRV);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerViewV.setLayoutManager(gridLayoutManager);

        postModelList = new ArrayList<>();

        adapter1 = new PostAdapter(getContext(), postModelList);
        recyclerViewV.setAdapter(adapter1);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Post");
        databaseReference.orderByChild("timestamp").limitToLast(100).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<PostModel> postModelList = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    PostModel postModel = postSnapshot.getValue(PostModel.class);
                    if (postModel != null) {
                        postModelList.add(postModel);
                    }
                }

                // Reverse the list to show the latest posts first
                Collections.reverse(postModelList);

                adapter1.setPostModelList(postModelList);
                adapter1.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load posts.", Toast.LENGTH_SHORT).show();
            }
        });


        //Add button
        addButton = view.findViewById(R.id.addButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                CharSequence options[] = new CharSequence[]{
                        "Post or Question",
                        "Notes",
                        "Cancel"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Choose to Upload");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent intent = new Intent(v.getContext(), UploadPostActivity.class);
                            startActivity(intent);
                        }
                        if (which == 1) {
                            Intent intent = new Intent(v.getContext(), UploadNotesActivity.class);
                            startActivity(intent);
                        }
                    }
                });
                builder.show();
            }
        });

        viewallbtn = view.findViewById(R.id.viewall);
        viewallbtn.setOnClickListener((View v) -> {
            Intent intent = new Intent(getContext(), NotesSubjectActivity.class);
            startActivity(intent);
        });
        return view;
    }

}
