package site.unoeyhi.apd.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "Cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    @ManyToOne
    @JoinColumn(name = "cart_member_id")  // cart_member_id 컬럼과 Member 엔티티를 매핑
    private Member member;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CartItem> cartItems = new ArrayList<>();
    
    public void addCartItem(CartItem cartItem) {
        this.cartItems.add(cartItem);
        cartItem.setCart(this);
    }

    @Column(nullable = false)
    private LocalDateTime updatedAt;  // 수정 시간 추가

    @Column(name = "created_at")
    private LocalDateTime createdAt;

       @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    @Override
    public String toString() {
        return "Cart{" +
               "cartId=" + cartId +
               ", member=" + member.getName() + 
               ", cartItems=" + cartItems.size() + 
               '}';
    }
    

}