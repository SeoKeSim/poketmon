package sks.poketmon.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long tokenValidityInMilliseconds;

    public JwtTokenProvider(@Value("${jwt.secret:defaultSecretKeyForJWTTokenThatShouldBeChangedInProduction}") String secret,
                            @Value("${jwt.token-validity-in-seconds:86400}") long tokenValidityInSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
    }

    // JWT 토큰 생성
    public String createToken(String userId, Long userCode) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(userId)
                .claim("userCode", userCode)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 토큰에서 사용자 ID 추출
    public String getUserId(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // JWT 토큰에서 사용자 코드 추출
    public Long getUserCode(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userCode", Long.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(key)  // secretKey → key로 수정
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // JWT 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Authorization 헤더에서 토큰 추출
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public Long getUserCodeFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = resolveToken(authHeader);
        if (token != null && validateToken(token)) {
            return getUserCode(token);  // 기존 메서드 재사용
        }
        return null;
    }

}