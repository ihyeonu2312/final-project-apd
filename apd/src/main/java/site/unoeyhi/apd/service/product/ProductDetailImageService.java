package site.unoeyhi.apd.service.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.entity.ProductDetailImage;
import site.unoeyhi.apd.repository.product.ProductDetailImageRepository;
import site.unoeyhi.apd.repository.product.ProductRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductDetailImageService {

    private final ProductDetailImageRepository productDetailImageRepository;
    private final ProductRepository productRepository;

    @Autowired
    public ProductDetailImageService(ProductDetailImageRepository productDetailImageRepository, ProductRepository productRepository) {
        this.productDetailImageRepository = productDetailImageRepository;
        this.productRepository = productRepository;
    }

    /**
     * ✅ 특정 상품의 상세 이미지 저장
     */
    public void saveDetailImages(Long productId, List<String> imageUrls) {
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("❌ 존재하지 않는 상품 ID: " + productId);
        }

        for (String imageUrl : imageUrls) {
            ProductDetailImage detailImage = new ProductDetailImage(productId, imageUrl);
            productDetailImageRepository.save(detailImage);
        }

        System.out.println("✅ [상세 이미지 저장 완료] 상품 ID: " + productId + " | 저장된 이미지 개수: " + imageUrls.size());
    }

    /**
     * ✅ 특정 상품의 상세 이미지 조회
     */
    public List<ProductDetailImage> getDetailImagesByProductId(Long productId) {
        return productDetailImageRepository.findByProduct_ProductId(productId);
    }

    /**
     * ✅ 특정 상품의 모든 상세 이미지 삭제
     */
    public void deleteDetailImagesByProductId(Long productId) {
        List<ProductDetailImage> images = productDetailImageRepository.findByProduct_ProductId(productId);
        productDetailImageRepository.deleteAll(images);
        System.out.println("🗑 [상세 이미지 삭제 완료] 상품 ID: " + productId);
    }

    //db에서 상품url가져오기
    public List<Map<String, Object>> findAllProductDetails() {
        List<Product> productList = productRepository.findAll();
    
        System.out.println("📌 [디버그] DB에서 가져온 상품 개수: " + productList.size());
    
        if (productList.isEmpty()) {
            System.out.println("🚨 [오류] DB에 상품이 없습니다!");
        }
    
        return productList.stream()
            .map(product -> {
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("productId", product.getProductId());
                productMap.put("detailUrl", product.getDetailUrl());
                return productMap;
            })
            .collect(Collectors.toList());
    }
    
}



