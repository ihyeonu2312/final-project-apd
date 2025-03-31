package site.unoeyhi.apd.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.dto.inquiry.*;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.entity.MemberInquiry;
import site.unoeyhi.apd.entity.InquiryResponse;
import site.unoeyhi.apd.repository.MemberInquiryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberInquiryService {

    private final MemberInquiryRepository inquiryRepository;

    // 문의 등록
    public Long createInquiry(Member member, MemberInquiryRequestDto dto) {
        MemberInquiry inquiry = MemberInquiry.builder()
                .member(member)
                .title(dto.title())
                .questionText(dto.questionText()) 
                .build();

        inquiryRepository.save(inquiry);
        return inquiry.getInquiryId();
    }

    // 내 문의 목록 조회
    @Transactional(readOnly = true)
    public List<MemberInquiryDetailDto> getMyInquiries(Member member) {
        List<MemberInquiry> inquiries = inquiryRepository.findByMember_MemberId(member.getMemberId());

        return inquiries.stream()
                .map(this::convertToDetailDto)
                .collect(Collectors.toList());
    }

    // 문의 상세 조회 (답변 포함)
    @Transactional(readOnly = true)
    public MemberInquiryDetailDto getInquiryDetail(Long inquiryId) {
        MemberInquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의글을 찾을 수 없습니다."));

        return convertToDetailDto(inquiry);
    }

    // 내부 변환용 메서드
    private MemberInquiryDetailDto convertToDetailDto(MemberInquiry inquiry) {
        List<InquiryResponseDto> responseDtos = inquiry.getResponses().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
                return new MemberInquiryDetailDto(
                  inquiry.getInquiryId(),       // ✅ inquiryId
                  inquiry.getTitle(),           // ✅ title
                  inquiry.getQuestionText(),   // ✅ questionText
                  inquiry.getStatus(),         // ✅ status
                  inquiry.getCreatedAt(),      // ✅ createdAt
                  responseDtos                  // ✅ responses
              );
              
    }

    private InquiryResponseDto convertToResponseDto(InquiryResponse response) {
        return new InquiryResponseDto(
                response.getResponseId(),
                response.getTitle(),
                response.getResponseText(),
                response.getResponseDate()
        );
    }
    @Transactional(readOnly = true)
public List<MemberInquiryDetailDto> getAllInquiries() {
    List<MemberInquiry> inquiries = inquiryRepository.findAll();

    return inquiries.stream()
            .map(this::convertToDetailDto)
            .collect(Collectors.toList());
}

}
