package com.upsglam.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

@Service
public class ImageProcessingClient {

    private final WebClient webClient;

    public ImageProcessingClient(@Qualifier("flaskWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<ResponseEntity<byte[]>> enviarImagenAFlask(
            FilePart imagen,
            String filtro,
            Double factor,
            Double offset,
            Double sigma,
            Double sharpFactor,
            Double highlightBoost,
            Double vignetteStrength,
            Double blueBoost,
            Double contrast
    ) {

        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("imagen", imagen).filename(imagen.filename());

        body.part("filtro", filtro);
        body.part("factor", factor.toString());
        body.part("offset", offset.toString());
        body.part("sigma", sigma.toString());
        body.part("sharp_factor", sharpFactor.toString());
        body.part("highlight_boost", highlightBoost.toString());
        body.part("vignette_strength", vignetteStrength.toString());
        body.part("blue_boost", blueBoost.toString());
        body.part("contrast", contrast.toString());

        return webClient.post()
                .uri("/procesar")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body.build()))
                .exchangeToMono(response -> {

                    MediaType contentType =
                            response.headers().contentType().orElse(MediaType.APPLICATION_OCTET_STREAM);

                    HttpHeaders headers = response.headers().asHttpHeaders();
                    String disposition = headers.getFirst(HttpHeaders.CONTENT_DISPOSITION);

                    return response.bodyToMono(byte[].class)
                            .map(bytes ->
                                    ResponseEntity.ok()
                                            .contentType(contentType)
                                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                                    disposition != null
                                                            ? disposition
                                                            : "inline; filename=procesada.png")
                                            .body(bytes)
                            );
                });
    }
}
