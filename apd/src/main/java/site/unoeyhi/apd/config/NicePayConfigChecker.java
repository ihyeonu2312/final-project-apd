package site.unoeyhi.apd.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class NicePayConfigChecker {

    @Value("${nicepay.api.auth-url:NOT_SET}")
    private String authUrl;

    @Value("${nicepay.client.id:NOT_SET}")
    private String clientId;

    @Value("${nicepay.client.secret:NOT_SET}")
    private String clientSecret;

    @PostConstruct
    public void checkConfig() {
        System.out.println("✅ NICEPAY AUTH URL: " + authUrl);
        System.out.println("✅ NICEPAY CLIENT ID: " + clientId);
        System.out.println("✅ NICEPAY CLIENT SECRET: " + clientSecret);
    }
}
