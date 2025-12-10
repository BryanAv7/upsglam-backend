package com.upsglam.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    private final WebClient supabaseWebClient;
    private final String bucketName;
    private final String supabaseUrl; 

    public SupabaseStorageService(WebClient supabaseWebClient,
                                  @Value("${app.supabase.storage-bucket}") String bucketName,
                                  @Value("${app.supabase.url}") String supabaseUrl) {
        this.supabaseWebClient = supabaseWebClient;
        this.bucketName = bucketName;
        this.supabaseUrl = supabaseUrl;
    }

    /**
     * Se sube la imagen y devuelve la URL pública
     */
    public Mono<String> uploadImage(String userUid, MultipartFile file) {
        String extension = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            extension = original.substring(original.lastIndexOf('.'));
        }

        String randomName = UUID.randomUUID().toString() + extension;
        String storagePath = "users/" + userUid + "/" + randomName;

        
        return Mono.fromCallable(() -> file.getBytes())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(bytes ->
                        supabaseWebClient.post()
                                // Supabase Storage upload endpoint: /storage/v1/object/{bucket}/{path}
                                .uri(uriBuilder -> uriBuilder.path("/storage/v1/object/{bucket}/{path}")
                                        .build(bucketName, storagePath))
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .bodyValue(bytes)
                                .retrieve()
                                .bodyToMono(Map.class) // Supabase devuelve info del objeto
                                .map(resp -> {
                                    // Se Construye URL pública
                                 
                                    return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + storagePath;
                                })
                );
    }
}
