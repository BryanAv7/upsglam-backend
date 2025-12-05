package com.upsglam.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class HelloController {

    @GetMapping("/")
    public Mono<String> hola() {
        return Mono.just("Hola Mundo desde UPSGlam 2.0!");
    }
}
