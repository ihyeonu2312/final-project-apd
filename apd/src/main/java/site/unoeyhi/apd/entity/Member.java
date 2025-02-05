package site.unoeyhi.apd.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;
}
// ✔️ @Entity(name = "members") → 테이블명 members
// ✔️ @Id, @GeneratedValue → 기본 키 자동 증가
// ✔️ Lombok 사용해서 getter, setter 자동 생성