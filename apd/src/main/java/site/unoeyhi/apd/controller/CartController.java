// package site.unoeyhi.apd.controller;

// import jakarta.validation.Valid;
// import site.unoeyhi.apd.entity.dto.CartItemDto;
// import site.unoeyhi.apd.entity.dto.CartRequestDto;
// import site.unoeyhi.apd.service.CartService;

// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/cart")
// public class CartController {

//     private final CartService cartService;

//     public CartController(CartService cartService) {
//         this.cartService = cartService;
//     }

//     @PostMapping("/add")
//     public ResponseEntity<CartItemDto> addItemToCart(@Valid @RequestBody CartRequestDto cartRequest) {
//         return cartService.addItemToCart(cartRequest);
//     }
// }

