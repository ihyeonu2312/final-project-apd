package site.unoeyhi.apd.service.product;

import site.unoeyhi.apd.dto.product.OptionDto;
import site.unoeyhi.apd.dto.product.ProductDto;
import site.unoeyhi.apd.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    Product saveProduct(ProductDto productDto);
    List<ProductDto> getAllProducts();
    Optional<Product> findByTitle(String title);
    List<Product> getProductsByCategoryId(Long categoryId);
    List<ProductDto> getProductsByCategory(Long categoryId);

    void saveProductOption(Long productId, OptionDto optionDto); // ✅ 옵션 저장
    void saveProductImage(Long productId, String imageUrl, boolean isThumbnail); // ✅ 추가 이미지 저장
    void saveProductDiscount(Product product, String discountType, double discountPrice); // ✅ 할인 저장
}

