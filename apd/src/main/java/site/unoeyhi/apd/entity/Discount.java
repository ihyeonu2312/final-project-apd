package site.unoeyhi.apd.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "discount")
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discount_id")
    private Long discountId;

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false) // ✅ 상품과 1:1 관계
    private Product product;

    @Column(name = "discount_type", nullable = false)
    private String discountType; // ✅ 할인 유형 (PERCENT, FIXED)

    @Column(name = "discount_value", nullable = false)
    private Double discountValue; // ✅ 할인 값

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate; // ✅ 할인 시작일

    @Column(name = "end_date")
    private LocalDate endDate; // ✅ 할인 종료일 (NULL 가능)

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
