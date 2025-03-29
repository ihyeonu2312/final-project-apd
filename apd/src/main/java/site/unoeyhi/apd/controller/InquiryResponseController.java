package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.unoeyhi.apd.dto.inquiry.InquiryResponseRequestDto;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.service.InquiryResponseService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/inquiries")
public class InquiryResponseController {

    private final InquiryResponseService responseService;

    // 문의 답변 등록
    @PostMapping("/response")
    public ResponseEntity<Long> createResponse(
            @AuthenticationPrincipal Member admin,
            @RequestBody InquiryResponseRequestDto dto
    ) {
        if (!admin.getRole().equals(Member.Role.관리자)) {
            return ResponseEntity.status(403).build(); // 권한 없음
        }

        Long responseId = responseService.createResponse(admin, dto);
        return ResponseEntity.ok(responseId);
    }
}
