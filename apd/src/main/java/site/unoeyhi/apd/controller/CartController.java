package site.unoeyhi.apd.controller;

import org.springframework.web.bind.annotation.*;
import site.unoeyhi.apd.entity.CartItem;
import site.unoeyhi.apd.entity.dto.CartItemDto;
import site.unoeyhi.apd.service.CartService;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public CartItem addProductToCart(@RequestBody CartItemDto cartItemDto) {
        return cartService.addProductToCart(cartItemDto);
    }
}
