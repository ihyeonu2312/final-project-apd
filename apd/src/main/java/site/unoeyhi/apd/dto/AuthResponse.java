package site.unoeyhi.apd.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;  // JWT 필드
    private Long memberId; // ✅ memberId 추가
}
