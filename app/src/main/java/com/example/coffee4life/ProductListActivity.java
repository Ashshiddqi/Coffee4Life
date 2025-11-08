package com.example.coffee4life;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coffee4life.network.models.Product;
import com.example.coffee4life.network.repositories.ProductRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.view.LayoutInflater;
import android.app.AlertDialog;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import android.content.Intent;
import android.net.Uri;

public class ProductListActivity extends AppCompatActivity {

    private RecyclerView rv;
    private FloatingActionButton fab;
    private ProductsAdapter adapter;
    private ProductRepository repo;
    private com.example.coffee4life.network.repositories.CategoryRepository categoryRepo;
    private com.example.coffee4life.network.repositories.StorageRepository storageRepo;

    private static final int REQ_PICK_IMAGE = 2011;
    private ImageView currentIvPreview;
    private TextView currentTvImageStatus;
    private String selectedImageUrl; // result of upload
    private AlertDialog activeDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        rv = findViewById(R.id.rv_products);
        fab = findViewById(R.id.fab_add_products);
        rv.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));
        rv.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(8), true));
        adapter = new ProductsAdapter(product -> showProductDialog(product), product -> confirmDelete(product));
        rv.setAdapter(adapter);
        repo = new ProductRepository(this);
        categoryRepo = new com.example.coffee4life.network.repositories.CategoryRepository(this);
        storageRepo = new com.example.coffee4life.network.repositories.StorageRepository(this);
        loadProducts();

        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { showProductDialog(null); }
            });
        }
    }

    private void confirmDelete(Product p) {
        if (p == null || p.id == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete '" + (p.name != null ? p.name : "-") + "'?")
                .setPositiveButton("Delete", (d, w) -> {
                    repo.delete(p.id, new ProductRepository.CallbackVoid() {
                        @Override public void onSuccess() {
                            Toast.makeText(ProductListActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                            loadProducts();
                        }
                        @Override public void onError(String message) {
                            Toast.makeText(ProductListActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadProducts() {
        repo.list(new ProductRepository.CallbackList() {
            @Override public void onSuccess(List<Product> items) {
                adapter.setItems(items);
            }
            @Override public void onError(String message) {
                Toast.makeText(ProductListActivity.this, message, Toast.LENGTH_LONG).show();
                adapter.setItems(new ArrayList<>());
            }
        });
    }

    private void showProductDialog(@Nullable Product existing) {
        boolean isEdit = existing != null && existing.id != null;
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(isEdit ? "Edit Product" : "Add Product");
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_product, null, false);
        EditText etName = content.findViewById(R.id.et_name);
        EditText etPrice = content.findViewById(R.id.et_price);
        EditText etStock = content.findViewById(R.id.et_stock);
        Spinner spCategory = content.findViewById(R.id.sp_category);
        TextView tvImageStatus = content.findViewById(R.id.tv_image_status);
        ImageView ivPreview = content.findViewById(R.id.iv_preview);

        // Reset preview context for this dialog
        currentIvPreview = ivPreview;
        currentTvImageStatus = tvImageStatus;
        selectedImageUrl = null;

        content.findViewById(R.id.btn_choose_image).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Image"), REQ_PICK_IMAGE);
        });

        // Load categories
        final List<com.example.coffee4life.network.models.Category> categories = new ArrayList<>();
        final List<String> displayNames = new ArrayList<>();
        ArrayAdapter<String> adapterCat = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, displayNames);
        spCategory.setAdapter(adapterCat);
        categoryRepo.list(new com.example.coffee4life.network.repositories.CategoryRepository.CallbackList() {
            @Override public void onSuccess(List<com.example.coffee4life.network.models.Category> items) {
                categories.clear(); displayNames.clear();
                if (items != null) {
                    categories.addAll(items);
                    for (com.example.coffee4life.network.models.Category c : items) displayNames.add(c.name);
                }
                adapterCat.notifyDataSetChanged();
                if (existing != null && existing.category_id != null) {
                    for (int i = 0; i < categories.size(); i++) {
                        if (existing.category_id.equals(categories.get(i).id)) { spCategory.setSelection(i); break; }
                    }
                }
            }
            @Override public void onError(String message) { /* ignore for now */ }
        });

        if (isEdit) {
            etName.setText(existing.name);
            etPrice.setText(existing.price != null ? existing.price.toPlainString() : "");
            etStock.setText(existing.stock != null ? String.valueOf(existing.stock) : "");
            if (existing.image_url != null) {
                tvImageStatus.setText("Image selected");
                ivPreview.setVisibility(View.VISIBLE);
                Glide.with(this).load(existing.image_url).centerCrop().into(ivPreview);
                selectedImageUrl = existing.image_url;
            }
        }

        b.setView(content);
        b.setPositiveButton(isEdit ? "Update" : "Save", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String pStr = etPrice.getText().toString().trim();
            String sStr = etStock.getText().toString().trim();
            if (name.isEmpty() || pStr.isEmpty() || sStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                BigDecimal price = new BigDecimal(pStr);
                Integer stock = Integer.parseInt(sStr);
                String categoryId = null;
                int idx = spCategory.getSelectedItemPosition();
                if (idx >= 0 && idx < categories.size()) categoryId = categories.get(idx).id;
                Product body = new Product(name, price, stock, categoryId, selectedImageUrl);
                if (isEdit) {
                    repo.update(existing.id, body, new ProductRepository.CallbackOne() {
                        @Override public void onSuccess(Product item) {
                            Toast.makeText(ProductListActivity.this, "Product updated", Toast.LENGTH_SHORT).show();
                            loadProducts();
                        }
                        @Override public void onError(String message) {
                            Toast.makeText(ProductListActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    repo.create(body, new ProductRepository.CallbackOne() {
                        @Override public void onSuccess(Product item) {
                            Toast.makeText(ProductListActivity.this, "Product added", Toast.LENGTH_SHORT).show();
                            loadProducts();
                        }
                        @Override public void onError(String message) {
                            Toast.makeText(ProductListActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } catch (Exception e) {
                Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
            }
        });
        b.setNegativeButton("Cancel", null);
        if (!isFinishing() && !isDestroyed()) {
            // Dismiss previous dialog if any to avoid leaks
            if (activeDialog != null && activeDialog.isShowing()) activeDialog.dismiss();
            activeDialog = b.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            if (storageRepo == null) storageRepo = new com.example.coffee4life.network.repositories.StorageRepository(this);
            storageRepo.uploadProductImage(this, uri, new com.example.coffee4life.network.repositories.StorageRepository.CallbackString() {
                @Override public void onSuccess(String url) {
                    runOnUiThread(() -> {
                        selectedImageUrl = url;
                        if (currentTvImageStatus != null) currentTvImageStatus.setText("Image selected");
                        if (currentIvPreview != null) {
                            currentIvPreview.setVisibility(View.VISIBLE);
                            Glide.with(ProductListActivity.this).load(url).centerCrop().into(currentIvPreview);
                        }
                    });
                }
                @Override public void onError(String message) {
                    runOnUiThread(() -> Toast.makeText(ProductListActivity.this, "Upload failed: " + message, Toast.LENGTH_LONG).show());
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        if (activeDialog != null && activeDialog.isShowing()) {
            activeDialog.dismiss();
            activeDialog = null;
        }
        super.onDestroy();
    }

    static class ProductsAdapter extends RecyclerView.Adapter<VH> {
        interface OnItemClick { void onClick(Product p); }
        interface OnItemLongClick { void onLongClick(Product p); }
        private final List<Product> items = new ArrayList<>();
        private final OnItemClick click;
        private final OnItemLongClick longClick;
        ProductsAdapter(OnItemClick click, OnItemLongClick longClick) { this.click = click; this.longClick = longClick; }
        void setItems(List<Product> data) {
            items.clear();
            if (data != null) items.addAll(data);
            notifyDataSetChanged();
        }
        @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_product, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(VH holder, int position) {
            if (items.isEmpty()) {
                holder.tvName.setText("No products");
                holder.tvPrice.setText("");
                holder.tvStock.setText("");
                holder.itemView.setOnClickListener(null);
                holder.ivThumb.setImageResource(R.drawable.ic_cart);
                return;
            }
            Product p = items.get(position);
            holder.tvName.setText(p.name != null ? p.name : "-");
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id","ID"));
            String price = (p.price != null ? nf.format(p.price) : "Rp 0").replace("Rp", "Rp ");
            holder.tvPrice.setText(price);
            holder.tvStock.setText(p.stock != null ? ("Stock: " + p.stock) : "");
            if (p.image_url != null && !p.image_url.isEmpty()) {
                Glide.with(holder.ivThumb.getContext()).load(p.image_url).centerCrop().into(holder.ivThumb);
            } else {
                holder.ivThumb.setImageResource(R.drawable.ic_cart);
            }
            holder.itemView.setOnClickListener(v -> { if (click != null) click.onClick(p); });
            holder.itemView.setOnLongClickListener(v -> { if (longClick != null) longClick.onLongClick(p); return true; });
        }
        @Override public int getItemCount() { return Math.max(items.size(), 1); }
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView ivThumb;
        final TextView tvName;
        final TextView tvPrice;
        final TextView tvStock;
        VH(View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_thumb);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvStock = itemView.findViewById(R.id.tv_stock);
        }
    }

    // Simple grid spacing decoration
    static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount;
        private final int spacing;
        private final boolean includeEdge;
        GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }
        @Override
        public void getItemOffsets(android.graphics.Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column
            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;
                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
