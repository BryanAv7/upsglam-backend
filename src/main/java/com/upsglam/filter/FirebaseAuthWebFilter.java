package com.upsglam.filter;

import com.upsglam.service.FirebaseAuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.*;
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
		// Permitir procesar imágenes sin token
		if (path.startsWith("/api/imagen/procesar")) {
			return chain.filter(exchange);
		}

		String uri = exchange.getRequest().getURI().toString();

		// ================================================
		// 1. EXCLUIR LLAMADAS INTERNAS DEL WEBCLIENT A FLASK
		// ================================================
		if (uri.contains("127.0.0.1:5000") || uri.contains("localhost:5000")) {
			return chain.filter(exchange);
		}

		// ================================================
		// 2. SOLO PROTEGER RUTAS /api/**
		// ================================================
		if (!path.startsWith("/api/")) {
			return chain.filter(exchange);
		}

		// ================================================
		// 3. VALIDAR AUTHORIZATION
		// ================================================
		String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (auth == null || !auth.startsWith("Bearer ")) {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}

		String token = auth.substring(7);

		// ================================================
		// 4. VERIFICAR TOKEN CON FIREBASE
		// ================================================
		return authService.verifyIdToken(token).flatMap(firebaseToken -> {
			exchange.getAttributes().put("firebaseUser", firebaseToken);
			return chain.filter(exchange);
		}).onErrorResume(e -> {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		});
	}
}
