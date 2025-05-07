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
    private String nickname;
    private String reviewImageUrl;
    private String createdAt;

    public ReviewDto(Long reviewId, Long productId, Long memberId, String nickname,
                 Double rating, String comment, String reviewImageUrl, String createdAt) {
    this.reviewId = reviewId;
    this.productId = productId;
    this.memberId = memberId;
    this.nickname = nickname;
    this.rating = rating;
    this.comment = comment;
    this.reviewImageUrl = reviewImageUrl;
    this.createdAt = createdAt;
}


}
