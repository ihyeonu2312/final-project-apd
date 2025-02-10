package site.unoeyhi.apd.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "Category")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Setter
public class Category{

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long categoryId;  // 카테고리 ID

  private String name;  // 카테고리 이름

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_category_id")
  private Category parentCategory;  // 상위 카테고리 (Self-referencing FK)

}