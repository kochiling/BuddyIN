package com.cscorner.buddyin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SearchPostNotesActivity extends AppCompatActivity {

    SearchView searchView;
    RecyclerView recyclerViewH,recyclerViewV;
    List<String> subList;
    List<PostModel> postModelList;
    SubjectAdapter adapter;
    PostAdapter adapter1;
    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    TextView label_notes,label_posts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_post_notes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        searchView = findViewById(R.id.search);
        // Initialize RecyclerViews
        recyclerViewH = findViewById(R.id.HorizontalNotesRV);
        recyclerViewV = findViewById(R.id.VerticalPostRV);
        // Initialize labels
        label_posts = findViewById(R.id.label_posts);
        label_notes = findViewById(R.id.label_notes);
        label_posts.setVisibility(View.VISIBLE);
        label_notes.setVisibility(View.VISIBLE);
        // Initially hide RecyclerViews and labels
        recyclerViewV.setVisibility(View.GONE);
        recyclerViewH.setVisibility(View.GONE);

        // Set up horizontal RecyclerView for subjects
        subList = new ArrayList<>();
        recyclerViewH.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new SubjectAdapter(this, subList);
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
                Toast.makeText(SearchPostNotesActivity.this, "Failed to load subjects.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up vertical RecyclerView for posts
        postModelList = new ArrayList<>();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        recyclerViewV.setLayoutManager(gridLayoutManager);
        adapter1 = new PostAdapter(this, postModelList);
        recyclerViewV.setAdapter(adapter1);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Post");
        databaseReference.orderByChild("timestamp").limitToLast(100).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postModelList.clear();
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
                Toast.makeText(SearchPostNotesActivity.this, "Failed to load posts.", Toast.LENGTH_SHORT).show();
            }
        });


        // Search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterResults(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Optional: Perform real-time search on query text change
                filterResults(newText);
                return true;
            }
        });
    }


    private void filterResults(String query) {
        // Filter for subjects
        List<String> filteredSubjectList = new ArrayList<>();
        for (String subject : subList) {
            if (subject.toLowerCase().contains(query.toLowerCase())) {
                filteredSubjectList.add(subject);
            }
        }

        // Filter for posts using a separate list
        ArrayList<PostModel> searchList = new ArrayList<>();
        for (PostModel post : postModelList) {
            if (post.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                    post.getUsername().toLowerCase().contains(query.toLowerCase()) ||
            post.getSubject().toLowerCase().contains(query.toLowerCase())) {
                searchList.add(post);
            }
        }
        adapter1.searchDataList(searchList);

        // Update the adapters with filtered data
        adapter.searchDataList(filteredSubjectList); // Update subject adapter
        adapter1.searchDataList(searchList); // Update post adapter

        // Notify the adapters of data changes
        adapter.notifyDataSetChanged();
        adapter1.notifyDataSetChanged();

        // Toggle the visibility of RecyclerViews and labels based on results
        if (filteredSubjectList.isEmpty()) {
            label_notes.setVisibility(View.VISIBLE);
            label_notes.setText(R.string.no_results);
            recyclerViewH.setVisibility(View.GONE);
        } else {
            label_notes.setVisibility(View.GONE);
            recyclerViewH.setVisibility(View.VISIBLE);
        }

        if (searchList.isEmpty()) {
            label_posts.setVisibility(View.VISIBLE);
            label_posts.setText(R.string.no_results);
            recyclerViewV.setVisibility(View.GONE);
        } else {
            label_posts.setVisibility(View.GONE);
            recyclerViewV.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {// Handle the Up button click (e.g., navigate back)
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}