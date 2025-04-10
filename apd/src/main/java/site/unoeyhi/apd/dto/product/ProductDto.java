package site.unoeyhi.apd.dto.product;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.*;
import site.unoeyhi.apd.entity.Discount;
import site.unoeyhi.apd.entity.Option;
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
    private List<ReviewDto> reviews; // ✅ 리뷰 목록 추가

    private String detailUrl;             // 상세 페이지 URL
    private List<String> additionalImages; // 추가 이미지 리스트
    private Map<String, List<String>> options; //옵션 리스트
    private LocalDateTime createdAt;      // 생성 날짜
    private LocalDateTime updatedAt;      // 수정 날짜

    // ✅ `Product` 엔티티를 `ProductDto`로 변환하는 생성자
    public ProductDto(Product product, Double avgRating, Discount discount, List<ReviewDto> reviews) {
        this.productId = product.getProductId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.stockQuantity = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        this.categoryId = product.getCategory() != null ? product.getCategory().getCategoryId() : null;
        this.imageUrl = product.getImageUrl() != null ? product.getImageUrl() : "";
        this.thumbnailImageUrl = product.getThumbnailImageUrl() != null ? product.getThumbnailImageUrl() : "";
        this.rating = avgRating != null ? avgRating : 0.0;
        this.detailUrl = product.getDetailUrl() != null ? product.getDetailUrl() : "";
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
        this.reviews = reviews;
    
        //할인 계산
        if (discount != null && isDiscountValid(discount)) {
            this.discountPrice = discount.getDiscountValue();
            this.originalPrice = this.price + this.discountPrice;
        } else {
            this.discountPrice = 0.0;
            this.originalPrice = this.price;
        }

        // ✅ 옵션 가공하여 Map<String, List<String>> 형태로 설정
        if (product.getOptions() != null) {
            this.options = product.getOptions().stream()
                .map(OptionDto::new) // Option → OptionDto 변환
                .collect(Collectors.groupingBy(
                    OptionDto::getOptionValueType,
                    Collectors.mapping(OptionDto::getOptionValue, Collectors.toList())
                ));
        } else {
            this.options = Map.of(); // 빈 맵 처리
        }
        
    }
    
    public ProductDto(Product product, Double avgRating, Discount discount) {
        this(product, avgRating, discount, List.of()); // 기본값으로 빈 리뷰 리스트 전달
    }
    

    // ✅ 할인 기간이 유효한지 확인하는 메서드
    private boolean isDiscountValid(Discount discount) {
        LocalDate today = LocalDate.now();
        return (discount.getStartDate().isBefore(today) || discount.getStartDate().isEqual(today)) &&
            (discount.getEndDate() == null || discount.getEndDate().isAfter(today));
    }

}
