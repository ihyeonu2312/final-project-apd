package site.unoeyhi.apd.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import site.unoeyhi.apd.entity.Member;

import java.time.LocalDateTime;

@SpringBootTest
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void testMemberSave() {
        // 데이터 생성
        Member member = Member.builder()
                .email("test@example.com")
                .password("password123")
                .nickname("testNickname")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .role(Member.Role.일반회원)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 데이터 저장
        memberRepository.save(member);

        // 저장 확인 (optional)
        System.out.println("Saved member ID: " + member.getMemberId());
    }
}