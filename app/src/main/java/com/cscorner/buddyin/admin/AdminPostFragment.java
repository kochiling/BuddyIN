package com.cscorner.buddyin.admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.cscorner.buddyin.NotesModel;
import com.cscorner.buddyin.PostAdapter;
import com.cscorner.buddyin.PostModel;
import com.cscorner.buddyin.R;
import com.cscorner.buddyin.UploadPostActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminPostFragment extends Fragment {

    RecyclerView recyclerView;
    FloatingActionButton addButton;
    List<PostModel> postModelList;
    PostAdapter adapter1;
    ItemTouchHelper.SimpleCallback simpleCallback;
    ImageView trashIcon;
    int originalPosition = -1;
    int longClickedPosition = -1;// Variable to store the original position before drag

    public AdminPostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_post, container, false);

        recyclerView = view.findViewById(R.id.nbs_rv);
        trashIcon = view.findViewById(R.id.trash_icon);
        trashIcon.setVisibility(View.GONE);// Hide trash icon initially

        addButton = view.findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), UploadPostActivity.class);
            startActivity(intent);
        });

        // Setup RecyclerView layout and adapter
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        postModelList = new ArrayList<>();
        adapter1 = new PostAdapter(getContext(), postModelList);
        recyclerView.setAdapter(adapter1);

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

                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
                itemTouchHelper.attachToRecyclerView(recyclerView);

                Collections.reverse(postModelList);  // Show latest posts first
                adapter1.setPostModelList(postModelList);
                adapter1.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load posts.", Toast.LENGTH_SHORT).show();
            }
        });

        simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Delete Post")
                        .setMessage("Are You Sure you want to delete this post?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            // Remove post from Firebase and list
                            int position = viewHolder.getAdapterPosition();
                            PostModel postModel = postModelList.get(position);

                            // Remove post image from Firebase Storage
                            String imageUrl = postModel.getPost_image();

                            if ("null".equals(imageUrl)) {
                                Toast.makeText(getContext(), "Skipped", Toast.LENGTH_SHORT).show();
                            }else{
                                // Get a reference to the image in Firebase Storage
                                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                storageReference.delete().addOnSuccessListener(aVoid -> {
                                    // Image deleted successfully
                                    Toast.makeText(getContext(), "Image deleted", Toast.LENGTH_SHORT).show();
                                }).addOnFailureListener(e -> {

                                });
                            }

                            DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Post").child(postModel.getPost_id());
                            postRef.removeValue();
                            postModelList.remove(position);
                            adapter1.notifyItemRemoved(position);

                            Toast.makeText(getContext(), "Post deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            // Revert to the original position if the deletion is cancelled
                            adapter1.notifyItemChanged(viewHolder.getAdapterPosition());
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        };

        return view;
    }
}
//    private void loadPostsFromFirebase() {
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Post");
//        databaseReference.orderByChild("timestamp").limitToLast(100).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                postModelList.clear();
//                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
//                    PostModel postModel = postSnapshot.getValue(PostModel.class);
//                    if (postModel != null) {
//                        postModelList.add(postModel);
//                    }
//                }
//                Collections.reverse(postModelList);  // Show latest posts first
//                adapter1.setPostModelList(postModelList);
//                adapter1.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getContext(), "Failed to load posts.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//

//    private void setupDragAndDrop() {
//        simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, 0) {
//
//            @Override
//            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
//
//                return false;
//            }
//
//            @Override
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//                // No swipe functionality in this case
//            }
//
//            @Override
//            public boolean isLongPressDragEnabled() {
//                return true; // Enable long press to drag
//            }
//
//            @Override
//            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
//                super.onSelectedChanged(viewHolder, actionState);
//
//                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
//                    trashIcon.setVisibility(View.VISIBLE);  // Show trash icon when dragging starts
//                    originalPosition = viewHolder.getAdapterPosition();  // Store the original position
//                } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
//                    trashIcon.setVisibility(View.GONE);  // Hide trash icon when dragging ends
//                }
//            }
//
//            @Override
//            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
//                super.clearView(recyclerView, viewHolder);
//
//                // Check if the item was dropped on the trash icon
//                if (isViewOverlapping(viewHolder.itemView, trashIcon)) {
//                    // Show confirmation dialog to delete
//                    showConfirmationDialog(viewHolder.getAdapterPosition());
//                } else {
//                    // Revert item position if not overlapping
//                    adapter1.notifyItemChanged(viewHolder.getAdapterPosition());
//                }
//
//                // Hide trash icon after drag ends
//                trashIcon.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
//                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
//
//                // Check overlap while dragging
//                if (isCurrentlyActive && isViewOverlapping(viewHolder.itemView, trashIcon)) {
//                    trashIcon.setColorFilter(getResources().getColor(R.color.red));
//                    trashIcon.setImageResource(R.drawable.baseline_delete_forever_24);  // Highlight trash icon
//                } else {
//                    trashIcon.setColorFilter(getResources().getColor(R.color.red));  // Reset to default color
//                    trashIcon.setImageResource(R.drawable.baseline_delete_24);  // Reset icon to default
//                }
//            }
//
//        };
//
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
//        itemTouchHelper.attachToRecyclerView(recyclerView);
//    }
//
//    private boolean isViewOverlapping(View firstView, View secondView) {
//        int[] firstViewPosition = new int[2];
//        int[] secondViewPosition = new int[2];
//
//        firstView.getLocationOnScreen(firstViewPosition);
//        secondView.getLocationOnScreen(secondViewPosition);
//
//        int firstViewLeft = firstViewPosition[0];
//        int firstViewTop = firstViewPosition[1];
//        int firstViewRight = firstViewLeft + firstView.getWidth();
//        int firstViewBottom = firstViewTop + firstView.getHeight();
//
//        int secondViewLeft = secondViewPosition[0];
//        int secondViewTop = secondViewPosition[1];
//        int secondViewRight = secondViewLeft + secondView.getWidth();
//        int secondViewBottom = secondViewTop + secondView.getHeight();
//
//        // Adjust sensitivity by expanding overlap area
//        int sensitivityPadding = 50; // Adjust this value as needed
//
//        int overlapLeft = Math.max(firstViewLeft, secondViewLeft) - sensitivityPadding;
//        int overlapTop = Math.max(firstViewTop, secondViewTop) - sensitivityPadding;
//        int overlapRight = Math.min(firstViewRight, secondViewRight) + sensitivityPadding;
//        int overlapBottom = Math.min(firstViewBottom, secondViewBottom) + sensitivityPadding;
//
//
//        if (overlapLeft < overlapRight && overlapTop < overlapBottom) {
//            int overlapWidth = overlapRight - overlapLeft;
//            int overlapHeight = overlapBottom - overlapTop;
//            int overlapArea = overlapWidth * overlapHeight;
//            System.out.println("Overlap Area: " + overlapArea);
//            return overlapArea > 0; // Indicates there is some overlap
//        } else {
//            System.out.println("No Overlap");
//            return false; // No overlap
//        }
//    }
//
//
//
//    // Show confirmation dialog before deleting the post
//    private void showConfirmationDialog(final int position) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setTitle("Delete Post")
//                .setMessage("Are You Sure you want to delete this post?")
//                .setPositiveButton("Delete", (dialog, which) -> {
//                    // Remove post from Firebase and list
//                    PostModel postModel = postModelList.get(position);
//                    DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Post").child(postModel.getPost_id());
//                    postRef.removeValue();
//                    postModelList.remove(position);
//                    adapter1.notifyItemRemoved(position);
//
//                    Toast.makeText(getContext(), "Post deleted", Toast.LENGTH_SHORT).show();
//                })
//                .setNegativeButton("Cancel", (dialog, which) -> {
//                    // Revert to the original position if the deletion is cancelled
//                    adapter1.notifyItemMoved(position, originalPosition);
//                })
//                .setIcon(android.R.drawable.ic_dialog_alert)
//                .show();
//    }
//}
