package site.unoeyhi.apd.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.repository.ProductRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
class AliExpressServiceTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("✅ 크롤링된 상품이 DB에 저장되는지 테스트")
    void testCrawledProductsSaved() {
        // Given
        Product product = Product.builder()
                .name("테스트 상품")
                .description("크롤링된 상품")
                .price(100.0)
                .stockQuantity(10)
                .imageUrl("https://example.com/sample.jpg")
                .build();

        // When
        productRepository.save(product);
        List<Product> products = productRepository.findAll();

        // Then
        assertThat(products).isNotEmpty();
        assertThat(products.get(0).getImageUrl()).isEqualTo("https://example.com/sample.jpg");
    }
}