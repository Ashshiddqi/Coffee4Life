package com.example.coffee4life.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.coffee4life.R;

import java.util.ArrayList;
import java.util.List;

public class MenuCategoryAdapter extends RecyclerView.Adapter<MenuCategoryAdapter.VH> {

    public static class Item {
        public final String id;
        public final String title;
        public final String imageUrl; // can be null
        public Item(String id, String title, String imageUrl) {
            this.id = id;
            this.title = title;
            this.imageUrl = imageUrl;
        }
    }

    public interface OnItemClick { void onClick(Item item); }

    private final List<Item> items = new ArrayList<>();
    private final OnItemClick onItemClick;

    public MenuCategoryAdapter(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void setItems(List<Item> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu_category, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Item it = items.get(position);
        holder.tvTitle.setText(it.title != null ? it.title : "-");
        if (it.imageUrl != null && !it.imageUrl.trim().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(it.imageUrl)
                    .placeholder(R.drawable.ic_cart)
                    .error(R.drawable.ic_cart)
                    .centerCrop()
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_cart);
        }
        holder.itemView.setOnClickListener(v -> { if (onItemClick != null) onItemClick.onClick(it); });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivImage; TextView tvTitle;
        VH(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }
    }
}
