package site.unoeyhi.apd.service.product;

import java.util.List;

import site.unoeyhi.apd.entity.ProductOption;

public interface ProductOptionService {
  ProductOption saveProductOption(ProductOption productOption);
  List<ProductOption> getAllProductOptions();
  ProductOption getProductOptionById(Long id);
  void deleteProductOption(Long id);
  ProductOption save(ProductOption productOption); // ✅ 저장 메서드 추가


}