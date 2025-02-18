package site.unoeyhi.apd.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true) // ✅ Builder에서 모든 필드를 포함하도록 설정
@Table(name = "product") // ✅ 테이블명 소문자로 변경
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId; // ✅ 상품 ID (PK, 자동 증가)

    @Column(name = "admin_id", nullable = false)
    private Long adminId; // ✅ 관리자 ID 추가

    @Column(nullable = false)
    private String name; // ✅ 상품 이름

    @Column(columnDefinition = "TEXT")
    private String description; // ✅ 상품 설명

    @Column(nullable = false)
    private Double price; // ✅ 상품 가격

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity; // ✅ 재고 수량

    // ✅ ManyToOne 관계 설정 (외래키: category_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ✅ 이미지 URL 필드 추가
    @Column(name = "image_url")
    private String imageUrl;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
