package com.cscorner.buddyin;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NotesSubjectActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<NotesModel> notesModelList;
    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    NotesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notes_subject);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.nbs_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("View All Notes");
        // Enable the Up button (back button)
        toolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.nbs_rv);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(NotesSubjectActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        notesModelList = new ArrayList<>();

        adapter = new NotesAdapter(NotesSubjectActivity.this, notesModelList);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Notes");
        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notesModelList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    NotesModel dataClass = itemSnapshot.getValue(NotesModel.class);
                    notesModelList.add(dataClass);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the Up button click (e.g., navigate back)
            onBackPressed();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_top, menu);

        // Get the search menu item
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        // Set up the query listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform the final search when user submits
                filterResults(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Perform search as text changes
                filterResults(newText);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void filterResults(String query) {
        // Filter for notes using a separate list
        ArrayList<NotesModel> searchList = new ArrayList<>();
        for (NotesModel note : notesModelList) {
            if (note.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                    note.getUser_name().toLowerCase().contains(query.toLowerCase()) ||
                    note.getSubject().toLowerCase().contains(query.toLowerCase()) ||
                    note.getFile_title().toLowerCase().contains(query.toLowerCase())) {
                searchList.add(note);
            }
        }
        adapter.searchDataList(searchList);  // Update adapter with the filtered list

        // Notify the adapter of data changes
        adapter.notifyDataSetChanged();
    }
}
