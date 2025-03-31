package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.unoeyhi.apd.dto.inquiry.InquiryResponseRequestDto;
import site.unoeyhi.apd.dto.inquiry.MemberInquiryDetailDto;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.service.InquiryResponseService;
import site.unoeyhi.apd.service.MemberInquiryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/inquiries")
public class InquiryResponseController {

    private final InquiryResponseService responseService;
    private final MemberInquiryService inquiryService; // ✅ 추가

    @PostMapping("/response")
    public ResponseEntity<Long> createResponse(
            @AuthenticationPrincipal Member admin,
            @RequestBody InquiryResponseRequestDto dto
    ) {
        if (!admin.getRole().equals(Member.Role.관리자)) {
            return ResponseEntity.status(403).build();
        }

        Long responseId = responseService.createResponse(admin, dto);
        return ResponseEntity.ok(responseId);
    }

    @GetMapping("")
    public ResponseEntity<List<MemberInquiryDetailDto>> getAllInquiries(@AuthenticationPrincipal Member admin) {
        System.out.println("✅ 관리자 인증 정보: " + admin); // 이거 찍어보면 바로 확인됨
        if (!admin.getRole().equals(Member.Role.관리자)) {
            return ResponseEntity.status(403).build();
        }

        List<MemberInquiryDetailDto> inquiries = inquiryService.getAllInquiries(); // ✅ 서비스 연결
        return ResponseEntity.ok(inquiries);
    }
}
