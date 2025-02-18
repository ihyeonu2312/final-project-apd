package site.unoeyhi.apd.controller;

import org.springframework.web.bind.annotation.*;

import site.unoeyhi.apd.model.CategoryModel;
import site.unoeyhi.apd.service.AliExpressService;

import java.util.List;

@RestController
@RequestMapping("/api/aliexpress")
public class AliExpressController {

    private final AliExpressService aliExpressService;

    public AliExpressController(AliExpressService aliExpressService) {
        this.aliExpressService = aliExpressService;
    }

    // 크롤링 실행 후 DB에 저장하는 API
    @PostMapping("/scrap")
    public List<CategoryModel> scrapAndSaveCategories() {
        return aliExpressService.scrapAndSaveCategories();
    }

    // 저장된 카테고리 조회 API
    @GetMapping("/categories")
    public List<CategoryModel> getAllCategories() {
        return aliExpressService.getAllCategory();
    }
}
