package site.unoeyhi.apd.repository;

import site.unoeyhi.apd.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
}
// ✔️ findByEmail() → 이메일로 회원 검색