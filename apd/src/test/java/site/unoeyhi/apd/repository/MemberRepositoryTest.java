package site.unoeyhi.apd.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.entity.Member.MemberStatus;

import java.time.LocalDateTime;
import java.util.logging.LogManager;
import java.util.logging.Logger;

@SpringBootTest
@Log4j2
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    // @Autowired
    // private PasswordEncoder encoder;

    @Test
    public void testExist() {
        log.info(memberRepository);
    }

    @Test
    @Transactional
    @Rollback(false)
    public void testMemberSave() {
        // 데이터 생성
        Member member = Member.builder()
                .name("이현우")
                .email("test@example.com")  
                // .password(encoder.encode("1234"))
                .password("1234")
                .nickname("testNickname")
                .phone("010-1234-5678")
                .address("서울시 강남구")
                .detailAdd("청담 자이아파트 102동204호")
                .role(Member.Role.일반회원)
                .status(MemberStatus.INACTIVE)
                .deletedAt(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastPass(null)
                .build();


        // 데이터 저장
        memberRepository.save(member);
        memberRepository.flush();  // 즉시 DB에 반영

        // 저장 확인 (optional)
        log.info("Saved member ID: " + member.getMemberId());
    }
}