package site.unoeyhi.apd.dto.product;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductSummaryDto {
    private Long productId;
    private String name;
    private String imageUrl;
    private Double price;
}