package me.mrepiko.discordbotbase.mics.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@Getter
public class HttpResponse {

    private final Response response;
    private final int responseCode;
    @Nullable private final String content;

}
