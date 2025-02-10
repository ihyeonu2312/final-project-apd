package site.unoeyhi.apd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.unoeyhi.apd.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 필요한 추가적인 쿼리 메소드들을 정의할 수 있습니다.
}