package com.vibeport.auth.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private String secret;

    private long accessExp;

    private long refreshExp;

    private SecretKey key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(String subject, String role) {
        Date now = new Date();
        Date exp = new Date();

        if(subject.equals("access")) {
            exp = new Date(now.getTime() + accessExp);
        } else if(subject.equals("refresh")) {
            exp = new Date(now.getTime() + refreshExp);
        } else {
            throw new RuntimeException("토큰 발급 에러");
        }

        return Jwts.builder()
                .claim("category", subject)
                .claim("role", role)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.ES256)
                .compact();
    }
    
    public Claims parseClaims(String token) {
        // TODO - deprecated 확인 필요
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
