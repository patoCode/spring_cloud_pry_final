package com.cinema.booking_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestClient;

@Configuration
@EnableRetry
public class RestClientConfig {

    @Value("${movie-service.url}")
    private String movieServiceUrl;

    @Bean
    public RestClient movieServiceClient() {
        return RestClient.builder()
                .baseUrl(movieServiceUrl)
                .build();
    }
}
