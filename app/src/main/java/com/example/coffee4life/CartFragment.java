package com.example.coffee4life;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

import com.example.coffee4life.network.models.OrderRequest;
import com.example.coffee4life.network.repositories.OrderRepository;

public class CartFragment extends Fragment {

    private ImageButton btnBack;
    private RecyclerView rvCartItems;
    private TextView tvTotalPrice;
    private CardView btnNext;

    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private int totalPrice = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnBack = view.findViewById(R.id.btn_back);
        rvCartItems = view.findViewById(R.id.rv_cart_items);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);
        btnNext = view.findViewById(R.id.btn_next);

        // Hide back button because we use persistent bottom navigation
        if (btnBack != null) btnBack.setVisibility(View.GONE);

        setupRecyclerView();
        setClickListeners();
        calculateTotal();
    }

    private void setupRecyclerView() {
        cartItems = new ArrayList<>();
        if (CartManager.count() > 0) {
            Map<String, Integer> qtyMap = new HashMap<>();
            Map<String, com.example.coffee4life.network.models.Product> prodMap = new HashMap<>();
            for (com.example.coffee4life.network.models.Product p : CartManager.items()) {
                if (p == null) continue;
                String key = (p.id != null && !p.id.isEmpty()) ? p.id : (p.name != null ? p.name : String.valueOf(p.hashCode()));
                qtyMap.put(key, qtyMap.getOrDefault(key, 0) + 1);
                if (!prodMap.containsKey(key)) {
                    prodMap.put(key, p);
                }
            }
            for (Map.Entry<String, Integer> e : qtyMap.entrySet()) {
                com.example.coffee4life.network.models.Product p = prodMap.get(e.getKey());
                String name = p != null && p.name != null ? p.name : "Product";
                int price = (p != null && p.price != null) ? p.price.intValue() : 0;
                CartItem ci = new CartItem(name, "default", price, e.getValue());
                if (p != null) {
                    if (p.image_url != null) ci.setImageUrl(p.image_url);
                    if (p.id != null) ci.setProductId(p.id);
                    if (p.stock != null) ci.setAvailableStock(p.stock);
                }
                cartItems.add(ci);
            }
        }
        cartAdapter = new CartAdapter(cartItems, new CartAdapter.CartItemListener() {
            @Override public void onQuantityChanged() { calculateTotal(); }
            @Override public void onItemDeleted(int position) {
                if (position >= 0 && position < cartItems.size()) {
                    CartItem removed = cartItems.get(position);
                    // Remove corresponding entries from CartManager
                    com.example.coffee4life.CartManager.removeByKey(removed.getProductId(), removed.getName(), removed.getQuantity());
                    cartItems.remove(position);
                    cartAdapter.notifyItemRemoved(position);
                    calculateTotal();
                    if (getActivity() instanceof MainNavActivity) {
                        ((MainNavActivity) getActivity()).updateCartBadge();
                    }
                }
            }
            @Override public void onQuantityDelta(int position, int delta) {
                if (position >= 0 && position < cartItems.size()) {
                    CartItem it = cartItems.get(position);
                    if (delta > 0) {
                        // Add one to CartManager to keep badge in sync (respecting stock in CartAdapter)
                        com.example.coffee4life.network.models.Product p = new com.example.coffee4life.network.models.Product();
                        p.id = it.getProductId();
                        p.name = it.getName();
                        p.stock = it.getAvailableStock();
                        com.example.coffee4life.CartManager.add(p);
                    } else if (delta < 0) {
                        com.example.coffee4life.CartManager.removeByKey(it.getProductId(), it.getName(), 1);
                    }
                    if (getActivity() instanceof MainNavActivity) {
                        ((MainNavActivity) getActivity()).updateCartBadge();
                    }
                }
            }
        });
        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCartItems.setAdapter(cartAdapter);
    }

    private void setClickListeners() {
        // Back disabled: bottom navigation handles navigation
        btnNext.setOnClickListener(v -> showPaymentDialog());
    }

    private void calculateTotal() {
        totalPrice = 0;
        for (CartItem item : cartItems) {
            totalPrice += item.getPrice() * item.getQuantity();
        }
        NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String formattedPrice = rupiahFormat.format(totalPrice).replace("Rp", "Rp ");
        tvTotalPrice.setText(formattedPrice);
    }

    private void showPaymentDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_payment);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etCustomerName = dialog.findViewById(R.id.et_customer_name);
        EditText etMoney = dialog.findViewById(R.id.et_money);
        TextView tvChange = dialog.findViewById(R.id.tv_change);
        TextView tvAmount = dialog.findViewById(R.id.tv_amount);
        TextView tvTotalPriceDialog = dialog.findViewById(R.id.tv_total_price_dialog);
        CardView btnPayNow = dialog.findViewById(R.id.btn_pay_now);
        TextView tvUserLabel = dialog.findViewById(R.id.tv_user_label);

        // Set logged-in user label (prefer Supabase display name)
        try {
            com.example.coffee4life.auth.SessionManager sm = new com.example.coffee4life.auth.SessionManager(requireContext());
            String display = sm.getDisplayName();
            if (display == null || display.trim().isEmpty()) {
                String email = sm.getEmail();
                display = email != null ? (email.contains("@") ? email.substring(0, email.indexOf("@")) : email) : "user";
            }
            if (tvUserLabel != null) tvUserLabel.setText(display);
        } catch (Exception ignored) {}

        NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String formattedPrice = rupiahFormat.format(totalPrice).replace("Rp", "Rp ");
        tvAmount.setText(formattedPrice);
        tvTotalPriceDialog.setText(formattedPrice);

        etMoney.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    try {
                        int money = Integer.parseInt(s.toString());
                        int change = money - totalPrice;
                        if (change >= 0) {
                            String formattedChange = rupiahFormat.format(change).replace("Rp", "Rp : ");
                            tvChange.setText(formattedChange);
                        } else {
                            tvChange.setText("Uang tidak cukup");
                        }
                    } catch (NumberFormatException e) {
                        tvChange.setText("Rp : 0");
                    }
                } else {
                    tvChange.setText("Rp : 0");
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnPayNow.setOnClickListener(v -> {
            String customerName = etCustomerName.getText().toString().trim();
            String moneyStr = etMoney.getText().toString().trim();
            if (customerName.isEmpty()) { Toast.makeText(getContext(), "Masukkan nama pelanggan", Toast.LENGTH_SHORT).show(); return; }
            if (moneyStr.isEmpty()) { Toast.makeText(getContext(), "Masukkan jumlah uang", Toast.LENGTH_SHORT).show(); return; }
            try {
                int money = Integer.parseInt(moneyStr);
                if (money < totalPrice) { Toast.makeText(getContext(), "Uang tidak cukup", Toast.LENGTH_SHORT).show(); return; }
                int change = money - totalPrice;
                String changeStr = rupiahFormat.format(change).replace("Rp", "Rp ");

                // Prepare order request for Supabase (align with schema)
                long orderId = System.currentTimeMillis();
                String orderNumber = String.valueOf(orderId);
                OrderRequest request = new OrderRequest(orderNumber, customerName, totalPrice, money, change);

                // Disable button to prevent double submit
                btnPayNow.setEnabled(false);

                OrderRepository repo = new OrderRepository(requireContext());
                repo.create(request, new OrderRepository.CallbackOrderId() {
                    @Override public void onSuccess(String orderId) {
                        if (!isAdded()) return;
                        // After order created, insert order_items in bulk
                        com.example.coffee4life.network.repositories.OrderItemRepository itemRepo = new com.example.coffee4life.network.repositories.OrderItemRepository(requireContext());
                        java.util.ArrayList<com.example.coffee4life.network.models.OrderItemRequest> payload = new java.util.ArrayList<>();
                        for (CartItem ci : cartItems) {
                            payload.add(new com.example.coffee4life.network.models.OrderItemRequest(
                                    orderId,
                                    ci.getProductId(),
                                    ci.getQuantity(),
                                    ci.getPrice(),
                                    ci.getName()
                            ));
                        }
                        itemRepo.bulkInsert(payload, new com.example.coffee4life.network.repositories.OrderItemRepository.CallbackVoid() {
                            @Override public void onSuccess() {
                                if (!isAdded()) return;
                                // Decrement stock for each product
                                com.example.coffee4life.network.repositories.ProductRepository pr = new com.example.coffee4life.network.repositories.ProductRepository(requireContext());
                                for (CartItem ci : cartItems) {
                                    if (ci.getProductId() != null) {
                                        int newStock = Math.max(0, ci.getAvailableStock() - ci.getQuantity());
                                        com.example.coffee4life.network.models.Product body = new com.example.coffee4life.network.models.Product();
                                        body.stock = newStock;
                                        pr.update(ci.getProductId(), body, new com.example.coffee4life.network.repositories.ProductRepository.CallbackOne() {
                                            @Override public void onSuccess(com.example.coffee4life.network.models.Product item) { /* ignore */ }
                                            @Override public void onError(String message) { /* ignore for now */ }
                                        });
                                    }
                                }
                                Toast.makeText(getContext(), "Pembayaran berhasil!\nKembalian: " + changeStr, Toast.LENGTH_LONG).show();
                                dialog.dismiss();

                                cartItems.clear();
                                CartManager.clear();
                                if (getActivity() instanceof MainNavActivity) {
                                    ((MainNavActivity) getActivity()).openOrdersTab();
                                    ((MainNavActivity) getActivity()).updateCartBadge();
                                }
                            }

                            @Override public void onError(String message) {
                                if (!isAdded()) return;
                                btnPayNow.setEnabled(true);
                                Toast.makeText(getContext(), "Gagal menyimpan order items: " + (message != null ? message : "unknown error"), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override public void onError(String message) {
                        if (!isAdded()) return;
                        btnPayNow.setEnabled(true);
                        Toast.makeText(getContext(), "Gagal menyimpan order: " + (message != null ? message : "unknown error"), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Jumlah uang tidak valid", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
