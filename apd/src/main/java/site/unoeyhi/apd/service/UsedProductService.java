package site.unoeyhi.apd.service;

import site.unoeyhi.apd.dto.usedproduct.UsedProductResponseDto;
import site.unoeyhi.apd.entity.UsedProduct;

import java.util.List;
import java.util.Optional;

public interface UsedProductService {
    UsedProduct createProduct(UsedProduct product);
    Optional<UsedProduct> findById(Integer id);
    List<UsedProduct> findAll();
    List<UsedProductResponseDto> findAllDtos(); // ✅ 추가
    void deleteById(Integer id);
}
