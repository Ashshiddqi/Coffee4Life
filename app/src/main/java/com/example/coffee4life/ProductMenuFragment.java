package com.example.coffee4life;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coffee4life.adapters.ProductAdapter;
import com.example.coffee4life.network.models.Product;
import com.example.coffee4life.network.repositories.ProductRepository;

import java.util.List;

public class ProductMenuFragment extends Fragment {

    private static final String ARG_CAT_ID = "category_id";
    private static final String ARG_CAT_NAME = "category_name";
    private static final String ARG_USER_NAME = "user_name";

    public static ProductMenuFragment newInstance(String catId, String catName, String userName) {
        ProductMenuFragment f = new ProductMenuFragment();
        Bundle b = new Bundle();
        b.putString(ARG_CAT_ID, catId);
        b.putString(ARG_CAT_NAME, catName);
        b.putString(ARG_USER_NAME, userName);
        f.setArguments(b);
        return f;
    }

    private RecyclerView rv;
    private ProductAdapter adapter;
    private ProductRepository repo;
    private String selectedCategoryId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        String userName = args != null ? args.getString(ARG_USER_NAME) : null;
        String categoryName = args != null ? args.getString(ARG_CAT_NAME) : null;
        selectedCategoryId = args != null ? args.getString(ARG_CAT_ID) : null;

        TextView tvUser = view.findViewById(R.id.tv_user_name);
        if (tvUser != null && userName != null) tvUser.setText(userName);

        TextView tvTitle = view.findViewById(R.id.tv_title);
        if (tvTitle != null && categoryName != null && !categoryName.isEmpty()) {
            tvTitle.setText("Select your " + categoryName);
        }

        ImageView ivProfile = view.findViewById(R.id.iv_profile);
        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> {
                if (getActivity() instanceof MainNavActivity) {
                    // Could open profile activity if needed
                }
            });
        }

        rv = view.findViewById(R.id.rv_products);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new ProductAdapter(item -> {
            boolean ok = CartManager.add(item);
            if (ok) {
                if (getActivity() instanceof MainNavActivity) {
                    ((MainNavActivity) getActivity()).updateCartBadge();
                }
                Toast.makeText(getContext(), "Added to cart", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Stok tidak mencukupi", Toast.LENGTH_SHORT).show();
            }
        });
        rv.setAdapter(adapter);

        repo = new ProductRepository(requireContext());
        loadProducts();
    }

    private void loadProducts() {
        ProductRepository.CallbackList cb = new ProductRepository.CallbackList() {
            @Override
            public void onSuccess(List<Product> items) {
                adapter.setItems(items);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        };
        if (selectedCategoryId != null && !selectedCategoryId.isEmpty()) {
            repo.listFiltered(selectedCategoryId, null, null, cb);
        } else {
            repo.list(cb);
        }
    }
}
