package site.unoeyhi.apd.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "category")
public class CategoryModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본 키 (자동 증가)

    @Column(nullable = false, unique = true)
    private String categoryName; // 카테고리명

    @Column(nullable = false)
    private String categoryUrl; // 카테고리 URL

}
