package sks.poketmon.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * JWT 토큰 생성
     */
    public String generateToken(String userId, String userName, Long userCode) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(userId)
                .claim("userName", userName)
                .claim("userCode", userCode)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * JWT 토큰에서 사용자 ID 추출
     */
    public String getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * JWT 토큰에서 사용자 이름 추출
     */
    public String getUserNameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userName", String.class);
    }

    /**
     * JWT 토큰에서 사용자 코드 추출
     */
    public Long getUserCodeFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userCode", Long.class);
    }

    /**
     * JWT 토큰에서 만료 시간 추출
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * JWT 토큰에서 Claims 추출
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * JWT 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            System.err.println("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            System.err.println("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            System.err.println("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            System.err.println("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            System.err.println("JWT claims string is empty");
        }
        return false;
    }

    /**
     * JWT 토큰 만료 여부 확인
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * JWT 토큰 갱신
     */
    public String refreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String userId = claims.getSubject();
            String userName = claims.get("userName", String.class);
            Long userCode = claims.get("userCode", Long.class);

            return generateToken(userId, userName, userCode);
        } catch (Exception e) {
            return null;
        }
    }
}