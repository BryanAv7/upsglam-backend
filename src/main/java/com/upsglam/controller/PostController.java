package com.upsglam.controller;

import com.upsglam.service.PostService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.codec.multipart.FilePart; // ← Importa esto
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Map<String, Object>> uploadPost(
            @RequestPart("uid") String userUid,
            @RequestPart(value = "caption", required = false) String caption,
            @RequestPart("image") FilePart image
    ) {
        return postService.createPost(userUid, caption, image); 
    }
}




