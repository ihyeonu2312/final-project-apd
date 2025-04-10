package site.unoeyhi.apd.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "category")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "products"})  // ✅ Lazy 로딩 문제 해결
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", updatable = false, nullable = false)
    private Long categoryId;

    @Column(nullable = false, unique = true) 
    private String categoryName; // ✅ 쿠팡 카테고리명

    private String coupangCategoryId; // DB의 coupang_category_id 컬럼

    @Column(nullable = false, unique = true)
    private String url; // DB의 url 컬럼

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();

    
}