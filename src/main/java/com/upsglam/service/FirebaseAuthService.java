package com.upsglam.service;

import com.upsglam.dto.*;
import com.google.firebase.auth.*;
import com.google.firebase.auth.UserRecord.CreateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class FirebaseAuthService {

    private final WebClient webClient;
    private final String apiKey;
    private final String identityToolkitBase;

    public FirebaseAuthService(@Value("${app.firebase.api-key}") String apiKey,
                               @Value("${app.firebase.identitytoolkit-url}") String identityToolkitBase) {
        this.apiKey = apiKey;
        this.identityToolkitBase = identityToolkitBase;
        this.webClient = WebClient.create(identityToolkitBase);
    }

    // 1) Registro (crea usuario con Admin SDK)
    public Mono<UserRecord> registerUser(String email, String password, String displayName) {
        return Mono.fromCallable(() -> {
            CreateRequest req = new CreateRequest()
                    .setEmail(email)
                    .setPassword(password)
                    .setDisplayName(displayName);
            return FirebaseAuth.getInstance().createUser(req);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // 2) Login email/password -> llama a REST signInWithPassword
    public Mono<LoginResponse> loginWithEmail(String email, String password) {
        String url = "/accounts:signInWithPassword?key=" + apiKey;
        LoginRequestBody body = new LoginRequestBody(email, password, true);

        return webClient.post()
                .uri(url)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(LoginResponse.class);
    }

    // 3) Google Sign-in: recibe Google id_token (del cliente) y llama signInWithIdp
    // Actualizado para incluir UID
    public Mono<LoginResponse> signInWithGoogleIdToken(String googleIdToken) {
        String url = "/accounts:signInWithIdp?key=" + apiKey;

        var payload = new java.util.HashMap<String, Object>();
        payload.put("postBody", "id_token=" + googleIdToken + "&providerId=google.com");
        payload.put("requestUri", "http://localhost"); // required but can be any valid URL
        payload.put("returnSecureToken", true);
        payload.put("returnIdpCredential", true);

        // 1. Llamada REST para iniciar sesión con Google
        return webClient.post()
                .uri(url)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .flatMap(resp -> {
                    // 2. Verificar idToken con Admin SDK para obtener UID
                    return verifyIdToken(resp.getIdToken())
                            .map(decodedToken -> {
                                resp.setLocalId(decodedToken.getUid()); // <-- UID de Firebase
                                return resp;
                            });
                });
    }

    // 4) Verify idToken with Admin SDK
    public Mono<FirebaseToken> verifyIdToken(String idToken) {
        return Mono.fromCallable(() -> FirebaseAuth.getInstance().verifyIdToken(idToken))
                .subscribeOn(Schedulers.boundedElastic());
    }

    // DTO internal for signInWithPassword request format
    static class LoginRequestBody {
        public String email;
        public String password;
        public boolean returnSecureToken;

        public LoginRequestBody(String email, String password, boolean returnSecureToken) {
            this.email = email;
            this.password = password;
            this.returnSecureToken = returnSecureToken;
        }
    }
}
