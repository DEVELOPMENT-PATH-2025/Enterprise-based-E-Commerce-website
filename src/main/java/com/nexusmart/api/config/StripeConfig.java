package com.nexusmart.api.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


import jakarta.annotation.PostConstruct;

@Configuration
public class StripeConfig {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
    }
}
