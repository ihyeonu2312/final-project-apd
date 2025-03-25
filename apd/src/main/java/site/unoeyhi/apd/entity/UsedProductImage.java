package site.unoeyhi.apd.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "used_product_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsedProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이미지 URL (S3 경로 또는 로컬 경로 저장)
    @Column(nullable = false)
    private String imageUrl;

    // 어떤 상품의 이미지인지 (다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_product_id")
    private UsedProduct usedProduct;
}
