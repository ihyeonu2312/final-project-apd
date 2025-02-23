package site.unoeyhi.apd.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "`option`")
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long optionId; // 옵션 ID (PK)

    @Column(name = "option_value_type", nullable = false)
    private String optionValueType; // 옵션 타입 (예: 색상, 크기)

    @Column(name = "option_value", nullable = false)
    private String optionValue; // 옵션 값 (예: 빨강, XL)

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 생성 날짜 (자동 저장)
}
