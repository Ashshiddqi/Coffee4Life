package com.example.coffee4life.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coffee4life.R;
import com.example.coffee4life.network.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {

    public interface OnItemClick {
        void onClick(Category item);
    }

    private final List<Category> items = new ArrayList<>();
    private final OnItemClick onItemClick;

    public CategoryAdapter(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void setItems(List<Category> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Category c = items.get(position);
        holder.tvName.setText(c.name != null ? c.name : "-");
        if (c.description != null && !c.description.trim().isEmpty()) {
            holder.tvDesc.setVisibility(View.VISIBLE);
            holder.tvDesc.setText(c.description);
        } else {
            holder.tvDesc.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(c);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDesc = itemView.findViewById(R.id.tv_description);
        }
    }
}
