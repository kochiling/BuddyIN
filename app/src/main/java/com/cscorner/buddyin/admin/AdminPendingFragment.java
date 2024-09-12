package com.cscorner.buddyin.admin;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cscorner.buddyin.LecturerModel;
import com.cscorner.buddyin.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class AdminPendingFragment extends Fragment {

    RecyclerView pending_rv;
    List<LecturerModel> lecturerModels;
    PendingAdapter adapter;
    DatabaseReference lecturerRef;
    TextView noDataText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_pending, container, false);

        noDataText = view.findViewById(R.id.no_data_text);
        noDataText.setVisibility(View.VISIBLE );

        pending_rv = view.findViewById(R.id.pending_crv);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        pending_rv.setLayoutManager(gridLayoutManager);

        lecturerModels = new ArrayList<>();
        adapter = new PendingAdapter(getContext(), lecturerModels);
        pending_rv.setAdapter(adapter);

        // Firebase reference to 'Lecturers' node
        lecturerRef = FirebaseDatabase.getInstance().getReference("Lecturers");

        // Querying the database for lecturers with status == 0
        Query query = lecturerRef.orderByChild("status").equalTo(0);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lecturerModels.clear(); // Clear the list before adding new data

                // Loop through all the results
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LecturerModel lecturer = snapshot.getValue(LecturerModel.class);
                    lecturerModels.add(lecturer); // Add the lecturer to the list
                }

                // Notify the adapter that the data has changed
                adapter.setLecturerListModel(lecturerModels);
                adapter.notifyDataSetChanged();
                noDataText.setVisibility(lecturerModels.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });

        return view;
    }
}