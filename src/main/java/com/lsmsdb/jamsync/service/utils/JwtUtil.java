package com.lsmsdb.jamsync.service.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    private static final String SECRET_KEY = "e1c861e631d6b98d21f10e258f56b9db7910a7a5f7c26d0b3843081b8570e49c";
    private static final long EXPIRATION_TIME_MILLISECONDS = 3600000; // 1 hour

    // Create a JWT
    public static String generateToken(String subject) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME_MILLISECONDS);

        Map<String, Object> claims = new HashMap<>();
        // You can add custom claims here

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // Validate a JWT
    public static boolean validateToken(String token, String subject) {
        try {
            Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
            return claims.getSubject().equals(subject) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isTokenExpired(String token) {
        Date expirationDate = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getExpiration();
        return expirationDate.before(new Date());
    }

    // Extract information from a JWT
    public static String extractSubject(String token) {
        try {
            return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}
