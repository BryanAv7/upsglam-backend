package com.upsglam.controller;

import com.upsglam.service.ImageProcessingClient;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api/imagen")
public class ImageController {

    private final ImageProcessingClient imageClient;

    public ImageController(ImageProcessingClient imageClient) {
        this.imageClient = imageClient;
    }

    @PostMapping(
            value = "/procesar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Mono<ResponseEntity<byte[]>> procesarImagen(
            @RequestPart("imagen") Mono<FilePart> imagen,
            @RequestPart(value = "filtro", required = false) Mono<String> filtro,
            @RequestPart(value = "factor", required = false) Mono<String> factor,
            @RequestPart(value = "offset", required = false) Mono<String> offset,
            @RequestPart(value = "sigma", required = false) Mono<String> sigma,
            @RequestPart(value = "sharp_factor", required = false) Mono<String> sharpFactor,
            @RequestPart(value = "highlight_boost", required = false) Mono<String> highlight,
            @RequestPart(value = "vignette_strength", required = false) Mono<String> vignette,
            @RequestPart(value = "blue_boost", required = false) Mono<String> blueBoost,
            @RequestPart(value = "contrast", required = false) Mono<String> contrast
    ) {

        return Mono.zip(
                List.of(
                        imagen,
                        filtro.defaultIfEmpty("sobel"),
                        factor.defaultIfEmpty("2.0"),
                        offset.defaultIfEmpty("128.0"),
                        sigma.defaultIfEmpty("90.0"),
                        sharpFactor.defaultIfEmpty("20.0"),
                        highlight.defaultIfEmpty("1.0"),
                        vignette.defaultIfEmpty("0.5"),
                        blueBoost.defaultIfEmpty("1.0"),
                        contrast.defaultIfEmpty("1.0")
                ),
                arr -> arr
        ).flatMap(arr -> {
            FilePart img = (FilePart) arr[0];
            String filtroV = (String) arr[1];
            Double factorV = Double.valueOf((String) arr[2]);
            Double offsetV = Double.valueOf((String) arr[3]);
            Double sigmaV = Double.valueOf((String) arr[4]);
            Double sharpV = Double.valueOf((String) arr[5]);
            Double highlightV = Double.valueOf((String) arr[6]);
            Double vignetteV = Double.valueOf((String) arr[7]);
            Double blueV = Double.valueOf((String) arr[8]);
            Double contrastV = Double.valueOf((String) arr[9]);

            return imageClient.enviarImagenAFlask(
                    img,
                    filtroV,
                    factorV,
                    offsetV,
                    sigmaV,
                    sharpV,
                    highlightV,
                    vignetteV,
                    blueV,
                    contrastV
            );
        });
    }
}
