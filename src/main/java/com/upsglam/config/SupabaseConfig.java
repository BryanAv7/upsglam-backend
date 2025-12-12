package com.upsglam.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SupabaseConfig {

    @Value("${app.supabase.url}")
    private String supabaseUrl;

    @Value("${app.supabase.service-role-key}")
    private String serviceRoleKey;

    @Value("${app.supabase.anon-key}")
    private String anonKey;

    @Value("${app.supabase.storage-bucket:posts}")
    private String bucketName; // nombre esperado por tu PostService

    @Value("${app.supabase.profile-bucket:profiles}")
    private String profileBucket; // nombre esperado por tu UserProfileService


    public String getSupabaseUrl() {
        return supabaseUrl;
    }

    public String getServiceRoleKey() {
        return serviceRoleKey;
    }

    public String getAnonKey() {
        return anonKey;
    }

    // IMPORTANTE: estos métodos deben mantenerse con este nombre
    public String getBucketName() {
        return bucketName;
    }

    public String getProfileBucket() {
        return profileBucket;
    }

    @Bean(name = "supabaseWebClient")
    public WebClient supabaseWebClient() {
        return WebClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader("apikey", serviceRoleKey) // backend
                .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
                .build();
    }
}
