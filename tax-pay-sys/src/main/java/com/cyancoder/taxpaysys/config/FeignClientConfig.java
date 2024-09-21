package com.cyancoder.taxpaysys.config;

import okhttp3.OkHttpClient;
import java.net.InetSocketAddress;
import java.net.Proxy;
import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;




@Configuration
public class FeignClientConfig {

    @Bean
    public Client feignClient() {
        // Create a proxy instance
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("s2.exirmatab.com", 6774));

        // Build OkHttpClient with proxy settings
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .proxy(proxy)
                .proxyAuthenticator((route, response) -> {
                    // Add proxy authentication
                    String credential = okhttp3.Credentials.basic("farid", "farid");
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                });

        return (Client) new OkHttpClient(okHttpClientBuilder);

    }
}

