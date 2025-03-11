package site.unoeyhi.apd.dto.product;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Long reviewId;
    private Long productId;
    private Long memberId;
    private Double rating;
    private String comment;
    private String reviewImageUrl;
    private String createdAt;
}
