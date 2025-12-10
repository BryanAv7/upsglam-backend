package com.upsglam.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SupabaseConfig {

    private final String supabaseUrl;
    private final String serviceRoleKey;
    private final String anonKey;
    private final String bucketName;

    // Constructor 
    public SupabaseConfig(
            @Value("${app.supabase.url}") String supabaseUrl,
            @Value("${app.supabase.service-role-key}") String serviceRoleKey,
            @Value("${app.supabase.anon-key}") String anonKey,
            @Value("${app.supabase.storage-bucket:posts}") String bucketName) {
        this.supabaseUrl = supabaseUrl;
        this.serviceRoleKey = serviceRoleKey;
        this.anonKey = anonKey;
        this.bucketName = bucketName;
    }

    // ✅ Getters (ahora sí existen)
    public String getSupabaseUrl() {
        return supabaseUrl;
    }

    public String getServiceRoleKey() {
        return serviceRoleKey;
    }

    public String getAnonKey() {
        return anonKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    @Bean
    public WebClient supabaseWebClient() {
        return WebClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader("apikey", anonKey)                         // anonKey
                .defaultHeader("Authorization", "Bearer " + serviceRoleKey) // serviceRoleKey
                .build();
    }
}