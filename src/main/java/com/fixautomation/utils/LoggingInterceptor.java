package com.fixautomation.utils;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Centralized OkHttp logging interceptor for HTTP requests and responses.
 */
@Component
public class LoggingInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long startTime = System.currentTimeMillis();

        log.info("--> {} {}", request.method(), request.url());
        if (request.body() != null) {
            Buffer buffer = new Buffer();
            request.body().writeTo(buffer);
            log.info("--> Body: {}", buffer.readUtf8());
        }

        Response response;
        try {
            response = chain.proceed(request);
        } catch (IOException e) {
            log.error("--> HTTP call failed: {}", e.getMessage());
            throw e;
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("<-- {} {} ({}ms)", response.code(), request.url(), duration);

        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            String bodyString = responseBody.string();
            log.debug("<-- Body: {}", bodyString);
            // Rebuild response so callers can read the body too
            return response.newBuilder()
                    .body(ResponseBody.create(bodyString, responseBody.contentType()))
                    .build();
        }

        return response;
    }
}
