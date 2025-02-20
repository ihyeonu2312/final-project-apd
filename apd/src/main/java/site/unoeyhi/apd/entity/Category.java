package site.unoeyhi.apd.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", updatable = false, nullable = false)
    private Long categoryId;

    // @Column(name = "coupang_category_key", nullable = true, unique = true)
    // private String coupangCategoryKey; // ✅ 쿠팡 카테고리 키 (예: "fashion", "electronics")

    @Column(nullable = false, unique = true)
    private String categoryName; // ✅ 쿠팡 카테고리명

    // @Column(nullable = false, unique = true)
    // private String categoryUrl; // ✅ 쿠팡 카테고리 URL

}