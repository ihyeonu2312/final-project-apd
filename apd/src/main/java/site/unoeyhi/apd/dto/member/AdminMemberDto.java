package site.unoeyhi.apd.dto.member;
// 관리자페이지 멤버 리스트
public record AdminMemberDto(
    Long memberId,
    String email,
    String nickname,
    String role,
    String status,
    String createdAt
) {}
