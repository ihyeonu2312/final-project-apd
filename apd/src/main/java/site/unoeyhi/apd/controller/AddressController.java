package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.unoeyhi.apd.service.AddressService;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {
    
    private final AddressService addressService;

    // 📌 클라이언트에서 Kakao 주소 검색 요청
    @GetMapping("/search")
    public ResponseEntity<String> searchAddress(@RequestParam String query) {
        String result = addressService.searchAddress(query);
        return ResponseEntity.ok(result);
    }
}
