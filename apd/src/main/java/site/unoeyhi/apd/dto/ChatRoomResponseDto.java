package site.unoeyhi.apd.dto;

import java.time.LocalDateTime;

public record ChatRoomResponseDto(
    Long chatRoomId,
    Long buyerId,
    Long sellerId,
    Integer usedProductId,
    String productName,
    LocalDateTime createdAt
) {}
