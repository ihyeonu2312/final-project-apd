package site.unoeyhi.apd.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import site.unoeyhi.apd.repository.CategoryRepository;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true) // ✅ Builder에서 모든 필드를 포함하도록 설정
@Table(name = "product") // ✅ 테이블명 소문자로 변경
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId; // ✅ 상품 ID (PK, 자동 증가)

    @Column(name = "admin_id", nullable = true) // ✅ nullable 허용
    private Long adminId; // ✅ 관리자 ID (크롤링 데이터에서 값 없을 수도 있음)

    @Column(nullable = false)
    private String name; // ✅ 상품 이름

    @Column(nullable = false)
    private Double price; // ✅ 상품 가격

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity; // ✅ 재고 수량

    // ✅ ManyToOne 관계 설정 (외래키: category_id)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ✅ 이미지 URL 필드 추가
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "thumbnail_image_url")
    private String thumbnailImageUrl;

    @Column(name = "detail_url", nullable = false)
    private String detailUrl; // ✅ 상세 페이지 URL 필수 필드로 지정

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOption> productOptions = new ArrayList<>();

    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    // 🛠 크롤링한 데이터에서 categoryName을 category_id로 자동 매핑하는 메서드 추가
    public void setCategoryByName(String categoryName, CategoryRepository categoryRepository) {
        this.category = categoryRepository.findByCategoryName(categoryName)
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setCategoryName(categoryName);
                    return categoryRepository.save(newCategory);
                });
    }
    
}
