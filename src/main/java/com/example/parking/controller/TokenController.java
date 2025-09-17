package com.example.parking.controller;


import com.example.parking.utils.JwtUtil;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TokenController {

    private final JwtDecoder jwtDecoder;

    public TokenController(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    // Option A: Accept Google ID token from client
    @PostMapping("/token")
    public Map<String, String> getServerJwt(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");
        if (idToken == null) throw new RuntimeException("Missing idToken");

        // Decode/verify Google ID token
        Jwt jwt = jwtDecoder.decode(idToken); // optional: use Google's JWKS for verification
        String email = jwt.getClaimAsString("email");
        List<String> roles = List.of("ROLE_USER"); // optionally map from email

        String serverJwt = JwtUtil.generateToken(email, roles);
        return Map.of("token", serverJwt);
    }
}
