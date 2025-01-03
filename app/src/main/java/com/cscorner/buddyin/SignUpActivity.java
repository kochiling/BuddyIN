package com.cscorner.buddyin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    TextInputEditText emailinput,passwordinput,confirminput;
    Button singupbtn;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextInputLayout emailinputlayout,passwordinputlayout, confirminputlayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        emailinput = findViewById(R.id.emailinput);
        passwordinput = findViewById(R.id.passwordinput);
        confirminput = findViewById(R.id.confirminput);
        singupbtn = findViewById(R.id.singupbtn);

        progressBar = findViewById(R.id.progressBar);
        emailinputlayout = findViewById(R.id.emailinputlayout);
        passwordinputlayout = findViewById(R.id.passwordinputlayout);
        confirminputlayout = findViewById(R.id.confirminputlayout);

        singupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

        passwordinput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();

                // Initialize the error message
                StringBuilder errorMessage = new StringBuilder("Password should contain:");

                // Check password length
                if (password.length() < 6) {
                    errorMessage.append("\n- At least 6 characters");
                }

                // Check for lowercase letters
                boolean hasLowerCase = !password.equals(password.toUpperCase());
                if (!hasLowerCase) {
                    errorMessage.append("\n- At least one lowercase letter");
                }

                // Check for uppercase letters
                boolean hasUpperCase = !password.equals(password.toLowerCase());
                if (!hasUpperCase) {
                    errorMessage.append("\n- At least one uppercase letter");
                }

                // Check for numbers
                boolean hasNumber = password.matches(".*\\d.*");
                if (!hasNumber) {
                    errorMessage.append("\n- At least one number");
                }

                // Check for special characters
                boolean hasSpecialChar = password.matches(".*[^a-zA-Z0-9].*");
                if (!hasSpecialChar) {
                    errorMessage.append("\n- At least one special character");
                }

                // Set error message based on criteria
                if (password.length() >= 6 && hasLowerCase && hasUpperCase && hasNumber && hasSpecialChar) {
                    passwordinputlayout.setHelperText("");
                    passwordinputlayout.setError(null);  // Clear error if all criteria are met
                } else {
                    passwordinputlayout.setError(errorMessage.toString());
                    passwordinputlayout.setHelperText(null);  // Clear helper text
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            emailinputlayout.setError("Email address is required!");
            Toast.makeText(this, "Email address is required!", Toast.LENGTH_SHORT).show();
            emailinput.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailinputlayout.setError("Email address is invalid!");
            Toast.makeText(this, "Email address is invalid!", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Password is required!", Toast.LENGTH_SHORT).show();
            passwordinput.requestFocus();
            return false;
        } else if (password.length() < 6) {
            passwordinputlayout.setError("Password should be at least 6 characters!");
            Toast.makeText(this, "Password should be at least 6 characters!", Toast.LENGTH_SHORT).show();
            passwordinput.requestFocus();
            return false;
        } else {
            passwordinputlayout.setError(null);
            return true;
        }
    }

    private boolean validatePasswordConfirmation(String password, String passwordConfirm) {
        if (passwordConfirm.isEmpty()) {
            confirminputlayout.setError("Confirm password is required!");
            Toast.makeText(this, "Confirm password is required!", Toast.LENGTH_SHORT).show();
            confirminput.requestFocus();
            return false;
        } else if (!passwordConfirm.equals(password)) {
            confirminputlayout.setError("Both passwords must be the same");
            Toast.makeText(this, "Both passwords must be the same", Toast.LENGTH_SHORT).show();
            confirminput.requestFocus();
            return false;
        } else {
            confirminputlayout.setError(null);
            return true;
        }
    }

    private void createAccount() {
        progressBar.setVisibility(View.VISIBLE);
        String email = Objects.requireNonNull(emailinput.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordinput.getText()).toString().trim();
        String passwordconfirm = Objects.requireNonNull(confirminput.getText()).toString().trim();

        if (!validateEmail(email) || !validatePassword(password) || !validatePasswordConfirmation(password, passwordconfirm)) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, RegisterActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}