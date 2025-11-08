package com.example.coffee4life;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coffee4life.adapters.CategoryAdapter;
import com.example.coffee4life.network.models.Category;
import com.example.coffee4life.network.repositories.CategoryRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;

import java.util.List;

public class CategoryListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private CategoryAdapter adapter;
    private CategoryRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        recyclerView = findViewById(R.id.rv_categories);
        fabAdd = findViewById(R.id.fab_add_category);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryAdapter(item -> showEditDialog(item));
        recyclerView.setAdapter(adapter);

        repo = new CategoryRepository(this);
        loadCategories();

        fabAdd.setOnClickListener(v -> showAddDialog());
    }

    private void loadCategories() {
        repo.list(new CategoryRepository.CallbackList() {
            @Override
            public void onSuccess(List<Category> items) {
                adapter.setItems(items);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(CategoryListActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Category");
        LayoutInflater inflater = LayoutInflater.from(this);
        final android.view.View view = inflater.inflate(R.layout.dialog_add_edit_category, null);
        final EditText etName = view.findViewById(R.id.et_name);
        final EditText etDesc = view.findViewById(R.id.et_description);
        builder.setView(view);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            Category body = new Category(name, desc.isEmpty() ? null : desc);
            repo.create(body, new CategoryRepository.CallbackOne() {
                @Override
                public void onSuccess(Category item) {
                    Toast.makeText(CategoryListActivity.this, "Created", Toast.LENGTH_SHORT).show();
                    loadCategories();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(CategoryListActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showEditDialog(Category existing) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Category");
        LayoutInflater inflater = LayoutInflater.from(this);
        final android.view.View view = inflater.inflate(R.layout.dialog_add_edit_category, null);
        final EditText etName = view.findViewById(R.id.et_name);
        final EditText etDesc = view.findViewById(R.id.et_description);
        etName.setText(existing.name != null ? existing.name : "");
        etDesc.setText(existing.description != null ? existing.description : "");
        builder.setView(view);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            Category body = new Category(name, desc.isEmpty() ? null : desc);
            repo.update(existing.id, body, new CategoryRepository.CallbackOne() {
                @Override
                public void onSuccess(Category item) {
                    Toast.makeText(CategoryListActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                    loadCategories();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(CategoryListActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });

        builder.setNeutralButton("Delete", (dialog, which) -> confirmDelete(existing));
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void confirmDelete(Category existing) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '" + (existing.name != null ? existing.name : "this item") + "'?")
                .setPositiveButton("Delete", (d, w) -> {
                    repo.delete(existing.id, new CategoryRepository.CallbackVoid() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(CategoryListActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                            loadCategories();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(CategoryListActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
