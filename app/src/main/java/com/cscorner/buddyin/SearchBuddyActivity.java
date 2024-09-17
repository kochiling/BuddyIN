package com.cscorner.buddyin;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.protobuf.Empty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchBuddyActivity extends AppCompatActivity {

    SearchView searchView;
    RecyclerView recyclerViewV;
    TextView label_posts;

    List<UserModel> UserList;
    SearchBuddyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_buddy);
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
        recyclerViewV = findViewById(R.id.VerticalPostRV);
        label_posts = findViewById(R.id.label_posts);

        recyclerViewV.setLayoutManager(new GridLayoutManager(this, 1));
        UserList = new ArrayList<>();
        adapter = new SearchBuddyAdapter(SearchBuddyActivity.this, UserList);
        recyclerViewV.setAdapter(adapter);

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
        ArrayList<KNNDataInfoModel> searchList = new ArrayList<>();
        DatabaseReference knnRef = FirebaseDatabase.getInstance().getReference("KNN Data Information");

        if (query.trim().isEmpty()) {
            // If query is empty, show the placeholder text and hide the RecyclerView
            label_posts.setVisibility(View.VISIBLE);
            label_posts.setText("Enter Keyword to search"); // Make sure this string is defined in your strings.xml
            recyclerViewV.setVisibility(View.GONE);
            UserList.clear(); // Clear previous results
            adapter.notifyDataSetChanged();
            return;
        }

        knnRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                searchList.clear(); // Clear previous search results

                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        KNNDataInfoModel knnData = dataSnapshot.getValue(KNNDataInfoModel.class);

                        if (knnData != null && (knnData.getName().toLowerCase().contains(query.toLowerCase()))) {
                            searchList.add(knnData);
                        }
                    }

                    if (!searchList.isEmpty()) {
                        label_posts.setVisibility(View.GONE);
                        recyclerViewV.setVisibility(View.VISIBLE);

                        // Clear UserList before adding new results
                        UserList.clear();

                        // Extract buddyId from each KNNDataInfoModel and fetch user details
                        for (KNNDataInfoModel knnData : searchList) {
                            String buddyId = knnData.getUserid(); // Get the buddyId from the KNN data
                            fetchUserDetails(buddyId); // Use buddyId to fetch user details
                        }
                    } else {
                        label_posts.setVisibility(View.VISIBLE);
                        label_posts.setText(R.string.no_results);
                        recyclerViewV.setVisibility(View.GONE);
                    }
                } else {
                    Log.d(TAG, "No KNN Data found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching KNN data: " + error.getMessage());
            }
        });
    }

    private void fetchUserDetails(String buddyId) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("User Info").child(buddyId);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel userModel = snapshot.getValue(UserModel.class);
                if (userModel != null) {
                    userModel.setKey(snapshot.getKey()); // Store key if needed
                    UserList.add(userModel); // Add user model to the list
                    adapter.setSearchBuddyList(UserList);
                    adapter.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "UserModel is null for buddyId: " + buddyId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchBuddyActivity.this, "Failed to load buddy info.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
