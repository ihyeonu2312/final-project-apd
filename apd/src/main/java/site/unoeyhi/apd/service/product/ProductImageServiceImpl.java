package site.unoeyhi.apd.service.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.dto.ProductImageDto;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.entity.ProductImage;
import site.unoeyhi.apd.repository.product.ProductImageRepository;
import site.unoeyhi.apd.repository.product.ProductRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    public ProductImageServiceImpl(ProductImageRepository productImageRepository, ProductRepository productRepository) {
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public ProductImage saveProductImage(ProductImageDto productImageDto) {
        Product product = productRepository.findById(productImageDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("🚨 상품 ID가 존재하지 않습니다: " + productImageDto.getProductId()));

        ProductImage productImage = ProductImage.builder()
                .product(product)
                .imageUrl(productImageDto.getImageUrl())
                .build();

        return productImageRepository.save(productImage);
    }

    @Override
    public List<String> getProductImages(Long productId) {
        List<ProductImage> images = productImageRepository.findByProductProductId(productId);
        return images.stream().map(ProductImage::getImageUrl).collect(Collectors.toList());
    }
}
