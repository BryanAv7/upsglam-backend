package com.upsglam.service;

import com.upsglam.config.SupabaseConfig;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserProfileService {

    private final WebClient webClient;            
    private final SupabaseConfig supabaseConfig;

    public UserProfileService(WebClient webClient, SupabaseConfig supabaseConfig) {
        this.webClient = webClient;
        this.supabaseConfig = supabaseConfig;
    }

    public Mono<String> uploadProfileImage(String userUid, FilePart file) {
        String storagePath = supabaseConfig.getProfileBucket() + "/" + userUid + "/profile.jpg";

        System.out.println("[UserProfileService] Subiendo archivo para UID=" + userUid);
        System.out.println("   Nombre original: " + file.filename());
        System.out.println("   ContentType: " + file.headers().getContentType());

        return webClient.put()
                .uri("/storage/v1/object/{bucket}/{path}", supabaseConfig.getProfileBucket(), userUid + "/profile.jpg")
                .contentType(file.headers().getContentType() != null ? file.headers().getContentType() : MediaType.IMAGE_JPEG)
                .body(file.content(), org.springframework.core.io.buffer.DataBuffer.class)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(r -> System.out.println("[UserProfileService] Subida completada, status: " + r.getStatusCode()))
                .thenReturn(getPublicUrl(storagePath));
    }

    public Mono<String> uploadProfileImageFromUrl(String userUid, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return Mono.just("");
        }

        String storagePath = supabaseConfig.getProfileBucket() + "/" + userUid + "/profile.jpg";
        WebClient plain = WebClient.create();

        System.out.println("[UserProfileService] Descargando imagen externa: " + imageUrl);

        return plain.get()
                .uri(imageUrl)
                .retrieve()
                .bodyToMono(byte[].class)
                .flatMap(bytes -> {
                    System.out.println("[UserProfileService] Imagen descargada, bytes: " + bytes.length);
                    return webClient.put()
                            .uri("/storage/v1/object/{bucket}/{path}", supabaseConfig.getProfileBucket(), userUid + "/profile.jpg")
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .bodyValue(bytes)
                            .retrieve()
                            .toBodilessEntity()
                            .doOnSuccess(r -> System.out.println("[UserProfileService] Subida completada, status: " + r.getStatusCode()))
                            .thenReturn(getPublicUrl(storagePath));
                })
                .onErrorResume(e -> {
                    System.err.println("[UserProfileService] Error al subir imagen desde URL: " + e.getMessage());
                    return Mono.just("");
                });
    }

    private String getPublicUrl(String path) {
        return supabaseConfig.getSupabaseUrl() + "/storage/v1/object/public/" + path;
    }

    public Mono<Void> saveProfileUrlToDb(String userUid, String profileUrl) {
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("user_uid", userUid);
        payload.put("profile_url", profileUrl);

        System.out.println("[UserProfileService] Guardando URL en DB: " + profileUrl);

        return webClient.post()
                .uri("/rest/v1/user_profile_images")
                .header("apikey", supabaseConfig.getAnonKey())
                .header("Authorization", "Bearer " + supabaseConfig.getServiceRoleKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(org.springframework.web.reactive.function.client.WebClientResponseException.class, e -> {
                    System.out.println("[UserProfileService] Error insert DB, intentando patch: " + e.getMessage());
                    return webClient.patch()
                            .uri("/rest/v1/user_profile_images?user_uid=eq." + userUid)
                            .header("apikey", supabaseConfig.getAnonKey())
                            .header("Authorization", "Bearer " + supabaseConfig.getServiceRoleKey())
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(payload)
                            .retrieve()
                            .bodyToMono(Void.class);
                });
    }


    // Obtener URL de foto de perfil desde Supabase
    public Mono<String> getProfileUrl(String userUid) {
        return webClient.get()
                .uri("/rest/v1/user_profile_images?select=profile_url&user_uid=eq." + userUid)
                .header("apikey", supabaseConfig.getAnonKey())
                .header("Authorization", "Bearer " + supabaseConfig.getServiceRoleKey())
                .retrieve()
                .bodyToMono(String.class)
                .map(json -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode node = mapper.readTree(json);
                        if (node.isArray() && node.size() > 0) {
                            return node.get(0).get("profile_url").asText();
                        } else {
                            return "";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "";
                    }
                })
                .onErrorReturn("");
    }
}
