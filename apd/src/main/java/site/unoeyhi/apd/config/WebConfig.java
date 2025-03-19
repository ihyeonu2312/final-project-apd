// package site.unoeyhi.apd.config;

// import org.springframework.context.annotation.Configuration;
// import org.springframework.lang.NonNull;
// import org.springframework.web.servlet.config.annotation.CorsRegistry;
// import org.springframework.web.servlet.config.annotation.EnableWebMvc;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @Configuration
// @EnableWebMvc
// public class WebConfig implements WebMvcConfigurer {

//     @Override
//     public void addCorsMappings(@NonNull CorsRegistry registry) {
//         registry.addMapping("/**")  // ✅ 모든 엔드포인트 허용
//                 .allowedOrigins("http://localhost:5173")  // ✅ 프론트엔드 URL 허용
//                 .allowedMethods("OPTIONS", "GET", "POST", "PUT", "PATCH", "DELETE") // ✅ OPTIONS 포함
//                 .allowedHeaders("*")  // ✅ 모든 헤더 허용
//                 .exposedHeaders("Authorization", "Set-Cookie") // ✅ 클라이언트에서 JWT, 쿠키 접근 허용
//                 .allowCredentials(true) // ✅ 인증 정보 포함 허용
//                 .maxAge(3600); // ✅ 1시간(3600초) 동안 CORS 정책을 캐시하여 불필요한 요청 방지
//     }
    
// }
