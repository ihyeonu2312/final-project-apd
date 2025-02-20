package site.unoeyhi.apd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.unoeyhi.apd.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByNickname(String nickname);
    Optional<Member> findByPhoneNumber(String phoneNumber);
}