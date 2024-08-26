package me.mrepiko.discordbotbase.mics.requests;

import lombok.Getter;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Getter
public class HttpRequest implements AutoCloseable {

    private final String url;
    private final HashMap<String, String> headers;
    private final RequestBody body;
    private final Method type;

    private volatile Response parentResponse;
    private static final OkHttpClient client = new OkHttpClient.Builder().connectionPool(new ConnectionPool(35, 5, TimeUnit.MINUTES)).build();

    public HttpRequest(String url) {
        this(url, new HashMap<>(), Method.GET);
    }

    public HttpRequest(String url, String body, Method type) {
        this(url, new HashMap<>(), body, type);
    }

    public HttpRequest(String url, String body) {
        this(url, new HashMap<>(), body, Method.POST);
    }

    public HttpRequest(String url, HashMap<String, String> headers, Method type) {
        this(url, headers, null, type);
    }

    public HttpRequest(String url, HashMap<String, String> headers, String body) {
        this(url, headers, body, Method.POST);
    }

    public HttpRequest(String url, HashMap<String, String> headers, String body, Method type) {
        this.url = url;
        this.headers = headers;
        if (body != null) this.body = RequestBody.create(body, MediaType.parse("application/json; charset=utf-8"));
        else this.body = null;
        this.type = type;
    }

    public void sendRequest(Consumer<HttpResponse> consumer) {
        Request.Builder builder = new Request.Builder().url(url);
        for (String s: headers.keySet()) builder.addHeader(s, headers.get(s));
        switch (type) {
            case GET -> builder.get();
            case POST -> {
                if (body == null) builder.get();
                else builder.post(body);
            }
            case PUT -> {
                if (body == null) builder.get();
                else builder.put(body);
            }
            case DELETE -> {
                if (body == null) builder.delete();
                else builder.delete(body);
            }
        }
        Request request = builder.build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                consumer.accept(new HttpResponse(response, response.code(), (response.body() == null) ? null : response.body().string()));
                response.close();
            }
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                consumer.accept(new HttpResponse(null, 520, null));
            }
        });
    }

    public HttpResponse sendRequest() throws IOException {
        Request.Builder builder = new Request.Builder().url(url);
        for (String s: headers.keySet()) builder.addHeader(s, headers.get(s));
        switch (type) {
            case GET -> builder.get();
            case POST -> {
                if (body == null) builder.get();
                else builder.post(body);
            }
            case PUT -> {
                if (body == null) builder.get();
                else builder.put(body);
            }
            case DELETE -> {
                if (body == null) builder.delete();
                else builder.delete(body);
            }
        }
        Request request = builder.build();
        Call call = client.newCall(request);
        parentResponse = call.execute();
        return new HttpResponse(parentResponse, parentResponse.code(), (parentResponse.body() == null) ? null : parentResponse.body().string());
    }

    @Override
    public void close() {
        if (parentResponse != null) parentResponse.close();
    }

    public enum Method {
        GET,
        POST,
        PUT,
        DELETE
    }

}
