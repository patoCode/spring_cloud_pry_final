package com.cinema.booking_service.config;

import com.cinema.booking_service.infrastructure.client.AuthTokenRelayInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestClient;

@Configuration
@EnableRetry
public class RestClientConfig {

    /**
     * Nombre de servicio registrado en SimpleDiscoveryClient
     * (spring.cloud.discovery.client.simple.instances.movie-service[0].uri).
     */
    @Value("${movie-service.url}")
    private String movieServiceUrl;

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder()
                .requestInterceptor(new AuthTokenRelayInterceptor());
    }

    @Bean
    public RestClient movieServiceClient(@LoadBalanced RestClient.Builder builder) {
        return builder.baseUrl(movieServiceUrl).build();
    }
}
