package com.example.coffee4life;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coffee4life.adapters.MenuCategoryAdapter;
import com.example.coffee4life.network.models.Category;
import com.example.coffee4life.network.models.Product;
import com.example.coffee4life.network.repositories.CategoryRepository;
import com.example.coffee4life.network.repositories.ProductRepository;

import java.util.ArrayList;
import java.util.List;

public class MenuCategoryFragment extends Fragment {

    public interface Navigator {
        void openCategory(String categoryId, String categoryName, String userName);
        void updateCartBadge();
    }

    private TextView tvUserName;
    private ImageView ivProfile;
    private RecyclerView rvCategories;
    private MenuCategoryAdapter adapter;
    private CategoryRepository categoryRepo;
    private ProductRepository productRepo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvUserName = view.findViewById(R.id.tv_user_name);
        ivProfile = view.findViewById(R.id.iv_profile);
        rvCategories = view.findViewById(R.id.rv_menu_categories);

        // Prefer Supabase display name from SessionManager
        String userName = null;
        try {
            com.example.coffee4life.auth.SessionManager sm = new com.example.coffee4life.auth.SessionManager(requireContext());
            userName = sm.getDisplayName();
            if ((userName == null || userName.trim().isEmpty())) {
                // fallback to intent extra
                if (getActivity() != null && getActivity().getIntent() != null) {
                    userName = getActivity().getIntent().getStringExtra("user_name");
                }
            }
            if (userName == null || userName.trim().isEmpty()) {
                // final fallback to email-derived
                String email = sm.getEmail();
                userName = (email != null) ? (email.contains("@") ? email.substring(0, email.indexOf("@")) : email) : null;
            }
        } catch (Exception ignored) {}
        tvUserName.setText(userName != null ? userName : "User");

        ivProfile.setOnClickListener(v -> {
            if (getActivity() == null) return;
            Intent intent = new Intent(getActivity(), ProfileManagementActivity.class);
            intent.putExtra("user_name", tvUserName.getText().toString());
            startActivity(intent);
        });

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MenuCategoryAdapter(item -> {
            if (getActivity() instanceof Navigator) {
                ((Navigator) getActivity()).openCategory(item.id, item.title, tvUserName.getText().toString());
            }
        });
        rvCategories.setAdapter(adapter);

        categoryRepo = new CategoryRepository(requireContext());
        productRepo = new ProductRepository(requireContext());
        loadCategoriesWithImages();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh user name (prefer display name)
        String refreshed = null;
        try {
            com.example.coffee4life.auth.SessionManager sm = new com.example.coffee4life.auth.SessionManager(requireContext());
            refreshed = sm.getDisplayName();
            if (refreshed == null || refreshed.trim().isEmpty()) {
                String email = sm.getEmail();
                refreshed = (email != null) ? (email.contains("@") ? email.substring(0, email.indexOf("@")) : email) : null;
            }
        } catch (Exception ignored) {}
        if (tvUserName != null) tvUserName.setText(refreshed != null ? refreshed : tvUserName.getText());
    }

    private void loadCategoriesWithImages() {
        categoryRepo.list(new CategoryRepository.CallbackList() {
            @Override
            public void onSuccess(List<Category> items) {
                if (items == null) items = new ArrayList<>();
                List<MenuCategoryAdapter.Item> display = new ArrayList<>();

                if (items.isEmpty()) {
                    adapter.setItems(display);
                    return;
                }

                final int[] remaining = {items.size()};
                for (Category c : items) {
                    display.add(new MenuCategoryAdapter.Item(c.id, c.name, null));
                    productRepo.listFiltered(c.id, 1, "created_at.asc", new ProductRepository.CallbackList() {
                        @Override
                        public void onSuccess(List<Product> products) {
                            if (products != null && !products.isEmpty()) {
                                Product p = products.get(0);
                                for (int i = 0; i < display.size(); i++) {
                                    if (display.get(i).id.equals(c.id)) {
                                        display.set(i, new MenuCategoryAdapter.Item(c.id, c.name, p.image_url));
                                        break;
                                    }
                                }
                            }
                            if (--remaining[0] == 0) adapter.setItems(display);
                        }

                        @Override
                        public void onError(String message) {
                            if (--remaining[0] == 0) adapter.setItems(display);
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
