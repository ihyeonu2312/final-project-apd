package site.unoeyhi.apd.entity.dto;

import jakarta.persistence.*;
import lombok.*;
import site.unoeyhi.apd.entity.Member;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale.Category;

@Entity(name = "tbl_product")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Product {
  
  Member member;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long productId;  // 상품 ID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "admin_id")
  private Member admin;  // 관리자 (FK -> Member)

  private String name;      // 상품 이름
  private String description;  // 상품 설명
  private Double price;     // 상품 가격
  private Integer stockQuantity;  // 재고 수량

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;  // 카테고리 (FK -> Category)

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // FileMetadata와 OneToMany 관계
  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<FileMetadata> fileMetadataList;  // 상품 이미지 목록
}
