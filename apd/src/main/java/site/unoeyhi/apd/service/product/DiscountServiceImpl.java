package site.unoeyhi.apd.service.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.entity.Discount;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.repository.product.DiscountRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;

    public DiscountServiceImpl(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    @Override
    public void saveDiscount(Product product, String discountType, double discountValue) {
        Discount discount = Discount.builder()
                .product(product)
                .discountType(discountType)
                .discountValue(discountValue)
                .startDate(LocalDate.now()) // ✅ 크롤링 시점이 할인 시작일
                .endDate(null) // ✅ 종료일은 크롤링 데이터에 없으므로 null 처리
                .createdAt(LocalDateTime.now())
                .build();

        discountRepository.save(discount);
        System.out.println("✅ [DiscountService] 할인 정보 저장 완료: " + discount);
    }

    @Override
    public Discount findByProductId(Long productId) {
        return discountRepository.findByProduct_ProductId(productId);
    }
}
