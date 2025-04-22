package site.unoeyhi.apd.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentInitiateResponseDto {
    private String paymentUrl; // NICE 리디렉트 URL
    private String paymentStatus; // PENDING
}