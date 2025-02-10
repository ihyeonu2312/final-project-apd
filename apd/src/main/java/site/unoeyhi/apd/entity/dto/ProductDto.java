package site.unoeyhi.apd.entity.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long adminId;         // 관리자 ID (FK)
    private String name;          // 상품 이름
    private String description;   // 상품 설명
    private Double price;         // 상품 가격
    private Integer stockQuantity; // 재고 수량
    private List<Long> categoryIds;  // 복수 카테고리 ID 리스트
}