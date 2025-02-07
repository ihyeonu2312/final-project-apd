package site.unoeyhi.apd.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
public class S3Service {

    private final S3Client s3Client;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(String key, byte[] content, String mimeType) {
        try {
            // S3 버킷에 업로드하는 로직
            InputStream inputStream = new ByteArrayInputStream(content);

            // PutObjectRequest 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket("your-bucket-name") // 실제 버킷 이름
                    .key(key)  // S3 객체 키 (파일명)
                    .contentType(mimeType)  // MIME 타입
                    .build();

            // S3에 파일 업로드
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, content.length));

            // 업로드 후 S3 경로를 반환
            return "s3://your-bucket-name/" + key;  // S3 경로 반환

        } catch (Exception e) {
            e.printStackTrace();
            return null; // 오류 발생 시 null 반환
        }
    }
}