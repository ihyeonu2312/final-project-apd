// package site.unoeyhi.apd.security;

// import site.unoeyhi.apd.util.JwtUtil;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.web.filter.OncePerRequestFilter;
// import org.springframework.stereotype.Component;

// import java.io.IOException;

// @Component
// public class JwtFilter extends OncePerRequestFilter {
//     private final JwtUtil jwtUtil;

//     public JwtFilter(JwtUtil jwtUtil) {
//         this.jwtUtil = jwtUtil;
//     }

//     @Override
//     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
//             throws ServletException, IOException {
//         String token = request.getHeader("Authorization");
//         if (token != null && token.startsWith("Bearer ")) {
//             String email = jwtUtil.extractEmail(token.substring(7));
//             SecurityContextHolder.getContext().setAuthentication(new JwtAuthentication(email));
//         }
//         chain.doFilter(request, response);
//     }
// }
