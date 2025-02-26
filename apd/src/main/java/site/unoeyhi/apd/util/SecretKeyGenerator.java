package site.unoeyhi.apd.util;

import java.util.Base64;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class SecretKeyGenerator {
    public static void main(String[] args) {
        try {
            // 🔹 HmacSHA256 키 생성
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            keyGen.init(256);
            SecretKey secretKey = keyGen.generateKey();

            // 🔹 Base64 인코딩된 Secret Key 출력
            String base64Key = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            System.out.println("✅ Base64 Secret Key: " + base64Key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
