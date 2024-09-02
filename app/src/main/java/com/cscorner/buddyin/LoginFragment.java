package com.cscorner.buddyin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginFragment extends Fragment {

    TextInputEditText emailinput,passwordinput;
    Button loginbtn,createAccbtn,forgetPasswordBtn;
    FrameLayout parent_framelayout;
    ProgressBar progressBar;
    TextInputLayout emailinputlayout,passwordinputlayout;
    FirebaseAuth mAuth;

//    @Override
//    public void onStart() {
//        super.onStart();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            Intent intent = new Intent(getActivity(), HomeActivity.class);
//            startActivity(intent);
//            requireActivity().finish();
//        }
//    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Check the user's role before redirecting
            checkUserRole(currentUser.getUid());
        }
    }

    private void checkUserRole(String userId) {
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Admin").child(userId);

        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && Boolean.TRUE.equals(dataSnapshot.child("isAdmin").getValue(Boolean.class))) {
                    // User is an admin, redirect to Admin Home Page
                    Intent adminIntent = new Intent(getActivity(), AdminHomePageActivity.class);
                    startActivity(adminIntent);
                    getActivity().finish();
                } else {
                    // Check if the user is a lecturer
                    checkLecturerStatus(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkLecturerStatus(String userId) {
        DatabaseReference lecturerRef = FirebaseDatabase.getInstance().getReference("Lecturers").child(userId);

        lecturerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer status = dataSnapshot.child("status").getValue(Integer.class);
                    handleLecturerLogin(status);
                } else {
                    // Not a lecturer, proceed to regular user home page
                    Intent intent = new Intent(getActivity(), HomeActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_login, container, false);

        emailinput = view.findViewById(R.id.emailinput);
        passwordinput = view.findViewById(R.id.passwordinput);
        loginbtn = view.findViewById(R.id.loginbtn);
        createAccbtn = view.findViewById(R.id.createAccbtn);
        forgetPasswordBtn = view.findViewById(R.id.forgetPasswordbtn);
        parent_framelayout = getActivity().findViewById(R.id.parent_framelayout);
        progressBar = view.findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        emailinputlayout = view.findViewById(R.id.emailinputlayout);
        passwordinputlayout = view.findViewById(R.id.passwordinputlayout);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        createAccbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(new SignUpRoleFragment());
                //Intent intent = new Intent(requireActivity(), RegisterActivity.class);
                //startActivity(intent);
            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(View.VISIBLE);
                String email = Objects.requireNonNull(emailinput.getText()).toString().trim();
                String password = Objects.requireNonNull(passwordinput.getText()).toString().trim();

                if (!validateEmail(email) || !validatePassword(password) ) {
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                // Attempt to log in as a regular user using Firebase Auth
                signInRegularUser(email, password);

            }


            private void signInRegularUser(String email, String password) {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Login successful, now check the user's role
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            checkUserRole(currentUser.getUid());
                        }
                    } else {
                        Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

//            private void signInRegularUser(String email, String password) {
//                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
//                    progressBar.setVisibility(View.GONE);
//                    if (task.isSuccessful()) {
//                        // Login successful, now check if user is a lecturer
//                        FirebaseUser currentUser = mAuth.getCurrentUser();
//                        if (currentUser != null) {
//                            String userId = currentUser.getUid();
//                            DatabaseReference lecturerRef = FirebaseDatabase.getInstance().getReference("Lecturers").child(userId);
//
//                            // Fetch lecturer details from Firebase
//                            lecturerRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                    if (dataSnapshot.exists()) {
//                                        // User is a lecturer, get the status
//                                        Integer status = dataSnapshot.child("status").getValue(Integer.class);
//                                        handleLecturerLogin(status);
//                                    } else {
//                                        // Not a lecturer, proceed to regular user home page
//                                        Toast.makeText(getActivity(), "Login Successful.", Toast.LENGTH_SHORT).show();
//                                        Intent intent = new Intent(getActivity(), HomeActivity.class);
//                                        startActivity(intent);
//                                        getActivity().finish();
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError databaseError) {
//                                    Toast.makeText(getActivity(), "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        }
//                    } else {
//                        Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }

        });

    }

    private void handleLecturerLogin(Integer status) {
        if (status == null) {
            Toast.makeText(getActivity(), "Error fetching lecturer status.", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (status) {
            case 0:
                // Status 0: Account pending approval
                new AlertDialog.Builder(requireActivity())
                        .setTitle("Account Pending")
                        .setMessage("Your lecturer account is pending approval. Please wait for confirmation.")
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, which) -> mAuth.signOut())
                        .show();
                break;
            case 1:
                // Status 1: Approved, redirect to Lecturer Home Page
                Intent lecturerIntent = new Intent(getActivity(), LecturerHomeActivity.class);
                startActivity(lecturerIntent);
                getActivity().finish();
                break;
            case 2:
                // Status 2: Approval failed
                new AlertDialog.Builder(requireActivity())
                        .setTitle("Approval Failed")
                        .setMessage("Your lecturer account approval has failed. Please contact support.")
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, which) -> mAuth.signOut())
                        .show();
                break;
            default:
                Toast.makeText(getActivity(), "Unknown status.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            emailinputlayout.setError("Email address is required!");
            Toast.makeText(getActivity(), "Email address is required!", Toast.LENGTH_SHORT).show();
            emailinput.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailinputlayout.setError("Email address is invalid!");
            Toast.makeText(getActivity(), "Email address is invalid!", Toast.LENGTH_SHORT).show();
            emailinput.requestFocus();
            return false;
        } else {
            emailinputlayout.setError(null);
            return true;
        }
    }



    private boolean validatePassword(String password) {
        if (password.isEmpty()) {
            passwordinputlayout.setError("Password is required!");
            Toast.makeText(getActivity(), "Password is required!", Toast.LENGTH_SHORT).show();
            passwordinput.requestFocus();
            return false;
        } else {
            passwordinputlayout.setError(null);
            return true;
        }
    }


    private void setFragment (Fragment fragment){
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(parent_framelayout.getId(), fragment);
        fragmentTransaction.commit();
    }
}

