package site.unoeyhi.apd.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartRequestDto { //controller 검증용
    @NotNull(message = "Product ID must not be null")
    private Long productId;

    @NotNull(message = "Quantity must not be null")
    private Integer quantity;
}