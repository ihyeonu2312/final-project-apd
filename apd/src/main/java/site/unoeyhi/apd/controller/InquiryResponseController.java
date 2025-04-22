package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.unoeyhi.apd.dto.inquiry.InquiryResponseRequestDto;
import site.unoeyhi.apd.dto.inquiry.MemberInquiryDetailDto;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.security.CustomUserDetails;
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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody InquiryResponseRequestDto dto
    ) {
        Member admin = userDetails.getMember();
        if (!admin.getRole().equals(Member.Role.관리자)) {
            return ResponseEntity.status(403).build();
        }
    
        Long responseId = responseService.createResponse(admin, dto);
        return ResponseEntity.ok(responseId);
    }
    

    @GetMapping("")
    public ResponseEntity<List<MemberInquiryDetailDto>> getAllInquiries(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Member admin = userDetails.getMember(); // 🔥 여기서 Member 추출
        if (!admin.getRole().equals(Member.Role.관리자)) {
            return ResponseEntity.status(403).build();
        }

        List<MemberInquiryDetailDto> inquiries = inquiryService.getAllInquiries();
        return ResponseEntity.ok(inquiries);
    }

    @GetMapping("/{id}")
public ResponseEntity<MemberInquiryDetailDto> getInquiryDetail(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
) {
    Member admin = userDetails.getMember();
    if (!admin.getRole().equals(Member.Role.관리자)) {
        return ResponseEntity.status(403).build();
    }

    MemberInquiryDetailDto detail = inquiryService.getInquiryDetail(id);
    return ResponseEntity.ok(detail);
}

}
