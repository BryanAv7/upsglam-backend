package com.upsglam.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class FeedService {

    @Value("${app.supabase.url}")
    private String supabaseUrl;

    @Value("${app.supabase.service-role-key}")
    private String supabaseServiceRole;

    private final WebClient client;

    public FeedService(WebClient.Builder builder) {
        this.client = builder.build();
    }

    public Mono<List<Map<String, Object>>> getPosts() {
        return client.get()
                .uri(supabaseUrl + "/rest/v1/posts?select=*")
                .header("apikey", supabaseServiceRole)
                .header("Authorization", "Bearer " + supabaseServiceRole)
                .header("Accept", "application/json")
                .header("Prefer", "return=representation")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }
}
