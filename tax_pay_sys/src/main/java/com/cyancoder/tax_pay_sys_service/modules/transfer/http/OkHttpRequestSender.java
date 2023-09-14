package com.cyancoder.tax_pay_sys_service.modules.transfer.http;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OkHttpRequestSender implements HttpRequestSender {

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static final TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }
    };

    private static final SSLContext trustAllSslContext;

    static {
        try {
            trustAllSslContext = SSLContext.getInstance("SSL");
            trustAllSslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private static final SSLSocketFactory trustAllSslSocketFactory = trustAllSslContext.getSocketFactory();

    static Logger logger = Logger.getLogger(OkHttpRequestSender.class.getName());

    public static OkHttpClient trustAllSslClient(OkHttpClient client) {
        OkHttpClient.Builder builder = client.newBuilder();
        builder.sslSocketFactory(trustAllSslSocketFactory, (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier((hostname, session) -> true);
        return builder.build();
    }

    @Override
    public HttpResponse sendPostRequest(@NotNull String url, Object requestBody, @Nullable Map<String, String> headers) {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .followRedirects(false)
                .build();

        client = trustAllSslClient(client);

        configureClient(client);

        byte[] sendData;
        if (requestBody instanceof String) {
            sendData = ((String) requestBody).getBytes();
        } else {
            try {
                sendData = mapper.writeValueAsBytes(requestBody);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("error.in.pars.object.to.json", e);
            }
        }

        RequestBody httpRequestBody = RequestBody.create(sendData, MediaType.parse("application/json; charset=utf-8"));
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(httpRequestBody);
        if(headers != null){
            requestBuilder.headers(Headers.of(headers));
        }
        Request request = requestBuilder.build();

        Call call = client.newCall(request);
        HttpResponse response = new HttpResponse();
        try {
            okhttp3.Response okResponse = call.execute();
            ResponseBody responseBody = okResponse.body();
            if(responseBody != null) {
                response.setBody(responseBody.string());
            }

            Map<String, String> responseHeaders = new HashMap<>();
            okResponse.headers().forEach(pair -> {
                responseHeaders.put(pair.component1(), pair.component2());
            });

            response.setHeaders(responseHeaders);
            response.setStatus(okResponse.code());
            return response;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "http request failed with message : {}", e.getMessage());
            if (e instanceof SSLException) {
                response.setStatus(496);
            } else {
                response.setStatus(408);
            }
            return response;
        }
    }

    protected void configureClient(OkHttpClient client) {
    }

    @Override
    public HttpResponse sendGetRequest(@NotNull String url, @Nullable Map<String, String> headers) {
        throw new RuntimeException("Not Implemented");
    }
}
