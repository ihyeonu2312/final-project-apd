package site.unoeyhi.apd.service.product;

import site.unoeyhi.apd.entity.Discount;
import site.unoeyhi.apd.entity.Product;

public interface DiscountService {
    void saveDiscount(Product product, String discountType, double discountValue);
    Discount findByProductId(Long productId);
}
