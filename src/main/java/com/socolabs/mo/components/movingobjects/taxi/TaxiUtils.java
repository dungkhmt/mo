package com.socolabs.mo.components.movingobjects.taxi;

import okhttp3.*;

import java.io.IOException;

public class TaxiUtils {
    public static OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType
            .get("application/json; charset=utf-8");

    public static String execPost(String url, String json) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder().url(url).post(body).build();
        try {
            try (Response response = client.newCall(request).execute()) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
