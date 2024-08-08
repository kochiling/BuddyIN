package com.cscorner.buddyin;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SubjectFilterActivity extends AppCompatActivity {

    TextView subjectTextView;
    RecyclerView recyclerView;
    List<NotesModel> notesModelList;
    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    NotesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_subject_filter);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        subjectTextView = findViewById(R.id.subjectText);
        String subject = getIntent().getStringExtra("subject");
        subjectTextView.setText(subject);

        recyclerView = findViewById(R.id.sf_rv);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(SubjectFilterActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        notesModelList = new ArrayList<>();
        adapter = new NotesAdapter(SubjectFilterActivity.this, notesModelList);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Notes");

        // Query to filter notes by subject
        Query subjectQuery = databaseReference.orderByChild("subject").equalTo(subject);
        eventListener = subjectQuery.addValueEventListener(new ValueEventListener() {
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
                // Handle possible errors.
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventListener != null) {
            databaseReference.removeEventListener(eventListener);
        }
    }
}