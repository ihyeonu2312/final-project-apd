package site.unoeyhi.apd.service.product;

import site.unoeyhi.apd.dto.ProductDto;
import site.unoeyhi.apd.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    Product saveProduct(ProductDto productDto);
    List<Product> getProductsByCategory(String coupangCategoryKey);
    List<Product> getAllProducts();
    Optional<Product> findByTitle(String title);
    
}

