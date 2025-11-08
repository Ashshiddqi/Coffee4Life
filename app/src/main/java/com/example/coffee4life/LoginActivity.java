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
import com.example.coffee4life.auth.SessionManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private FloatingActionButton fabLogin;
    private ImageButton btnBack;
    private TextView tvSignUp;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        initViews();

        // Check if email was passed from SignUp activity
        handleIntentData();

        // Set click listeners
        setClickListeners();

        authRepository = new AuthRepository(this);
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        fabLogin = findViewById(R.id.fab_login);
        btnBack = findViewById(R.id.btn_back);
        tvSignUp = findViewById(R.id.tv_sign_up);
    }

    private void handleIntentData() {
        // Check if email was passed from SignUp activity
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("email")) {
            String email = intent.getStringExtra("email");
            etEmail.setText(email);
        }
    }

    private void setClickListeners() {
        // Back button click
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Login button click
        fabLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        // Sign up text click - navigate to sign up activity
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Basic validation
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

        // If validation passes, proceed with login
        performLogin(email, password);
    }

    private void performLogin(final String email, String password) {
        fabLogin.setEnabled(false);
        authRepository.signIn(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse res) {
                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainNavActivity.class);
                String userName = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
                try {
                    if (res != null && res.user != null && res.user.user_metadata != null) {
                        Object dn = res.user.user_metadata.get("display_name");
                        Object fn = res.user.user_metadata.get("full_name");
                        if (dn != null && dn.toString().trim().length() > 0) {
                            userName = dn.toString();
                        } else if (fn != null && fn.toString().trim().length() > 0) {
                            userName = fn.toString();
                        }
                    }
                } catch (Exception ignored) {}
                // Persist display name for later usage across app
                try {
                    SessionManager sm = new SessionManager(LoginActivity.this);
                    sm.setDisplayName(userName);
                } catch (Exception ignored) {}
                intent.putExtra("user_name", userName);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                fabLogin.setEnabled(true);
            }
        });
    }
}