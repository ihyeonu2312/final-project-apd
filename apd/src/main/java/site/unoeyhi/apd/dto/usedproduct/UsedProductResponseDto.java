package site.unoeyhi.apd.dto.usedproduct;

import java.math.BigDecimal;
import java.util.List;

public record UsedProductResponseDto(
    Integer id,
    String name,
    String description,
    BigDecimal price,
    String condition,
    String status,
    String sellerNickname,
    List<String> imageUrls
) {}
