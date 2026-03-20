package com.ocms.backend.security;

import com.ocms.backend.common.BizException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expire-hours}")
    private Integer expireHours;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(LoginUser loginUser) {
        LocalDateTime expireAt = LocalDateTime.now().plusHours(expireHours);
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", loginUser.getUserId());
        claims.put("username", loginUser.getUsername());
        claims.put("role", loginUser.getRole());
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(Date.from(expireAt.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(key())
                .compact();
    }

    public LoginUser parse(String token) {
        try {
            Claims payload = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
            return new LoginUser(
                    ((Number) payload.get("uid")).longValue(),
                    (String) payload.get("username"),
                    (String) payload.get("role")
            );
        } catch (Exception ex) {
            throw new BizException(401, "登录状态无效或已过期");
        }
    }
}
