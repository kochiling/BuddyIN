package com.cscorner.buddyin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    TextInputEditText emailinput,passwordinput,confirminput;
    Button singupbtn,backToLoginbtn;
    FirebaseAuth mAuth;
    ProgressBar progressBar;

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
        backToLoginbtn = findViewById(R.id.backToLoginbtn);

        backToLoginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
                finish();
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
        progressBar.setVisibility(View.VISIBLE);
        String email, password, passwordconfirm;
        email = Objects.requireNonNull(emailinput.getText()).toString().trim();
        password = Objects.requireNonNull(passwordinput.getText()).toString().trim();
        passwordconfirm = Objects.requireNonNull(confirminput.getText()).toString().trim();

        if (email.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Email address is required!", Toast.LENGTH_SHORT).show();
            emailinput.findViewById(R.id.emailinput).requestFocus();
        }
        //validate to check if email format is invalid
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Email address is invalid!", Toast.LENGTH_SHORT).show();
            emailinput.findViewById(R.id.emailinput).requestFocus();
        }
        //validate to check if password is empty
        else if (password.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Password is required!", Toast.LENGTH_SHORT).show();
            passwordinput.findViewById(R.id.passwordinput).requestFocus();
        }
        //validate to check if password is less than 8 characters
        else if (password.length() < 6) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Password should be at least 6 characters!", Toast.LENGTH_SHORT).show();
            passwordinput.findViewById(R.id.passwordinput).requestFocus();
        }
        //validate to check if confirm password is empty
        else if (passwordconfirm.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            confirminput.setError("Confirm password is required!");
            Toast.makeText(this, "Confirm password is required!", Toast.LENGTH_SHORT).show();
            confirminput.findViewById(R.id.confirminput).requestFocus();
        }
        //validate to check if both password match
        else if (!passwordconfirm.equals(password)) {
            progressBar.setVisibility(View.GONE);
            confirminput.setError("Both passwords does not match!");
            Toast.makeText(this, "Both passwords does not match!", Toast.LENGTH_SHORT).show();
            confirminput.findViewById(R.id.confirminput).requestFocus();
        } else {
            // create an account
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Account Created",
                                    Toast.LENGTH_SHORT).show();
                            // Redirect to the PersonalInformation class
                            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Authentication Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}