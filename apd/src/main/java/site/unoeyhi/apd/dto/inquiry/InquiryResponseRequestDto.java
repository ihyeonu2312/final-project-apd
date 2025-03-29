package site.unoeyhi.apd.dto.inquiry;

public record InquiryResponseRequestDto(
    Long inquiryId,
    String title,
    String responseText
) {}
