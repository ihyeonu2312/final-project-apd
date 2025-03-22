package site.unoeyhi.apd.service.product;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.entity.ProductDetailImage;
import site.unoeyhi.apd.repository.product.ProductDetailImageRepository;
import site.unoeyhi.apd.repository.product.ProductRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductDetailImageService {

    private final ProductDetailImageRepository productDetailImageRepository;
    private final ProductRepository productRepository;

    /** ✅ 상세 이미지 저장 */
   
    public void saveDetailImages(Long productId, List<String> imageUrls) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        for (String imageUrl : imageUrls) {
            ProductDetailImage image = new ProductDetailImage();
            image.setProduct(product);
            image.setImageUrl(imageUrl);
            image.setCreatedAt(LocalDateTime.now());
            productDetailImageRepository.save(image);
        }
        System.out.println("✅ [DB 저장 완료] " + imageUrls.size() + "개 이미지 저장됨!");
    }

    /** ✅ 상품 상세 정보 조회 (이미지가 없는 상품만) */
    public List<Map<String, Object>> findAllProductDetails() {
        return productDetailImageRepository.findAllProductDetails();
    }
    public List<ProductDetailImage> getDetailImagesByProductId(Long productId) {
        return productDetailImageRepository.findByProduct_ProductId(productId);
    }

}
