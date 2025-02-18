package site.unoeyhi.apd.entity.dto;

import java.util.ArrayList;
import java.util.List;

public class CategoryDto {
  private String name;  // ✅ 카테고리명
  private String url;   // ✅ 카테고리 URL 추가
  private Long categoryId; // ✅ 기본 키 추가
    // 생성자
    public CategoryDto(String Name, String Url , Long categoryId) {
        this.name = Name;
        this.url = Url;
        this.categoryId = categoryId;
    }

    public List<CategoryDto> scrapCategories() {
    List<CategoryDto> categoryList = new ArrayList<>();

    // 기존 크롤링 로직 수행 후, DTO 변환
    categoryList.add(new CategoryDto(name, url, categoryId));

    return categoryList;
}

    // Getter & Setter
    public String getCategoryName() { return name; }
    public void setCategoryName(String categoryName) { this.name = categoryName; }

    public String getCategoryUrl() { return url; }
    public void setCategoryUrl(String categoryUrl) { this.url = categoryUrl; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

  }
