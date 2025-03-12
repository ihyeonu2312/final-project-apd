package site.unoeyhi.apd.service.cart;

public interface CartService {
    void addToCart(Long memberId, Long productId, int quantity);
}
