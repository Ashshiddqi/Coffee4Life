package com.example.coffee4life;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coffee4life.network.models.OrderItemResponse;
import com.example.coffee4life.network.repositories.OrderItemRepository;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailDialogFragment extends DialogFragment {

    private RecyclerView rvItems;
    private TextView tvOrderNumber, tvCustomer, tvSubtotal, tvCash, tvChange;
    private Button btnClose;
    private ItemsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.dialog_order_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvOrderNumber = view.findViewById(R.id.tv_order_number);
        tvCustomer = view.findViewById(R.id.tv_customer);
        tvSubtotal = view.findViewById(R.id.tv_subtotal);
        tvCash = view.findViewById(R.id.tv_cash);
        tvChange = view.findViewById(R.id.tv_change);
        rvItems = view.findViewById(R.id.rv_items);
        btnClose = view.findViewById(R.id.btn_close);

        rvItems.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ItemsAdapter();
        rvItems.setAdapter(adapter);

        Bundle args = getArguments();
        if (args != null) {
            String orderId = args.getString("order_id");
            String orderNumber = args.getString("order_number");
            String customerName = args.getString("customer_name");
            int subtotal = args.getInt("subtotal", 0);
            int cash = args.getInt("cash", 0);
            int change = args.getInt("change", 0);

            tvOrderNumber.setText("Order #" + orderNumber);
            tvCustomer.setText("Customer: " + (customerName != null ? customerName : "-"));
            tvSubtotal.setText("Subtotal: " + OrdersStore.formatRupiah(subtotal));
            tvCash.setText("Cash: " + OrdersStore.formatRupiah(cash));
            tvChange.setText("Change: " + OrdersStore.formatRupiah(change));

            if (orderId != null) {
                OrderItemRepository repo = new OrderItemRepository(requireContext());
                repo.list(orderId, new OrderItemRepository.CallbackList() {
                    @Override public void onSuccess(List<OrderItemResponse> items) {
                        if (!isAdded()) return;
                        adapter.setItems(items);
                    }
                    @Override public void onError(String message) {
                        if (!isAdded()) return;
                        adapter.setItems(new ArrayList<>());
                    }
                });
            }
        }

        btnClose.setOnClickListener(v -> dismiss());
    }

    static class ItemsAdapter extends RecyclerView.Adapter<ItemVH> {
        private List<OrderItemResponse> items = new ArrayList<>();
        void setItems(List<OrderItemResponse> data) {
            items = (data != null) ? data : new ArrayList<>();
            notifyDataSetChanged();
        }
        @NonNull @Override public ItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ItemVH(v);
        }
        @Override public void onBindViewHolder(@NonNull ItemVH holder, int position) {
            if (items.isEmpty()) {
                holder.t1.setText("Tidak ada item");
                holder.t2.setText("");
                return;
            }
            OrderItemResponse it = items.get(position);
            String title = (it.product_name != null ? it.product_name : (it.product_id != null ? it.product_id : "Item"));
            String sub = "x" + (it.qty != null ? it.qty : 0) + " • " + OrdersStore.formatRupiah(it.price != null ? it.price : 0);
            holder.t1.setText(title);
            holder.t2.setText(sub);
        }
        @Override public int getItemCount() { return Math.max(items.size(), 1); }
    }

    static class ItemVH extends RecyclerView.ViewHolder {
        final TextView t1;
        final TextView t2;
        ItemVH(@NonNull View itemView) {
            super(itemView);
            t1 = itemView.findViewById(android.R.id.text1);
            t2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
