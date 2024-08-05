package com.cscorner.buddyin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.util.Objects;

public class LoginFragment extends Fragment {

    TextInputEditText emailinput,passwordinput;
    Button loginbtn,createAccbtn,forgetPasswordBtn;
    FrameLayout parent_framelayout;
    ProgressBar progressBar;
    TextInputLayout emailinputlayout,passwordinputlayout;
    FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getActivity(), HomeActivity.class);
            startActivity(intent);
            requireActivity().finish();
        }
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
                setFragment(new SignUpFragment());
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


                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Login Successful.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    } else {
                        Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

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

