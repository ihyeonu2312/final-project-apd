package site.unoeyhi.apd.dto.cart;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InicisPaymentRequestDto {
    private String mid;
    private String orderId;
    private String price;
    private String buyerName;
    private String timestamp;
    private String hashData;
    private String apiUrl;
}
