package site.unoeyhi.apd.dto.product;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.*;
import site.unoeyhi.apd.entity.Discount;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.entity.ProductImage;
import site.unoeyhi.apd.entity.ProductOption;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ProductDto {
    private Long productId;               // 상품 ID
    private String name;                  // 상품명
    private Double originalPrice;        // 원가 (소비자가격) 추가
    private Double price;                 // 가격
    private Double discountPrice;       
    private Integer stockQuantity;        // 재고 수량
    private Long categoryId;              // 카테고리 ID
    private String imageUrl;              // 상품 이미지 URL
    private String thumbnailImageUrl;     // 썸네일 이미지 URL
    private Double rating;                // 평점
    private String detailUrl;             // 상세 페이지 URL
    private List<String> additionalImages; // 추가 이미지 리스트
    private List<OptionDto> options;       // 옵션 리스트
    private LocalDateTime createdAt;      // 생성 날짜
    private LocalDateTime updatedAt;      // 수정 날짜

    // ✅ `Product` 엔티티를 `ProductDto`로 변환하는 생성자
    public ProductDto(Product product, Double avgRating, Discount discount) {
        this.productId = product.getProductId();
        this.name = product.getName();
        this.price = product.getPrice(); // 상품가 (할인 적용 후 가격)
        this.stockQuantity = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        this.categoryId = product.getCategory() != null ? product.getCategory().getCategoryId() : null;
        this.imageUrl = product.getImageUrl() != null ? product.getImageUrl() : "";
        this.thumbnailImageUrl = product.getThumbnailImageUrl() != null ? product.getThumbnailImageUrl() : "";
        this.rating = avgRating != null ? avgRating : 0.0;
        this.detailUrl = product.getDetailUrl() != null ? product.getDetailUrl() : "";
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
    
        // ✅ 할인 가격 설정 (할인이 없으면 discountPrice = 0)
        if (discount != null && isDiscountValid(discount)) {
            this.discountPrice = discount.getDiscountValue(); // 할인 금액
            this.originalPrice = this.price + this.discountPrice; // 원가 = 상품가 + 할인가
        } else {
            this.discountPrice = 0.0; // 할인 없음
            this.originalPrice = this.price; // 원가 = 상품가
        }
    }
    

// ✅ 할인 기간이 유효한지 확인하는 메서드
private boolean isDiscountValid(Discount discount) {
    LocalDate today = LocalDate.now();
    return (discount.getStartDate().isBefore(today) || discount.getStartDate().isEqual(today)) &&
           (discount.getEndDate() == null || discount.getEndDate().isAfter(today));
}

}
