package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import site.unoeyhi.apd.util.JwtUtil;
import site.unoeyhi.apd.entity.dto.LoginRequest;
import site.unoeyhi.apd.entity.dto.AuthResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // 사용자 인증 수행
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // SecurityContext에 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // JWT 생성
        String jwt = jwtUtil.generateToken(authentication.getName());

        // 응답 반환
        return ResponseEntity.ok(new AuthResponse(jwt));
    }
}
