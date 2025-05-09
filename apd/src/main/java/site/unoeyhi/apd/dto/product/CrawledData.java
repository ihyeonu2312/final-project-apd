package site.unoeyhi.apd.dto.product;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrawledData {
    private String name;
    private double price;
    private int stockQuantity;
    private String imageUrl;
    private Long categoryId; // ✅ 크롤링한 데이터에서 categoryId 매핑
}
