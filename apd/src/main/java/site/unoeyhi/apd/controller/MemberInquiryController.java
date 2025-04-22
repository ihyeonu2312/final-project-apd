package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import site.unoeyhi.apd.dto.inquiry.MemberInquiryRequestDto;
import site.unoeyhi.apd.dto.inquiry.MemberInquiryDetailDto;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.repository.MemberRepository;
import site.unoeyhi.apd.service.MemberInquiryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inquiries")
public class MemberInquiryController {

    private final MemberRepository memberRepository;
    private final MemberInquiryService inquiryService;

    // ✅ 문의 등록
    @PostMapping
    public ResponseEntity<Long> createInquiry(@RequestBody MemberInquiryRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // JWT에서 subject로 꺼낸 이메일
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        Long inquiryId = inquiryService.createInquiry(member, dto);
        return ResponseEntity.ok(inquiryId);
    }

    // ✅ 내 문의 목록 조회
    @GetMapping
    public ResponseEntity<List<MemberInquiryDetailDto>> getMyInquiries() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        List<MemberInquiryDetailDto> inquiries = inquiryService.getMyInquiries(member);
        return ResponseEntity.ok(inquiries);
    }

    // ✅ 문의 상세 조회
    @GetMapping("/{inquiryId}")
    public ResponseEntity<MemberInquiryDetailDto> getInquiryDetail(@PathVariable Long inquiryId) {
        MemberInquiryDetailDto detail = inquiryService.getInquiryDetail(inquiryId);
        return ResponseEntity.ok(detail);
    }
} 