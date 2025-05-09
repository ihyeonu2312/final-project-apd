package site.unoeyhi.apd.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "product_image")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY) // ✅ 상품과 N:1 관계 설정
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "is_thumbnail", nullable = false)
    private boolean isThumbnail;  // ✅ 썸네일 여부 추가

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ✅ 48x48 이미지 URL을 492x492 URL로 변환하는 메서드 추가
    public String getLargeImageUrl() {
        if (imageUrl.contains("48x48")) {
            return imageUrl.replace("48x48", "492x492");
        }
        return imageUrl;
    }

    @Builder
    public ProductImage(String imageUrl, boolean isThumbnail, Product product) { // ✅ boolean으로 수정
        this.imageUrl = imageUrl;
        this.isThumbnail = isThumbnail;
        this.product = product;
    }
}
