package site.unoeyhi.apd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.AP_NORTHEAST_2) // ✅ 사용자의 AWS 지역 설정 (예: 서울은 AP_NORTHEAST_2)
                .credentialsProvider(DefaultCredentialsProvider.create()) // ✅ 기본 AWS 자격 증명 사용
                .build();
    }
}
