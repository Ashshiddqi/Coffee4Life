package com.example.coffee4life;

import android.os.Bundle;
import android.view.View;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.example.coffee4life.auth.SessionManager;

public class ProfileManagementActivity extends AppCompatActivity {

    private CardView cardCategories, cardProducts;
    private TextView tvUserName;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_management);

        cardCategories = findViewById(R.id.card_categories);
        cardProducts = findViewById(R.id.card_products);
        tvUserName = findViewById(R.id.tv_user_name);
        btnLogout = findViewById(R.id.btn_logout);

        cardCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileManagementActivity.this, CategoryListActivity.class);
                startActivity(intent);
            }
        });

        cardProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileManagementActivity.this, ProductListActivity.class);
                startActivity(intent);
            }
        });

        SessionManager sm = new SessionManager(this);
        String display = sm.getDisplayName();
        if (display == null || display.trim().isEmpty()) {
            String email = sm.getEmail();
            display = email != null ? (email.contains("@") ? email.substring(0, email.indexOf("@")) : email) : "User";
        }
        if (tvUserName != null) {
            tvUserName.setText(display);
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(ProfileManagementActivity.this)
                            .setTitle("Konfirmasi Logout")
                            .setMessage("Yakin ingin keluar dari akun?")
                            .setNegativeButton("Batal", null)
                            .setPositiveButton("Logout", (dialog, which) -> {
                                SessionManager sm = new SessionManager(ProfileManagementActivity.this);
                                sm.clear();
                                Intent intent = new Intent(ProfileManagementActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .show();
                }
            });
        }
    }
}
