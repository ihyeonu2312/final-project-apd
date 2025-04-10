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
@EntityListeners(org.springframework.data.jpa.domain.support.AuditingEntityListener.class)
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long optionId;

    @Column(name = "option_value_type", nullable = false)
    private String optionValueType;

    @Column(name = "option_value", nullable = false)
    private String optionValue;

    @Column(name = "price_gap", nullable = false, columnDefinition = "int default 0")
    private int priceGap;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
