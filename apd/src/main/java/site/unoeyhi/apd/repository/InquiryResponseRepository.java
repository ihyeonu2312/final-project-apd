package site.unoeyhi.apd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.unoeyhi.apd.entity.InquiryResponse;

import java.util.List;

public interface InquiryResponseRepository extends JpaRepository<InquiryResponse, Long> {
    List<InquiryResponse> findByInquiry_InquiryId(Long inquiryId); // 특정 문의에 대한 답변 목록
}
