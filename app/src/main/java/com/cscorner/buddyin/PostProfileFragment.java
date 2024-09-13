package com.cscorner.buddyin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
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


public class PostProfileFragment extends Fragment {

    TextView noDataText;
    List<PostModel> postModelList;
    PostAdapter adapter1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noDataText = view.findViewById(R.id.no_data_text);
        noDataText.setVisibility(View.VISIBLE );

        RecyclerView recyclerView = view.findViewById(R.id.posts_crv);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        postModelList = new ArrayList<>();

        adapter1 = new PostAdapter(getContext(), postModelList);
        recyclerView.setAdapter(adapter1);

        // Get the current user's ID (assuming you're using Firebase Authentication)
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Reference to the "Post" node in Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Post");

        // Query to fetch posts where userId equals the current user's ID
        databaseReference.orderByChild("user_id").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
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

                        // Update the adapter with the fetched posts
                        adapter1.setPostModelList(postModelList);
                        adapter1.notifyDataSetChanged();
                        noDataText.setVisibility(postModelList.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load posts.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}