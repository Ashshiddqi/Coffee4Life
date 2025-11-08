package com.example.coffee4life;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainNavActivity extends AppCompatActivity implements MenuCategoryFragment.Navigator {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nav);

        bottomNav = findViewById(R.id.bottom_navigation);
        if (savedInstanceState == null) {
            replaceFragment(new MenuCategoryFragment(), false);
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    replaceFragment(new MenuCategoryFragment(), false);
                    return true;
                } else if (id == R.id.nav_cart) {
                    replaceFragment(new CartFragment(), false);
                    return true;
                } else if (id == R.id.nav_orders) {
                    replaceFragment(new OrdersFragment(), false);
                    return true;
                }
                return false;
            }
        });

        updateCartBadge();
    }

    private void replaceFragment(Fragment f, boolean addToBackStack) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, f);
        if (addToBackStack) tx.addToBackStack(null);
        tx.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }

    public void updateCartBadge() {
        if (bottomNav == null) return;
        BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_cart);
        int count = CartManager.count();
        if (count > 0) {
            badge.setVisible(true);
            badge.setNumber(count);
        } else {
            badge.clearNumber();
            badge.setVisible(false);
        }
    }

    // Navigator from Home (category click) -> open Product list, staying in Home tab
    @Override
    public void openCategory(String categoryId, String categoryName, String userName) {
        replaceFragment(ProductMenuFragment.newInstance(categoryId, categoryName, userName), true);
    }

    public void openOrdersTab() {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_orders);
        }
    }
}
