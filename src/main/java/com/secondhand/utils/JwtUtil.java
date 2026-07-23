package com.secondhand.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET = "your-secret-key-secondhand-2024";
    private static final long EXPIRATION = 30 * 60 * 1000;

    public static String generateToken(Long userId, String username, Integer role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }

    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.getSubject());
    }
}