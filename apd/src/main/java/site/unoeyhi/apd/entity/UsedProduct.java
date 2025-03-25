package site.unoeyhi.apd.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "used_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsedProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer usedProductId;  // 🔹 자동 증가 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Member seller;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private Condition condition;  // 🔹 상품 상태 (Enum)

    @Enumerated(EnumType.STRING)
    private Status status;        // 🔹 거래 상태 (Enum)

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;  // 🔹 Soft Delete 용

    // 채팅방 연관관계 (양방향)
    @OneToMany(mappedBy = "usedProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoom> chatRooms = new ArrayList<>();

    // 이미지 연관관계 (추후 구현용)
    @OneToMany(mappedBy = "usedProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsedProductImage> images = new ArrayList<>();

    // 상품 상태
    public enum Condition {
        새상품, 최상, 상, 중, 하
    }

    // 거래 상태
    public enum Status {
        판매중, 예약중, 거래완료
    }
}
