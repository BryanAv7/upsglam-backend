package com.upsglam.service;

import com.upsglam.config.SupabaseConfig;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PostService {

    private final WebClient supabaseWebClient;
    private final SupabaseConfig supabaseConfig; 

    public PostService(WebClient supabaseWebClient, SupabaseConfig supabaseConfig) {
        this.supabaseWebClient = supabaseWebClient;
        this.supabaseConfig = supabaseConfig;
    }

    public Mono<Map<String, Object>> createPost(String userUid, String caption, FilePart image) {
        String filename = userUid + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
        String path = "posts/" + filename; 

        return uploadToSupabaseStorage(path, image)
                .map(url -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("imageUrl", url);
                    result.put("uid", userUid); // Puente entre Firebase y Supabase
                    result.put("caption", caption);
                    result.put("filename", filename);
                    return result;
                });
    }

    private Mono<String> uploadToSupabaseStorage(String path, FilePart filePart) {
        
        String objectUrl = "/storage/v1/object/" + supabaseConfig.getBucketName() + "/" + path;

        return supabaseWebClient.put()
                .uri(objectUrl)
                .contentType(filePart.headers().getContentType() != null 
                        ? filePart.headers().getContentType() 
                        : MediaType.IMAGE_JPEG)
                .body(filePart.content(), DataBuffer.class) 
                .retrieve()
                .toBodilessEntity()
                .thenReturn(getPublicUrl(path));
    }

    private String getPublicUrl(String path) {
        return supabaseConfig.getSupabaseUrl() 
               + "/storage/v1/object/public/" 
               + supabaseConfig.getBucketName() + "/" + path;
    }
}