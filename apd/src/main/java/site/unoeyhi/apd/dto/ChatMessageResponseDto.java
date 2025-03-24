package site.unoeyhi.apd.dto;

import java.time.LocalDateTime;

public record ChatMessageResponseDto(
    Long chatMessageId,
    Long senderId,
    String senderNickname,
    String message,
    LocalDateTime sentAt,
    boolean isRead
) {}
