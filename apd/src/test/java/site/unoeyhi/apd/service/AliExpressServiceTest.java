package site.unoeyhi.apd.service;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.repository.ProductRepository;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // ✅ 실제 DB 사용
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AliExpressServiceTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AliExpressService aliExpressService;

    @Test
    @DisplayName("✅ 크롤링한 상품이 실제 DB에 저장되는지 확인")
    void testFetchProductDetails() {
        // Given
        String url = "https://www.aliexpress.com/p/calp-plus/index.html?categoryTab=kr_home_appliances";
        int maxProducts = 3;

        // When
        List<String> productNames = aliExpressService.fetchProductDetails(url, maxProducts);

        // Then
        List<Product> savedProducts = productRepository.findAll();
        System.out.println("✅ DB에 저장된 상품 개수: " + savedProducts.size());

        assertThat(savedProducts).hasSize(productNames.size());  // ✅ 크롤링된 개수와 저장된 개수 비교
    }
}
