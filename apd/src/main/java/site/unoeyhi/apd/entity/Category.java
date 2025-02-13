package site.unoeyhi.apd.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false)
    private String name;

     // ✅ 부모 카테고리 설정 (같은 Category를 참조)
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "parent_category_id")
     private Category parentCategory;
 
     // ✅ 하위 카테고리 리스트
     @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL)
     private List<Category> subCategories = new ArrayList<>();
 
     // ✅ 상품과의 다대다 관계 (양방향 매핑)
     @ManyToMany(mappedBy = "categories", cascade = CascadeType.PERSIST)
     private List<Product> products = new ArrayList<>();
 
 }