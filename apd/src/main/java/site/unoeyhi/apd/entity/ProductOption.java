package site.unoeyhi.apd.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "product_option")
public class ProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_id")
    private Long productOptionId; // 옵션-상품 매핑 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) // ✅ 상품 삭제 시 옵션 매핑도 삭제됨
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // 상품 (Foreign Key)

    @ManyToOne(fetch = FetchType.LAZY) // ✅ 옵션은 여러 상품과 연결될 수 있으므로 CascadeType 설정 X
    @JoinColumn(name = "option_id", nullable = false)
    private Option option; // 옵션 (Foreign Key)
}
