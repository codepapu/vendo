package com.vendo.tax.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Tax Service Configuration
 */
@Configuration
public class TaxServiceConfig {
    
    @Bean(name = "taxRestTemplate")
    public RestTemplate taxRestTemplate(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                super.prepareConnection(connection, httpMethod);
                connection.setConnectTimeout(5000); // 5 seconds
                connection.setReadTimeout(10000); // 10 seconds
            }
        };
        
        return builder
                .requestFactory(() -> factory)
                .build();
    }
}

