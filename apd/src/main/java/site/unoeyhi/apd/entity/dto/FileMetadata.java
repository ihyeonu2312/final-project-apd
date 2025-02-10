package site.unoeyhi.apd.entity.dto;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;
import site.unoeyhi.apd.entity.Product;

@Entity(name = "tbl_file_metadata")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "product")
public class FileMetadata {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer fileId;  // 파일 ID (기본키)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;  // 상품과 연관 (FK -> Product)

  private String fileName;  // 파일 이름
  private String s3Url;     // S3 파일 URL
  private Long fileSize;    // 파일 크기 (바이트)
  private String contentType;  // 파일 MIME 타입
  private LocalDateTime createdAt;  // 파일 업로드 시간
}
