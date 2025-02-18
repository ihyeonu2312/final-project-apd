package site.unoeyhi.apd.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    private static final String SECRET_KEY = "your_secret_key_must_be_at_least_256_bits_long_!!!!"; // 🔹 256비트 이상 키 사용 필수
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24시간 만료 (단위: 밀리초)

    // 🔹 시크릿 키를 HMAC SHA 키로 변환 (JWT 서명에 사용)
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    // ✅ 📌 JWT 생성 (이메일 기반)
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // ✅ 최신 방식 적용
                .compact();
    }


    // ✅ 📌 JWT에서 이메일 추출
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // ✅ 최신 방식 적용
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
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
     // ✅ 📌 JWT 생성 (클레임 포함)
     public String generateTokenWithClaims(String key, Boolean value, long expirationMillis) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(key, value); // 클레임에 개인정보 동의 여부 추가

        return Jwts.builder()
                .setClaims(claims) // 클레임 설정
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis)) // 만료 시간 설정
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 비밀키로 서명
                .compact();
    }

    // ✅ 📌 JWT 토큰을 파싱하여 클레임 추출
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey()) // 서명 키 확인
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("JWT 토큰이 만료되었습니다.");
        } catch (JwtException e) {
            throw new RuntimeException("JWT 토큰이 유효하지 않습니다.");
        }
    }
}