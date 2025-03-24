package site.unoeyhi.apd.dto;

public record ChatMessageRequestDto(
    Long senderId,
    String message
) {}
