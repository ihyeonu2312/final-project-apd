package site.unoeyhi.apd.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String SECRET_KEY = "hUqkplDA80F8OLxXgwUI9sZUAHAqOrKKqrNT5QLFONw=";


    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24시간 만료 (단위: 밀리초)

    // 🔹 시크릿 키를 HMAC SHA 키로 변환 (JWT 서명에 사용)
    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    // ✅ 📌 JWT 생성 (이메일 기반)
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("authType", "EMAIL")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // ✅ 최신 방식 적용
                .compact();
    }


     // ✅ 📌 JWT 생성 (카카오 기반)
     public String generateTokenForKakao(Long kakaoId) {
        return Jwts.builder()
                .setSubject(String.valueOf(kakaoId))
                .claim("authType", "KAKAO") // 🔹 카카오 인증 방식 추가
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ 📌 JWT에서 이메일 추출
    public String extractSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // ✅ 최신 방식 적용
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public String extractAuthType(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(SECRET_KEY)
            .parseClaimsJws(token)
            .getBody();
        return (String) claims.get("authType");
    }

    // ✅ 📌 JWT 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // ✅ 최신 방식 적용
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false; // 유효하지 않은 토큰
        }
    }
    
}