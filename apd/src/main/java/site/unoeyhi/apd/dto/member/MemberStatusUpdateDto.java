package site.unoeyhi.apd.dto.member;

public record MemberStatusUpdateDto(
    String status // 예: "ACTIVE", "BANNED", "DEACTIVATED" 등
) {}
