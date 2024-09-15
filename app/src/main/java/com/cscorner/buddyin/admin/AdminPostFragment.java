package com.cscorner.buddyin.admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.cscorner.buddyin.NotesAdapter;
import com.cscorner.buddyin.NotesModel;
import com.cscorner.buddyin.PostAdapter;
import com.cscorner.buddyin.PostModel;
import com.cscorner.buddyin.R;
import com.cscorner.buddyin.SubjectAdapter;
import com.cscorner.buddyin.UploadNotesActivity;
import com.cscorner.buddyin.UploadPostActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminPostFragment extends Fragment {

    RecyclerView recyclerView;
    FloatingActionButton addButton;
    List<PostModel> postModelList;
    PostAdapter adapter1;



    DatabaseReference databaseReference;
    ValueEventListener eventListener;

    public AdminPostFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_post, container, false);

        recyclerView = view.findViewById(R.id.nbs_rv);

        addButton = view.findViewById(R.id.addButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent intent = new Intent(v.getContext(), UploadPostActivity.class);
                startActivity(intent);
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        postModelList = new ArrayList<>();

        adapter1 = new PostAdapter(getContext(), postModelList);
        recyclerView.setAdapter(adapter1);

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

        return view;
    }
}