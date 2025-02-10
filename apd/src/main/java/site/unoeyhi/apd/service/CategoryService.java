package site.unoeyhi.apd.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

   // 여러 카테고리를 ID 리스트로 조회하는 메서드
  public List<Category> findCategoriesByIds(List<Long> categoryIds) {
    return categoryRepository.findAllById(categoryIds);
  }

  // 필요하면 단일 카테고리 조회 메서드도 제공
  public Category findById(Long categoryId) {
      return categoryRepository.findById(categoryId)
              .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
  }
}
