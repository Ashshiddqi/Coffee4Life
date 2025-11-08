package com.example.coffee4life;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coffee4life.network.models.OrderResponse;
import com.example.coffee4life.network.repositories.OrderRepository;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class OrdersFragment extends Fragment {
    private RecyclerView rv;
    private OrdersAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rv = view.findViewById(R.id.rv_orders);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrdersAdapter();
        rv.setAdapter(adapter);
        loadOrders();
    }

    private void loadOrders() {
        OrderRepository repo = new OrderRepository(requireContext());
        repo.list(new OrderRepository.CallbackList() {
            @Override public void onSuccess(java.util.List<OrderResponse> items) {
                if (!isAdded()) return;
                adapter.setItems(items);
            }
            @Override public void onError(String message) {
                if (!isAdded()) return;
                adapter.setItems(new java.util.ArrayList<>());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }

    static class OrdersAdapter extends RecyclerView.Adapter<OrderVH> {
        private java.util.List<OrderResponse> items = new java.util.ArrayList<>();
        void setItems(java.util.List<OrderResponse> data) {
            items = (data != null) ? data : new java.util.ArrayList<>();
            notifyDataSetChanged();
        }
        @NonNull @Override public OrderVH onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order, parent, false);
            return new OrderVH(v);
        }
        @Override public void onBindViewHolder(@NonNull OrderVH holder, int position) {
            if (items.isEmpty()) {
                holder.title.setText("Belum ada transaksi");
                holder.subtitle.setText("");
                holder.amount.setText("");
                return;
            }
            OrderResponse r = items.get(position);
            String title = "Order #" + (r.order_number != null ? r.order_number : "-");
            String formattedDate = formatIso(r.created_at);
            String sub = (r.customer_name != null ? r.customer_name : "-") + (formattedDate != null ? " • " + formattedDate : "");
            String amount = OrdersStore.formatRupiah(r.subtotal != null ? r.subtotal : 0);
            holder.title.setText(title);
            holder.subtitle.setText(sub);
            holder.amount.setText(amount);

            holder.itemView.setOnClickListener(v -> {
                android.os.Bundle args = new android.os.Bundle();
                args.putString("order_id", r.id);
                args.putString("order_number", r.order_number);
                args.putString("customer_name", r.customer_name);
                args.putInt("subtotal", r.subtotal != null ? r.subtotal : 0);
                args.putInt("cash", r.cash != null ? r.cash : 0);
                args.putInt("change", r.change != null ? r.change : 0);
                OrderDetailDialogFragment dialog = new OrderDetailDialogFragment();
                dialog.setArguments(args);
                androidx.fragment.app.FragmentActivity act = (androidx.fragment.app.FragmentActivity) v.getContext();
                dialog.show(act.getSupportFragmentManager(), "order_detail");
            });
        }
        @Override public int getItemCount() { return Math.max(items.size(), 1); }
    }

    static class OrderVH extends RecyclerView.ViewHolder {
        final android.widget.TextView title;
        final android.widget.TextView subtitle;
        final android.widget.TextView amount;
        OrderVH(@NonNull android.view.View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_title);
            subtitle = itemView.findViewById(R.id.tv_subtitle);
            amount = itemView.findViewById(R.id.tv_amount);
        }
    }

    @Nullable
    private static String formatIso(@Nullable String iso) {
        try {
            if (iso == null || iso.trim().isEmpty()) return null;
            OffsetDateTime odt = OffsetDateTime.parse(iso);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", new Locale("id","ID"));
            return odt.format(fmt);
        } catch (Exception e) {
            return iso; // fallback to raw if parsing fails
        }
    }
}
