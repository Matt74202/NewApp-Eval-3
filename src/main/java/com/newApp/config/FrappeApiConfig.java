package com.newApp.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FrappeApiConfig {

    @Bean
    public RestTemplate restTemplate() {
        BasicCookieStore cookieStore = new BasicCookieStore();
        HttpClient httpClient = HttpClients.custom()
                .setConnectionManager(new BasicHttpClientConnectionManager())
                .setDefaultCookieStore(cookieStore)
                .build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }

    @Bean
    public CookieStore cookieStore() {
        return new BasicCookieStore();
    }
}