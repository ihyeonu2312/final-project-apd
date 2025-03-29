package site.unoeyhi.apd.dto.inquiry;

import java.time.LocalDateTime;
// 답변 조회용
public record InquiryResponseDto(
    Long responseId,
    String title,
    String responseText,
    LocalDateTime responseDate
) {}
