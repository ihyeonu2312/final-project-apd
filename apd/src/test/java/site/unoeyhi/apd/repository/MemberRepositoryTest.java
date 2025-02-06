package site.unoeyhi.apd.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;

import jakarta.transaction.Transactional;
import site.unoeyhi.apd.entity.Member;

import java.time.LocalDateTime;

@SpringBootTest
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Test
    @Transactional
    @Rollback(false)
    public void testMemberSave() {
        // 데이터 생성
        Member member = Member.builder()
                .email("test@example.com")
                .password(encoder.encode("1234"))
                .nickname("testNickname")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .role(Member.Role.일반회원)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        
        // 데이터 저장
        memberRepository.save(member);
        memberRepository.flush();  // 즉시 DB에 반영

        // 저장 확인 (optional)
        System.out.println("Saved member ID: " + member.getMemberId());
    }
}