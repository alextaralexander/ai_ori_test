package com.bestorigin.monolith.publiccontent.impl.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class TestAuthController {

    @PostMapping("/test-login")
    public Map<String, String> testLogin(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "guest");
        return Map.of("token", "test-token-" + username);
    }
}
