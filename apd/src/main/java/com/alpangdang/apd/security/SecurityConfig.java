package com.alpangdang.config;

import com.alpangdang.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.MembernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class JwtFilter extends MembernamePasswordAuthenticationFilter {
    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            String email = jwtUtil.extractEmail(token.substring(7));
            SecurityContextHolder.getContext().setAuthentication(new JwtAuthentication(email));
        }
        chain.doFilter(request, response);
    }
}
