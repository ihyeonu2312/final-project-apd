package site.unoeyhi.apd.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.repository.CategoryRepository;
import site.unoeyhi.apd.repository.ProductRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Rollback(false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @Order(1)
    @DisplayName("✅ 부모 카테고리 및 하위 카테고리 저장 테스트")
    void testSaveCategoryWithParent() {
        // ✅ 부모 카테고리 생성
        Category fashionCategory = Category.builder().name("패션").build();
        categoryRepository.save(fashionCategory);

        // ✅ 하위 카테고리 추가
        Category menFashion = Category.builder().name("남성 패션").parentCategory(fashionCategory).build();
        Category womenFashion = Category.builder().name("여성 패션").parentCategory(fashionCategory).build();
        categoryRepository.saveAll(List.of(menFashion, womenFashion));

        // ✅ 상품 추가
        Product product1 = Product.builder().name("남성 정장").price(100.0).stockQuantity(10).categories(List.of(menFashion)).build();
        Product product2 = Product.builder().name("여성 원피스").price(120.0).stockQuantity(15).categories(List.of(womenFashion)).build();
        productRepository.saveAll(List.of(product1, product2));

        // ✅ 검증
        List<Category> allCategories = categoryRepository.findAll();
        assertThat(allCategories).isNotEmpty();

        // ✅ `parentCategory`가 올바르게 설정되었는지 확인
        assertThat(menFashion.getParentCategory()).isNotNull();
        assertThat(menFashion.getParentCategory().getName()).isEqualTo("패션");

        assertThat(womenFashion.getParentCategory()).isNotNull();
        assertThat(womenFashion.getParentCategory().getName()).isEqualTo("패션");

        // ✅ 상품과 카테고리 연관 관계 확인
        assertThat(product1.getCategories()).contains(menFashion);
        assertThat(product2.getCategories()).contains(womenFashion);
    }
}
