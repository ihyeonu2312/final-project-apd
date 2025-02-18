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

    // ✅ 📌 새로운 generateTokenWithClaims 추가 (개인정보 동의 토큰 발급)
    public String generateTokenWithClaims(String key, Boolean value, long expirationMillis) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(key, value);

        return Jwts.builder()
                .setClaims(claims) // ✅ 클레임 추가
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // ✅ secretKey 오류 수정
                .compact();
    }

    // ✅ 📌 JWT에서 Claims(데이터) 추출
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // ✅ 최신 방식 적용
                .build()
                .parseClaimsJws(token)
                .getBody();
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
}
