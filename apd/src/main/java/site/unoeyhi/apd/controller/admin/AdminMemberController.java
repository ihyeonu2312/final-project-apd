package site.unoeyhi.apd.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.unoeyhi.apd.dto.member.AdminMemberDto;
import site.unoeyhi.apd.dto.member.MemberRoleUpdateDto;
import site.unoeyhi.apd.dto.member.MemberStatusUpdateDto;
import site.unoeyhi.apd.service.AdminMemberService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/members")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    // 🔹 전체 회원 조회
    @GetMapping
    public ResponseEntity<List<AdminMemberDto>> getAllMembers() {
        return ResponseEntity.ok(adminMemberService.getAllMembers());
    }

    // 🔹 권한 변경
    @PatchMapping("/{memberId}/role")
    public ResponseEntity<Void> updateRole(
            @PathVariable Long memberId,
            @RequestBody MemberRoleUpdateDto dto
    ) {
        adminMemberService.updateRole(memberId, dto.role());
        return ResponseEntity.ok().build();
    }

    // 🔹 상태 변경 (예: 정상 → 정지)
    @PatchMapping("/{memberId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long memberId,
            @RequestBody MemberStatusUpdateDto dto
    ) {
        adminMemberService.updateStatus(memberId, dto.status());
        return ResponseEntity.ok().build();
    }
}
