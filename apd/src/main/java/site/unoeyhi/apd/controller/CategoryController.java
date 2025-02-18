package site.unoeyhi.apd.controller;

import org.springframework.web.bind.annotation.*;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public Category addCategory(@RequestBody Category category) {
        return categoryService.saveCategory(category);
    }

    @GetMapping
    public List<Category> getCategories() {
        return categoryService.getAllCategories();
    }
}
