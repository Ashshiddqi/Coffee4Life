package com.example.coffee4life.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coffee4life.R;
import com.bumptech.glide.Glide;
import com.example.coffee4life.network.models.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    public interface OnItemClick {
        void onClick(Product item);
    }

    private final List<Product> items = new ArrayList<>();
    private final OnItemClick onItemClick;
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public ProductAdapter(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void setItems(List<Product> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product p = items.get(position);
        holder.tvName.setText(p.name != null ? p.name : "-");
        String priceText = p.price != null ? currency.format(p.price) : "-";
        holder.tvPrice.setText("Price: " + priceText);
        holder.tvStock.setText("Stock: " + (p.stock != null ? p.stock : 0));
        ImageView iv = holder.ivThumb;
        if (p.image_url != null && !p.image_url.trim().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(p.image_url)
                    .placeholder(R.drawable.ic_cart)
                    .error(R.drawable.ic_cart)
                    .centerCrop()
                    .into(iv);
        } else {
            iv.setImageResource(R.drawable.ic_cart);
        }
        holder.itemView.setOnClickListener(v -> { if (onItemClick != null) onItemClick.onClick(p); });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvStock;
        ImageView ivThumb;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvStock = itemView.findViewById(R.id.tv_stock);
            ivThumb = itemView.findViewById(R.id.iv_thumb);
        }
    }
}
