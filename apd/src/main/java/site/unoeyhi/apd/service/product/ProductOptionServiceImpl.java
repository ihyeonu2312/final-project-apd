package site.unoeyhi.apd.service.product;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import site.unoeyhi.apd.entity.ProductOption;
import site.unoeyhi.apd.repository.product.ProductOptionRepository;

@Service
@Transactional
public class ProductOptionServiceImpl implements ProductOptionService {

    private final ProductOptionRepository productOptionRepository;

    public ProductOptionServiceImpl(ProductOptionRepository productOptionRepository) {
        this.productOptionRepository = productOptionRepository;
    }

    @Override
    public ProductOption saveProductOption(ProductOption productOption) {
        return productOptionRepository.save(productOption);
    }

    @Override
    public List<ProductOption> getAllProductOptions() {
        return productOptionRepository.findAll();
    }

    @Override
    public ProductOption getProductOptionById(Long id) {
        return productOptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 ProductOption이 존재하지 않습니다: " + id));
    }

    @Override
    public void deleteProductOption(Long id) {
        productOptionRepository.deleteById(id);
    }

    @Override
    public ProductOption save(ProductOption productOption) {
        return productOptionRepository.save(productOption); // ✅ DB에 저장
    }
}