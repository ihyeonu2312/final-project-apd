// package site.unoeyhi.apd.repository;

// import org.junit.jupiter.api.*;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.test.annotation.Rollback;
// import org.springframework.transaction.annotation.Transactional;

// import lombok.extern.log4j.Log4j2;
// import site.unoeyhi.apd.entity.Category;
// import site.unoeyhi.apd.entity.Product;
// import site.unoeyhi.apd.repository.product.ProductRepository;

// import java.time.LocalDateTime;
// import java.util.List;

// import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest
// @Transactional
// @Rollback(false)
// @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
// @Log4j2
// class CategoryRepositoryTest {

//     @Autowired
//     private CategoryRepository categoryRepository;

//     @Autowired
//     private ProductRepository productRepository;

//     private Category category;
//     private Product product;

// @Test
// void test(){
//     List<Category> category= categoryRepository.findAll();
//     log.info(category);
// }


//     @BeforeEach
//     void setup() {
//         // ✅ 카테고리 생성 및 저장
//         category = new Category();
//         category.setCategoryName("패션");
//         categoryRepository.save(category);

//         // ✅ 상품 생성 및 저장
//         product = new Product();
//         product.setName("Test Product");
//         product.setPrice(100.0);
//         product.setStockQuantity(10);
//         product.setCreatedAt(LocalDateTime.now());
//         product.setUpdatedAt(LocalDateTime.now());
//         product.setCategory(category);
//         productRepository.save(product);
//     }

//     @Test
//     @DisplayName("✅ 카테고리 저장 및 상품 연결 테스트")
//     void testCategoryWithProduct() {
//         // ✅ when: 상품 조회
//         Product foundProduct = productRepository.findById(product.getProductId()).orElseThrow();

//         // ✅ then: 단일 카테고리 검증
//         assertThat(foundProduct.getCategory().getCategoryName()).isEqualTo("패션");
//     }
// }
