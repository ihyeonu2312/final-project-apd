package site.unoeyhi.apd.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Table(name = "Cart_Item")
@Getter
@Setter
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)  // cart_id 컬럼과 Cart 엔티티를 매핑
    private Cart cart; // 장바구니 (FK -> Cart)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // 상품 (FK -> Product)

    @Column(name = "quantity")
    private int quantity;

    @Column(nullable = false)
    private LocalDateTime updatedAt;  // 수정 시간 추가

    @Column(name = "created_at")
    private LocalDateTime createdAt;


     // 기본 생성자 추가
     public CartItem() {
    }

    // 생성자나 메서드를 추가하여 유효성 검사 및 값 설정
    public CartItem(Cart cart, Product product, int quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    // 수량 업데이트 메서드 추가
    public void updateQuantity(int quantity) {
        this.quantity = quantity;
        this.updatedAt = LocalDateTime.now();  // 수정 시간이 갱신
    }
    @Override
    public String toString() {
        return "CartItem{" +
               "cartItemId=" + cartItemId +
               ", cartId=" + cart.getCartId() + 
               '}';
    }
}