package com.example.whatisthat;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Translator {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();
    String URL = "https://cs32ukd9fi.execute-api.us-east-2.amazonaws.com/desarrollo/what-is-that-translator";

    public String translate(String word, String lang) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("word", word.toLowerCase());
            jsonObject.put("lang", lang);
        } catch (JSONException jsonError) {
            jsonError.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);

        Request request = new Request.Builder()
            .url(URL)
            .post(body)
            .build();

        try {
            Response response = client.newCall(request).execute();
            String translated = Objects.requireNonNull(response.body()).string();

            Log.i("TRANSLATED", translated);
            return response.code() == 200 ? translated : word;
        } catch (IOException ioError) {
            ioError.printStackTrace();
        }

        return word;
    }

}
