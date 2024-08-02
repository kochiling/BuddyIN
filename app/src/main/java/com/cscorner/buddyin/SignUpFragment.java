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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SignUpFragment extends Fragment {

    TextInputEditText emailinput,passwordinput,confirminput;
    Button singupbtn,backToLoginbtn;
    FrameLayout parent_framelayout;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextInputLayout emailinputlayout,passwordinputlayout, confirminputlayout;


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        mAuth = FirebaseAuth.getInstance();
        emailinput = view.findViewById(R.id.emailinput);
        passwordinput = view.findViewById(R.id.passwordinput);
        confirminput = view.findViewById(R.id.confirminput);
        singupbtn = view.findViewById(R.id.singupbtn);
        backToLoginbtn = view.findViewById(R.id.backToLoginbtn);
        parent_framelayout = getActivity().findViewById(R.id.parent_framelayout);
        progressBar = view.findViewById(R.id.progressBar);
        emailinputlayout = view.findViewById(R.id.emailinputlayout);
        passwordinputlayout = view.findViewById(R.id.passwordinputlayout);
        confirminputlayout = view.findViewById(R.id.confirminputlayout);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        backToLoginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(new LoginFragment());

            }
        });

        singupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

    }

    private void createAccount() {
        //validate to check if email is empty
        progressBar.setVisibility(View.VISIBLE);
        String email, password, passwordconfirm;
        email = Objects.requireNonNull(emailinput.getText()).toString().trim();
        password = Objects.requireNonNull(passwordinput.getText()).toString().trim();
        passwordconfirm = Objects.requireNonNull(confirminput.getText()).toString().trim();

        if (email.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            emailinputlayout.setError("Email address is required!");
            Toast.makeText(getActivity(), "Email address is required!", Toast.LENGTH_SHORT).show();
            emailinput.findViewById(R.id.emailinput).requestFocus();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            progressBar.setVisibility(View.GONE);
            emailinputlayout.setError("Email address is invalid!");
            Toast.makeText(getActivity(), "Email address is invalid!", Toast.LENGTH_SHORT).show();
            emailinput.findViewById(R.id.emailinput).requestFocus();
        }
        else if (password.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            passwordinputlayout.setError("Password is required!");
            Toast.makeText(getActivity(), "Password is required!", Toast.LENGTH_SHORT).show();
            passwordinput.findViewById(R.id.passwordinput).requestFocus();
        }
        else if (password.length() < 6) {
            progressBar.setVisibility(View.GONE);
            passwordinputlayout.setError("Password should be at least 6 characters!");
            Toast.makeText(getActivity(), "Password should be at least 6 characters!", Toast.LENGTH_SHORT).show();
            passwordinput.findViewById(R.id.passwordinput).requestFocus();
        }
        else if (passwordconfirm.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "Confirm password is required!", Toast.LENGTH_SHORT).show();
            confirminput.findViewById(R.id.confirminput).requestFocus();
        }
        else if (!passwordconfirm.equals(password)) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "Both of the password must be same", Toast.LENGTH_SHORT).show();
            confirminput.findViewById(R.id.confirminput).requestFocus();
        } else {
            // For non-admin users, attempt to create an account
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Account Created", Toast.LENGTH_SHORT).show();
                            // Redirect to the PersonalInformation class
                            Intent intent = new Intent(getActivity(), RegisterActivity.class);
                            startActivity(intent);
                            onDestroy();
                        } else {
                            Toast.makeText(getActivity(), "Authentication Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void setFragment (Fragment fragment){
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(parent_framelayout.getId(), fragment);
        fragmentTransaction.commit();
    }
}