package site.unoeyhi.apd.service.product;

import site.unoeyhi.apd.dto.ProductImageDto;
import site.unoeyhi.apd.entity.ProductImage;

import java.util.List;

public interface ProductImageService {
    ProductImage saveProductImage(ProductImageDto productImageDto); // ✅ 상품 이미지 저장
    List<String> getProductImages(Long productId); // ✅ 특정 상품의 이미지 리스트 조회
}
