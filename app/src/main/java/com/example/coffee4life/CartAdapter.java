package com.example.coffee4life;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private CartItemListener listener;

    public interface CartItemListener {
        void onQuantityChanged();
        void onItemDeleted(int position);
        void onQuantityDelta(int position, int delta);
    }

    public CartAdapter(List<CartItem> cartItems, CartItemListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivItemImage;
        private TextView tvItemName;
        private TextView tvItemDetails;
        private TextView tvQuantity;
        private TextView tvItemPrice;
        private ImageButton btnDecrease;
        private ImageButton btnIncrease;
        private View itemContent;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemImage = itemView.findViewById(R.id.iv_item_image);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvItemDetails = itemView.findViewById(R.id.tv_item_details);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvItemPrice = itemView.findViewById(R.id.tv_item_price);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            itemContent = itemView.findViewById(R.id.item_content);
        }

        public void bind(CartItem item, int position) {
            // Set item data
            tvItemName.setText(item.getName());
            tvItemDetails.setText(item.getDetails());
            tvQuantity.setText("x " + item.getQuantity());

            // Format price
            NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            String formattedPrice = rupiahFormat.format(item.getTotalPrice())
                    .replace("Rp", "Rp\n")
                    .replace(",00", "");
            tvItemPrice.setText(formattedPrice);

            // Load image URL if available
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(ivItemImage.getContext())
                        .load(item.getImageUrl())
                        .centerCrop()
                        .into(ivItemImage);
            }

            // Decrease quantity
            btnDecrease.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getBindingAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) return;
                    if (item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                        notifyItemChanged(pos);
                        listener.onQuantityChanged();
                        if (listener != null) listener.onQuantityDelta(pos, -1);
                    } else {
                        listener.onItemDeleted(pos);
                    }
                }
            });

            // Increase quantity
            btnIncrease.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getBindingAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) return;
                    if (item.getAvailableStock() > 0 && item.getQuantity() >= item.getAvailableStock()) {
                        Toast.makeText(v.getContext(), "Stok maksimal tercapai", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    item.setQuantity(item.getQuantity() + 1);
                    notifyItemChanged(pos);
                    listener.onQuantityChanged();
                    if (listener != null) listener.onQuantityDelta(pos, +1);
                }
            });

            // Swipe to delete functionality
            // For simple implementation, add long press to delete
            itemContent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = getBindingAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) return false;
                    listener.onItemDeleted(pos);
                    return true;
                }
            });
        }
    }
}