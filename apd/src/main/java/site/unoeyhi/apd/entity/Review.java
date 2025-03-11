package site.unoeyhi.apd.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;  // 리뷰 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY)  // 🔹 상품과 연결 (ManyToOne)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "review_member_id", nullable = false) // 🔹 작성한 회원 ID
    private Long memberId;

    @Column(nullable = false)
    private Double rating; // 🔹 별점

    @Column(nullable = false, length = 500)
    private String comment; // 🔹 리뷰 내용

    @Column(name = "review_image_url")
    private String reviewImageUrl; // 🔹 리뷰 이미지 (선택사항)

    @Column(nullable = false, updatable = false)
    private String createdAt; // 🔹 리뷰 작성 날짜
}
