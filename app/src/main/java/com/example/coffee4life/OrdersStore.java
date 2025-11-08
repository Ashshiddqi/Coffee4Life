package com.example.coffee4life;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrdersStore {
    private static final String PREF = "orders_prefs";
    private static final String KEY = "orders_history";

    public static void add(Context ctx, OrderRecord record) {
        try {
            JSONArray arr = readArray(ctx);
            JSONObject obj = new JSONObject();
            obj.put("id", record.id);
            obj.put("customerName", record.customerName);
            obj.put("totalPrice", record.totalPrice);
            obj.put("itemCount", record.itemCount);
            arr.put(obj);
            writeArray(ctx, arr);
        } catch (Exception ignored) {}
    }

    public static List<OrderRecord> list(Context ctx) {
        List<OrderRecord> out = new ArrayList<>();
        try {
            JSONArray arr = readArray(ctx);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                out.add(new OrderRecord(
                        o.optLong("id"),
                        o.optString("customerName"),
                        o.optInt("totalPrice"),
                        o.optInt("itemCount")
                ));
            }
        } catch (Exception ignored) {}
        return out;
    }

    public static void clear(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().remove(KEY).apply();
    }

    private static JSONArray readArray(Context ctx) throws JSONException {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String s = sp.getString(KEY, "[]");
        return new JSONArray(s);
    }

    private static void writeArray(Context ctx, JSONArray arr) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putString(KEY, arr.toString()).apply();
    }

    public static String formatTime(long millis) {
        return new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id","ID")).format(new Date(millis));
    }

    public static String formatRupiah(int amount) {
        java.text.NumberFormat f = java.text.NumberFormat.getCurrencyInstance(new Locale("id","ID"));
        return f.format(amount).replace("Rp", "Rp ");
    }
}
