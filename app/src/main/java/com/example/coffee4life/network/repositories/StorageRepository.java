package com.example.coffee4life.network.repositories;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import com.example.coffee4life.Constants;
import com.example.coffee4life.auth.SessionManager;
import com.example.coffee4life.network.RetrofitClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StorageRepository {

    public interface CallbackString {
        void onSuccess(String url);
        void onError(String message);
    }

    private final SessionManager sessionManager;
    private final OkHttpClient httpClient;

    public StorageRepository(Context ctx) {
        this.sessionManager = new SessionManager(ctx.getApplicationContext());
        // Reuse Retrofit's OkHttpClient (has interceptors/apikey)
        this.httpClient = RetrofitClient.getInstance().callFactory() instanceof OkHttpClient
                ? (OkHttpClient) RetrofitClient.getInstance().callFactory()
                : new OkHttpClient();
    }

    public void uploadProductImage(Context ctx, Uri uri, CallbackString cb) {
        new Thread(() -> {
            try {
                String token = sessionManager.getAccessToken();
                if (token == null) {
                    cb.onError("Missing access token. Please login.");
                    return;
                }
                ContentResolver cr = ctx.getContentResolver();
                String ext = getExtension(cr, uri);
                String contentType = getMimeType(cr, uri);
                if (contentType == null) contentType = "application/octet-stream";
                if (ext == null) ext = "bin";

                byte[] bytes = readAllBytes(cr, uri);
                String objectPath = "products/" + UUID.randomUUID() + "." + ext;

                String url = Constants.SUPABASE_URL + "/storage/v1/object/product-images/" + objectPath;

                RequestBody body = RequestBody.create(bytes, MediaType.parse(contentType));
                Request req = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + token)
                        .addHeader("apikey", Constants.SUPABASE_ANON_KEY)
                        .addHeader("Content-Type", contentType)
                        .post(body)
                        .build();

                try (Response resp = httpClient.newCall(req).execute()) {
                    if (resp.isSuccessful()) {
                        // Public URL (assumes bucket 'product-images' is public)
                        String publicUrl = Constants.SUPABASE_URL + "/storage/v1/object/public/product-images/" + objectPath;
                        cb.onSuccess(publicUrl);
                    } else {
                        String err = resp.body() != null ? resp.body().string() : ("HTTP " + resp.code());
                        cb.onError(err);
                    }
                }
            } catch (Exception e) {
                cb.onError(e.getMessage());
            }
        }).start();
    }

    private static String getExtension(ContentResolver cr, Uri uri) {
        String type = cr.getType(uri);
        if (type != null) {
            return MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
        }
        String path = uri.getPath();
        if (path != null) {
            int dot = path.lastIndexOf('.');
            if (dot >= 0 && dot < path.length() - 1) return path.substring(dot + 1);
        }
        return null;
    }

    private static String getMimeType(ContentResolver cr, Uri uri) {
        String type = cr.getType(uri);
        if (type != null) return type;
        String ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        if (ext != null) return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        return null;
    }

    private static byte[] readAllBytes(ContentResolver cr, Uri uri) throws IOException {
        try (InputStream in = cr.openInputStream(uri); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (in == null) return new byte[0];
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            return out.toByteArray();
        }
    }
}
