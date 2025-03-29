package site.unoeyhi.apd.dto.inquiry;

import java.time.LocalDateTime;
import java.util.List;
// 문의 상세 + 답변 포함 조회
public record MemberInquiryDetailDto(
    Long inquiryId,
    String title,
    String questionText, // ✅ 필드명 맞추기
    String status,
    LocalDateTime createdAt,
    List<InquiryResponseDto> responses
) {}

