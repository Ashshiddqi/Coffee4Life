package com.example.coffee4life;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.coffee4life.auth.AuthRepository;
import com.example.coffee4life.network.models.AuthResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etMobileNumber, etEmail, etPassword;
    private FloatingActionButton fabSignUp;
    private ImageButton btnBack;
    private TextView tvSignIn;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        initViews();

        // Set click listeners
        setClickListeners();

        authRepository = new AuthRepository(this);
    }

    private void initViews() {
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        fabSignUp = findViewById(R.id.fab_sign_up);
        btnBack = findViewById(R.id.btn_back);
        tvSignIn = findViewById(R.id.tv_sign_in);
    }

    private void setClickListeners() {
        // Back button click
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Sign up button click
        fabSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSignUp();
            }
        });

        // Sign in text click - navigate to login
        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Close sign up activity
            }
        });
    }

    private void handleSignUp() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Basic validation
        if (fullName.isEmpty()) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        if (fullName.length() < 2) {
            etFullName.setError("Please enter a valid full name");
            etFullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // If validation passes, proceed with registration
        performSignUp(fullName, email, password);
    }

    private void performSignUp(final String fullName, final String email, String password) {
        fabSignUp.setEnabled(false);
        authRepository.signUp(fullName, email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse res) {
                Toast.makeText(SignUpActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_LONG).show();
                fabSignUp.setEnabled(true);
            }
        });
    }
}