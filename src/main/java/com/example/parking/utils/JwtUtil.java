package com.example.parking.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.List;

public class JwtUtil {
    private static final String SECRET = "mY7r9L2vN8pQ3sT5uV1xC0bZ6hK4wE8y"; // Use secure random key
    private static final long EXPIRATION_MS = 3600_000; // 1 hour

    public static String generateToken(String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(SignatureAlgorithm.HS256, SECRET.getBytes())
                .compact();
    }

    public static String getSecret() {
        return SECRET;
    }
}
