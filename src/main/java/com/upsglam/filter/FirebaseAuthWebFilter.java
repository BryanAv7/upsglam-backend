package com.upsglam.filter;

import com.upsglam.service.FirebaseAuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.*;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class FirebaseAuthWebFilter implements WebFilter {

    private final FirebaseAuthService authService;

    public FirebaseAuthWebFilter(FirebaseAuthService authService) {
        this.authService = authService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // proteger rutas /api/** (ejemplo)
        if (!path.startsWith("/api/")) {
            return chain.filter(exchange);
        }

        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        String token = auth.substring(7);

        return authService.verifyIdToken(token)
                .flatMap(firebaseToken -> {
                    // Aquí podrías añadir datos al exchange.attributes para downstream handlers
                    exchange.getAttributes().put("firebaseUser", firebaseToken);
                    return chain.filter(exchange);
                })
                .onErrorResume(e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }
}
