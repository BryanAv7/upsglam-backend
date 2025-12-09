package com.upsglam.controller;

import com.upsglam.dto.*;
import com.upsglam.service.FirebaseAuthService;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final FirebaseAuthService authService;

    public AuthController(FirebaseAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<?>> register(@RequestBody RegisterRequest req) {
        return authService.registerUser(
                req.email,
                req.password,
                req.displayName
        ).map(userRecord -> ResponseEntity.ok(userRecord));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<?>> login(@RequestBody LoginRequest req) {
        return authService.loginWithEmail(
                req.email,
                req.password
        ).map(resp -> ResponseEntity.ok(resp));
    }

    @PostMapping("/google")
    public Mono<ResponseEntity<?>> googleSignIn(@RequestBody GoogleSignInRequest req) {
        return authService.signInWithGoogleIdToken(
                req.idToken
        ).map(resp -> ResponseEntity.ok(resp));
    }

    @GetMapping("/verify")
    public Mono<ResponseEntity<?>> verifyToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.badRequest().body("Missing or invalid Authorization header"));
        }

        String idToken = authHeader.substring(7);

        return authService.verifyIdToken(idToken)
                .map((FirebaseToken token) -> ResponseEntity.ok(token));
    }
}