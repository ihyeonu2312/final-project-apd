package site.unoeyhi.apd.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false, unique = true)
    private String name; // ✅ 카테고리명

    @Column(nullable = false, unique = true)
    private String url; // ✅ AliExpress 카테고리 URL

    @Column(nullable = false)
    private String categoryName; // ✅ AliExpress 원본 카테고리명

    @Column(nullable = false)
    private String categoryUrl; // ✅ AliExpress 원본 URL
}
