package site.unoeyhi.apd.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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
    private Integer usedProductId; // DB에서 INT니까 Integer로

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private Member seller;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal price;

    private String condition;

    private String status;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    // 양방향 매핑 (옵션)
    @OneToMany(mappedBy = "usedProduct", cascade = CascadeType.ALL)
    private List<ChatRoom> chatRooms = new ArrayList<>();
}
