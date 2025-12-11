package com.upsglam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // 🔥 1. Crear el bean WebClient.Builder
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    // 🔥 2. Crear WebClient usando el builder
    //@Bean
    @Bean(name = "flaskWebClient")
    public WebClient webClient(WebClient.Builder builder) {
        return builder
            .baseUrl("http://localhost:5001")
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(c -> c.defaultCodecs().maxInMemorySize(50 * 1024 * 1024))
                    .build())
            .build();
    }

}
