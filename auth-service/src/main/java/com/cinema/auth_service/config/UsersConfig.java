package com.cinema.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class UsersConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername("demo")
                        .password(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("demo"))
                        .roles("USER")
                        .build(),
                User.withUsername("admin")
                        .password(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("admin"))
                        .roles("USER", "ADMIN")
                        .build());
    }
}
