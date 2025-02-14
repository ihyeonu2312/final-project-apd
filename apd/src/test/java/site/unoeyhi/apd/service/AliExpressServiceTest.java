package site.unoeyhi.apd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.repository.CategoryRepository;
import site.unoeyhi.apd.repository.ProductRepository;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AliExpressServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private AliExpressService aliExpressService;

    @BeforeAll
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("✅ 크롤링한 상품이 데이터베이스에 저장되는지 확인")
    void testFetchProductDetails() {
        // Given
        String url = "https://www.aliexpress.com/category/100003109/women-clothing.html";
        int maxProducts = 3;
    
        Category category = new Category();
        category.setName("FASHION");
    
        // ✅ 카테고리 조회가 정상적으로 동작하는지 Mock 설정
        when(categoryRepository.findByName("FASHION")).thenReturn(Optional.of(category));
    
        // ✅ Mock 동작 설정: 저장할 때 리스트에 추가되도록 함
        doAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            System.out.println("✅ 저장된 상품: " + product.getName());
            return product;
        }).when(productRepository).save(any(Product.class));
    
        // When
        List<String> productNames = aliExpressService.fetchProductDetails(url, maxProducts);
    
        // Then
        assertThat(productNames).isNotEmpty();  // 상품 리스트가 비어 있지 않아야 함
        verify(productRepository, atLeast(1)).save(any(Product.class)); // 최소한 한 개의 상품이 저장되어야 함
    }
    
}
