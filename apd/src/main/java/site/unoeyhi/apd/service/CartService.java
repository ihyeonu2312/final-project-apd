package site.unoeyhi.apd.service;

import site.unoeyhi.apd.entity.Cart;
import site.unoeyhi.apd.entity.CartItem;
import site.unoeyhi.apd.entity.Member;

import java.util.List;

public interface CartService {
    List<Cart> getAllCart(Member member);

    Cart getCartForMember(Member member);

    List<CartItem> getCartItems(Member member);

    void addItemCart(Member member, Long productId, int quantity);

    void removeItemFromCart(Member member, Long productId);
}
