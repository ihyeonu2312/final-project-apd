package site.unoeyhi.apd.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.entity.UsedProduct;
import site.unoeyhi.apd.repository.UsedProductRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsedProductService {

    private final UsedProductRepository usedProductRepository;

    public UsedProduct findById(Integer productId) {
        return usedProductRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
    }
}
