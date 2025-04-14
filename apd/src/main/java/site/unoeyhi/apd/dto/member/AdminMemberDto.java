package site.unoeyhi.apd.dto.member;

public record AdminMemberDto(
    Long memberId,
    String email,
    String nickname,
    String role,
    String status,
    String createdAt
) {}
