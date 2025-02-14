package site.unoeyhi.apd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

@Log4j2
@Service
@RequiredArgsConstructor
public class AddressService {
    
    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private final RestTemplate restTemplate;  // ✅ ExternalApiConfig에서 주입됨


    
    // 📌 Kakao 주소 검색 API 호출
    public String searchAddress(String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = UriComponentsBuilder
                .fromHttpUrl("https://dapi.kakao.com/v2/local/search/address.json")
                .queryParam("query", encodedQuery)
                .toUriString();
    
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);
    
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    
        // ✅ Kakao API 응답을 서버 로그에 출력
        log.info("🔍 Kakao API 응답: {}", response.getBody());
    
        return response.getBody(); // JSON 반환
    }
}