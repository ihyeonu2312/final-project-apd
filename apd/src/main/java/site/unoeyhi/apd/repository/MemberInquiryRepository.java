package site.unoeyhi.apd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.unoeyhi.apd.entity.MemberInquiry;

import java.util.List;

public interface MemberInquiryRepository extends JpaRepository<MemberInquiry, Long> {
    List<MemberInquiry> findByMember_MemberId(Long memberId); // 사용자별 문의 목록 조회
}
