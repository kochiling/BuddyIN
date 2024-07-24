package com.cscorner.buddyin;

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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    RecyclerView recyclerViewH;
    List<SubjectClass> subList;
    SubjectAdapter adapter;
    FloatingActionButton addButton;


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
        loadTestData();

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

        return view;

    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadTestData() {

        // Add some test data to the taskList
        subList.add(new SubjectClass("Math"));
        subList.add(new SubjectClass("Science"));
        subList.add(new SubjectClass("History"));
        subList.add(new SubjectClass("Chemistry"));
        subList.add(new SubjectClass("Physic"));

        // Notify the adapter about the data changes
        adapter.notifyDataSetChanged();
    }

    // Other methods of the HomeFragment
}


