package site.unoeyhi.apd.dto;

public record ChatRoomRequestDto(
    Long buyerId,
    Long sellerId,
    Integer productId
) {}
