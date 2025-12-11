package com.upsglam.controller;

import com.upsglam.dto.GoogleSignInRequest;
import com.upsglam.service.FirebaseAuthService;
import com.upsglam.service.UserProfileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final FirebaseAuthService authService;
    private final UserProfileService userProfileService;

    public AuthController(FirebaseAuthService authService,
                          UserProfileService userProfileService) {
        this.authService = authService;
        this.userProfileService = userProfileService;
    }

    // -- Registro --
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> register(
            @RequestPart("displayName") String displayName,
            @RequestPart("email") String email,
            @RequestPart("password") String password,
            @RequestPart(value = "photo", required = false) FilePart photo
    ) {
        System.out.println("[AuthController] Registro recibido: email=" + email + ", displayName=" + displayName);
        System.out.println("[AuthController] Foto recibida: " + (photo != null ? photo.filename() : "null"));

        return authService.registerUser(email, password, displayName)
                .flatMap(userRecord -> {
                    String uid = userRecord.getUid();
                    System.out.println("[AuthController] UID generado: " + uid);

                    if (photo == null) {
                        Map<String, Object> res = new HashMap<>();
                        res.put("uid", uid);
                        res.put("displayName", displayName);
                        res.put("photoUrl", "");
                        return Mono.just(ResponseEntity.ok(res));
                    }

                    System.out.println("[AuthController] Subiendo foto al bucket de perfiles...");
                    return userProfileService.uploadProfileImage(uid, photo)
                            .doOnNext(url -> System.out.println("[AuthController] URL subida: " + url))
                            .flatMap(url ->
                                    userProfileService.saveProfileUrlToDb(uid, url)
                                            .doOnSuccess(v -> System.out.println("[AuthController] URL guardada en DB"))
                                            .thenReturn(url)
                            )
                            .map(url -> {
                                Map<String, Object> res = new HashMap<>();
                                res.put("uid", uid);
                                res.put("displayName", displayName);
                                res.put("photoUrl", url);
                                return ResponseEntity.ok(res);
                            });
                });
    }

    // -- Login --
    @PostMapping("/login")
    public Mono<ResponseEntity<?>> login(@RequestBody com.upsglam.dto.LoginRequest req) {
        System.out.println("[AuthController] Login solicitado: " + req.email);
        return authService.loginWithEmail(req.email, req.password)
                .flatMap(resp -> 
                    userProfileService.getProfileUrl(resp.getLocalId())
                            .map(photoUrl -> {
                                Map<String, Object> result = new HashMap<>();
                                result.put("uid", resp.getLocalId());
                                result.put("displayName", resp.getDisplayName());
                                result.put("photoUrl", photoUrl);
                                System.out.println("[AuthController] Login OK, photoUrl: " + photoUrl);
                                return ResponseEntity.ok(result);
                            })
                );
    }

    // -- Google SignIn --
    @PostMapping("/google")
    public Mono<ResponseEntity<Map<String, Object>>> googleSignIn(@RequestBody GoogleSignInRequest req) {
        System.out.println("[AuthController] Google SignIn solicitado");
        return authService.signInWithGoogleIdToken(req.idToken)
                .flatMap(resp -> {
                    String uid = resp.getLocalId();
                    String googlePhotoUrl = resp.getPhotoUrl();
                    System.out.println("[AuthController] Google UID: " + uid + ", PhotoURL: " + googlePhotoUrl);

                    return userProfileService.getProfileUrl(uid)
                            .flatMap(photoUrlFromDb -> {
                                String finalPhotoUrl = !photoUrlFromDb.isBlank() ? photoUrlFromDb : googlePhotoUrl;
                                if (!googlePhotoUrl.isBlank() && photoUrlFromDb.isBlank()) {
                                    System.out.println("[AuthController] Subiendo foto de Google a Supabase...");
                                    return userProfileService.uploadProfileImageFromUrl(uid, googlePhotoUrl)
                                            .flatMap(url -> userProfileService.saveProfileUrlToDb(uid, url)
                                                    .thenReturn(url)
                                            )
                                            .map(url -> finalPhotoUrl);
                                }
                                return Mono.just(finalPhotoUrl);
                            })
                            .map(finalPhotoUrl -> {
                                Map<String, Object> out = new HashMap<>();
                                out.put("uid", uid);
                                out.put("displayName", resp.getDisplayName());
                                out.put("photoUrl", finalPhotoUrl);
                                return ResponseEntity.ok(out);
                            });
                });
    }

    // -- Verificar Token --
    @GetMapping("/verify")
    public Mono<ResponseEntity<?>> verifyToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[AuthController] Authorization header inválido");
            return Mono.just(ResponseEntity.badRequest().body("Missing or invalid Authorization header"));
        }
        String idToken = authHeader.substring(7);
        System.out.println("[AuthController] Verificando token: " + idToken);
        return authService.verifyIdToken(idToken)
                .doOnNext(resp -> System.out.println("[AuthController] Token verificado OK"))
                .map(ResponseEntity::ok);
    }
}
