package site.unoeyhi.apd.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.unoeyhi.apd.dto.member.AdminMemberDto;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.repository.MemberRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMemberService {

    private final MemberRepository memberRepository;

    // 🔹 전체 회원 조회
    public List<AdminMemberDto> getAllMembers() {
        return memberRepository.findAll().stream()
            .map(member -> new AdminMemberDto(
                member.getMemberId(),
                member.getEmail(),
                member.getNickname(),
                member.getRole().name(),
                member.getStatus().name(),
                member.getCreatedAt().toString()
            ))
            .toList();
    }

    // 🔹 회원 권한 변경
    public void updateRole(Long memberId, String newRole) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        member.setRole(Member.Role.valueOf(newRole));
        memberRepository.save(member);
    }

    // 🔹 회원 상태 변경
    public void updateStatus(Long memberId, String newStatus) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        member.setStatus(Member.MemberStatus.valueOf(newStatus));
        memberRepository.save(member);
    }
}
