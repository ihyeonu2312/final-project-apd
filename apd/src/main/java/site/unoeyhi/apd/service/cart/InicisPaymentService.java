package site.unoeyhi.apd.service.cart;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import site.unoeyhi.apd.dto.cart.InicisPaymentRequestDto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

@Service
public class InicisPaymentService {

    @Value("${spring.inicis.mid}")
    private String mid;

    @Value("${spring.inicis.signKey}")
    private String signKey;

    @Value("${spring.inicis.apiUrl}")
    private String apiUrl;

    public InicisPaymentRequestDto createPaymentRequest(Long amount, String orderId, String buyerName) {
        String timestamp = getCurrentTimestamp();
        String hashData = generateHashData(mid, orderId, amount, timestamp, signKey);

        return InicisPaymentRequestDto.builder()
                .mid(mid)
                .orderId(orderId)
                .price(amount.toString())
                .buyerName(buyerName)
                .timestamp(timestamp)
                .hashData(hashData)
                .apiUrl(apiUrl)
                .build();
    }

    private String generateHashData(String mid, String orderId, Long amount, String timestamp, String signKey) {
        try {
            String data = mid + orderId + amount + timestamp + signKey;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing error", e);
        }
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
