package com.upsglam.controller;

import com.upsglam.service.FeedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    /**
     * Obtiene todos los posts del feed
     */
    @GetMapping
    public Mono<ResponseEntity<List<Map<String, Object>>>> getPosts() {
        return feedService.getPosts()
                .map(ResponseEntity::ok)
                .onErrorResume(e ->
                        Mono.just(
                                ResponseEntity.badRequest().body(
                                        List.of(Map.of("error", e.getMessage()))
                                )
                        )
                );
    }
}
