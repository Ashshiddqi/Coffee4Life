package com.example.coffee4life;

import com.example.coffee4life.network.models.Product;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CartManager {
    private static final List<Product> ITEMS = new ArrayList<>();

    public static synchronized boolean add(Product p) {
        if (p == null) return false;
        // Enforce stock: count existing items with same id
        if (p.stock != null && p.stock >= 0) {
            String key = p.id != null ? p.id : p.name;
            int current = 0;
            for (Product it : ITEMS) {
                if (it == null) continue;
                String k2 = it.id != null ? it.id : it.name;
                if (key != null && key.equals(k2)) current++;
            }
            if (current >= p.stock) {
                return false; // would exceed stock
            }
        }
        ITEMS.add(p);
        return true;
    }

    public static synchronized int count() {
        return ITEMS.size();
    }

    public static synchronized List<Product> items() {
        return Collections.unmodifiableList(ITEMS);
    }

    public static synchronized void clear() {
        ITEMS.clear();
    }

    public static synchronized void removeByKey(String productId, String name, int qty) {
        if (qty <= 0) return;
        String key = productId != null ? productId : name;
        int removed = 0;
        for (int i = 0; i < ITEMS.size() && removed < qty; ) {
            Product it = ITEMS.get(i);
            String k2 = it != null ? (it.id != null ? it.id : it.name) : null;
            if (key != null && key.equals(k2)) {
                ITEMS.remove(i);
                removed++;
            } else {
                i++;
            }
        }
    }
}
