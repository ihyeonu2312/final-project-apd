package site.unoeyhi.apd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.unoeyhi.apd.entity.UsedProductImage;

import java.util.List;

@Repository
public interface UsedProductImageRepository extends JpaRepository<UsedProductImage, Long> {

    // 특정 상품에 대한 이미지 목록 조회
    List<UsedProductImage> findByUsedProduct_UsedProductId(Integer usedProductId);

    // 상품 ID로 전체 삭제 (옵션)
    void deleteByUsedProduct_UsedProductId(Integer usedProductId);
}
