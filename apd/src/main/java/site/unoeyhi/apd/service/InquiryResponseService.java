package site.unoeyhi.apd.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.dto.inquiry.InquiryResponseRequestDto;
import site.unoeyhi.apd.entity.InquiryResponse;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.entity.MemberInquiry;
import site.unoeyhi.apd.repository.InquiryResponseRepository;
import site.unoeyhi.apd.repository.MemberInquiryRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryResponseService {

    private final InquiryResponseRepository responseRepository;
    private final MemberInquiryRepository inquiryRepository;

    // 답변 등록
    public Long createResponse(Member admin, InquiryResponseRequestDto dto) {
        // 문의글 존재 확인
        MemberInquiry inquiry = inquiryRepository.findById(dto.inquiryId())
                .orElseThrow(() -> new IllegalArgumentException("해당 문의글이 존재하지 않습니다."));

        InquiryResponse response = InquiryResponse.builder()
                .inquiry(inquiry)
                .admin(admin)  // 관리자 (Member)
                .title(dto.title())
                .responseText(dto.responseText())
                .responseDate(LocalDateTime.now())
                .build();

        responseRepository.save(response);
        return response.getResponseId();
    }
}
